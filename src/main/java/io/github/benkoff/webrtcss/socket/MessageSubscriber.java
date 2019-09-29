package io.github.benkoff.webrtcss.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.benkoff.webrtcss.config.SpringContext;
import io.github.benkoff.webrtcss.domain.MessageFrame;
import io.github.benkoff.webrtcss.domain.Room;
import io.github.benkoff.webrtcss.domain.RoomService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class MessageSubscriber {
    // sessionId, Queue
    private static Map<String, Sinks.Many<MessageFrame>> messageBufferMap = new HashMap<>();
    // sessionId, Room
    private static Map<String, Room> sessionIdToRoomMap = new HashMap<>();

    private static final String MSG_TYPE_TEXT = "text"; // text message
    private static final String MSG_TYPE_OFFER = "offer"; // SDP Offer message
    private static final String MSG_TYPE_ANSWER = "answer"; // SDP Answer message
    private static final String MSG_TYPE_ICE = "ice"; // New ICE Candidate message
    private static final String MSG_TYPE_JOIN = "join"; // join room data message
    private static final String MSG_TYPE_LEAVE = "leave"; // leave room data message
    private final RoomService roomService = SpringContext.getBean(RoomService.class);
    private final ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
    private final WebSocketSession session;
    private final Sinks.Many<MessageFrame> many;

    private final String sessionId;

    public MessageSubscriber(WebSocketSession session) {
        this.many = Sinks.many().unicast().onBackpressureBuffer();
        this.session = session;
        this.sessionId = session.getId();
        afterConnectionEstablished();
    }

    public void onNext(MessageFrame message) {
        handleTextMessage(message);
    }

    public void onError(Throwable error) {
        //TODO log error
        error.printStackTrace();
    }

    public void onComplete() {
        afterConnectionClosed();
    }


    /**
     * webSocket has been opened, send a message to the client
     * when data field contains 'true' value, the client starts negotiating
     * to establish peer-to-peer connection, otherwise they wait for a counterpart
     */
    public void afterConnectionEstablished() {
        log.debug("[ws] Session has been established with status {}");
        MessageFrame payload = MessageFrame.builder()
                .from("Server")
                .type(MSG_TYPE_JOIN)
                .data(String.valueOf(true))
                .build();
        many.tryEmitNext(payload);
        messageBufferMap.put(sessionId, many);
    }

    public void afterConnectionClosed() {
        log.debug("[ws] Session has been closed with status");
        // lastReceivedEvent.ifPresent(messageFrame -> events.add(MessageFrame.builder()
        //         .build()));
        Sinks.Many<MessageFrame> buffer = messageBufferMap.get(sessionId);
        messageBufferMap.remove(sessionId);
    }

    /**
     * a message has been received
     */
    protected void handleTextMessage(MessageFrame messageFrame) {
        try {
            log.info("[ws] Message sessionId: {} message: {} received", sessionId, messageFrame);
            String userName = messageFrame.getFrom(); // origin of the message
            Room room;
            switch (messageFrame.getType()) {
                // text message from client has been received
                case MSG_TYPE_TEXT:
                    log.info("[ws] Text message: {}", messageFrame);
                    break;
                // process signal received from client
                case MSG_TYPE_OFFER:
                case MSG_TYPE_ANSWER:
                case MSG_TYPE_ICE:
                    Object candidate = messageFrame.getCandidate();
                    Object sdp = messageFrame.getSdp();
                    log.debug("[ws] Signal: {}",
                            candidate != null
                                    ? candidate.toString().substring(0, 64)
                                    : sdp.toString().substring(0, 64));

                    Room rm = sessionIdToRoomMap.get(sessionId);
                    if (rm != null) {
                        List<String> sessionIds = roomService.getClients(rm);
                        for (String sessionId : sessionIds) {
                            if (!sessionId.equals(this.sessionId) && messageBufferMap.containsKey(sessionId))
                                messageBufferMap.get(sessionId).tryEmitNext(MessageFrame.builder()
                                        .from(userName)
                                        .type(messageFrame.getType())
                                        .data(messageFrame.getData())
                                        .candidate(candidate)
                                        .sdp(sdp)
                                        .build());
                        }
                    }
                    break;

                // identify user and their opponent
                case MSG_TYPE_JOIN:
                    // message.data contains connected room id
                    log.info("[ws] {} has joined Room: #{}", userName, messageFrame.getData());
                    room = roomService.findRoomByStringId(messageFrame.getData())
                            .orElseThrow(() -> new IOException("Invalid room number received!"));
                    // add client to the Room clients list
                    roomService.addClient(room, sessionId);
                    sessionIdToRoomMap.put(sessionId, room);
                    break;

                case MSG_TYPE_LEAVE:
                    // message data contains connected room id
                    // room id taken by session id
                    // remove the client which leaves from the Room clients list
                    log.info("[ws] {} is going to leave Room: #{}", userName, messageFrame.getData());
                    room = sessionIdToRoomMap.remove(sessionId);
                    roomService.getClients(room).stream()
                            .filter(sessionId -> sessionId.equals(this.sessionId))
                            .findFirst()
                            .ifPresent((sessionId) -> roomService.removeClientByName(room, sessionId));
                    if (roomService.getClients(room).size() == 0)
                        roomService.removeRoom(room);
                    break;
                // something should be wrong with the received message, since it's type is unrecognizable
                default:
                    log.debug("[ws] Type of the received message {} is undefined!", messageFrame.getType());
                    // handle this if needed
            }

        } catch (IOException e) {
            log.debug("An error occured: {}", e.getMessage());
        }
    }
}
