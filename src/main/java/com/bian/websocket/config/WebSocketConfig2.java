package com.bian.websocket.config;

import com.bian.websocket.handler.WebSocketHandler2;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@Log4j2
public class WebSocketConfig2 implements WebSocketConfigurer {
    private final WebSocketHandler2 webSocketHandler;

    public WebSocketConfig2(WebSocketHandler2 webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/websocket/achieve-2")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
        registry.addHandler(webSocketHandler, "/websocket/achieve-2/sock-js").withSockJS();
    }
}
