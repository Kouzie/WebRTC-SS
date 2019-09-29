package io.github.benkoff.webrtcss.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessRoomDto {
    private String sid;
    private String uuid;
    private String action;

    public void setId(String id) {
        this.sid = id;
    }
}
