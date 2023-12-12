package com.bian.websocket.handler;

import com.bian.websocket.context.ClientContext;
import com.bian.websocket.entity.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Log4j2
@Component
public class WebSocketHandler2 extends TextWebSocketHandler {

    private final ClientContext clientContext;

    public WebSocketHandler2() {
        log.info("WebSocketHandler2 构造方法");
        this.clientContext = ClientContext.getClientContext();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Client is connected: " + session.getId());
        clientContext.addClient(new Client(session.getId(), session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("handleMessage: " + message + " " + session);
        clientContext.refreshHeartbeatTime(session.getId());
        session.sendMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket connection error. " + exception.getMessage());
        clientContext.removeAndKillClient(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed. " + status);
        session.close(status);
        clientContext.removeAndKillClient(session.getId());
    }
}
