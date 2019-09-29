package io.github.benkoff.webrtcss.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class Parser {

    public Optional<Long> parseId(String sid) {
        Long id = null;
        try {
            id = Long.valueOf(sid);
        } catch (Exception e) {
            log.error("An error occured: {}", e.getMessage());
        }

        return Optional.ofNullable(id);
    }
}
