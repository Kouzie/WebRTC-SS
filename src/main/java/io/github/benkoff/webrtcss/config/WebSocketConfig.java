package io.github.benkoff.webrtcss.config;

import io.github.benkoff.webrtcss.domain.MessageFrame;
import io.github.benkoff.webrtcss.socket.MyWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Configuration
class WebSocketConfig {

    /*@Bean // tomcat, jetty options
    public WebSocketService webSocketService() {
        TomcatRequestUpgradeStrategy strategy = new TomcatRequestUpgradeStrategy();
        strategy.setMaxSessionIdleTimeout(0L);
        return new HandshakeWebSocketService(strategy);
    }*/

    @Bean
    public HandlerMapping handlerMapping(MyWebSocketHandler myWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/signal", myWebSocketHandler);
        int order = -1; // before annotated controllers
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping(map, order);
        // ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        // client.setMaxFramePayloadLength(2097152);
        return simpleUrlHandlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        //return new WebSocketHandlerAdapter(webSocketService());
        return new WebSocketHandlerAdapter();
    }
}