package io.github.benkoff.webrtcss.domain;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MessageFrame {
    private String from;
    private String type;
    private String data;
    private Object candidate;
    private Object sdp;

}
