package io.github.benkoff.webrtcss.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;
import java.util.*;

@Getter
@Setter
@EqualsAndHashCode
public class Room {
    @NotNull private final Long id;
    private List<String> clients;
    private Flux<MessageFrame> outputEvents;

    public Room(Long id) {
        this.id = id;
        this.clients = new LinkedList<>();
    }
}
