package io.github.benkoff.webrtcss.domain;

import io.github.benkoff.webrtcss.util.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;

@Service
public class RoomService {    
    private final Parser parser;
    // repository substitution since this is a very simple realization
    private final Set<Room> rooms = new TreeSet<>(Comparator.comparing(Room::getId));

    @Autowired
    public RoomService(final Parser parser) {
        this.parser = parser;
    }

    public Set<Room> getRooms() {
        final TreeSet<Room> defensiveCopy = new TreeSet<>(Comparator.comparing(Room::getId));
        defensiveCopy.addAll(rooms);

        return defensiveCopy;
    }

    public Boolean addRoom(final Room room) {
        return rooms.add(room);
    }

    public Boolean removeRoom(final Room room) {
        return rooms.remove(room);
    }

    public Optional<Room> findRoomByStringId(final String sid) {
        // simple get() because of parser errors handling
        return rooms.stream().filter(r -> r.getId().equals(parser.parseId(sid).get())).findAny();
    }

    public Long getRoomId(Room room) {
        return room.getId();
    }

    public List<String> getClients(final Room room) {
        return Optional.ofNullable(room)
                .map(r -> Collections.unmodifiableList(r.getClients()))
                .orElse(Collections.emptyList());
    }

    public void addClient(final Room room, final String sessionId) {
        room.getClients().add(sessionId);
    }

    public void removeClientByName(final Room room, final String sessionId) {
        room.getClients().remove(sessionId);
    }
}
