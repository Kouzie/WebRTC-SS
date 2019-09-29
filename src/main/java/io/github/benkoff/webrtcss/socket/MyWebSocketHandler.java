package io.github.benkoff.webrtcss.socket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.benkoff.webrtcss.domain.MessageFrame;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    // session id to room mapping

    /**
     * 모든 작업을 하나의 handle 매서드 안에 모두 정의해야함
     */
    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        MessageSubscriber subscriber = new MessageSubscriber(session);
        return session.receive()
                .map(this::toMessageFrame) // socket message -> MessageFrame POJO
                .doOnNext(subscriber::onNext)
                .doOnError(subscriber::onError)
                .doOnComplete(subscriber::onComplete)
                .zipWith(session.send(subscriber.getMany().asFlux().map(messageFrame -> {
                    try {
                        return session.textMessage(objectMapper.writeValueAsString(messageFrame));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return session.textMessage(e.getMessage());
                    }
                })))
                .then();
    }

    private MessageFrame toMessageFrame(WebSocketMessage message) {
        try {
            MessageFrame messageFrame = objectMapper.readValue(message.getPayloadAsText(), MessageFrame.class);
            return messageFrame;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return MessageFrame.builder()
                    .build();
        }
    }

    private WebSocketMessage sendMessage(WebSocketSession session, MessageFrame messageFrame) throws JsonProcessingException {
        log.info("send message:{}", messageFrame);
        return session.textMessage(objectMapper.writeValueAsString(messageFrame));
    }
}
