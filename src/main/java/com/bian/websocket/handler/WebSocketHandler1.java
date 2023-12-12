package com.bian.websocket.handler;

import com.bian.websocket.context.ClientContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Log4j2
@Component
@ServerEndpoint("/websocket/achieve-1")
public class WebSocketHandler1 {
    private final ClientContext clientContext;

    public WebSocketHandler1() {
        log.info("WebSocketHandler1 构造方法");
        this.clientContext = ClientContext.getClientContext();
    }

    @OnOpen
    public void afterConnectionEstablished(Session session, EndpointConfig config) {
        log.info("Client is connected: " + session.getId());
        //clientContext.addClient(new Client(session.getId(), session));
    }

    @OnMessage
    public void handleMessage(Session session, String message) throws IOException {
        log.info("handleMessage: " + message + " " + session);
        clientContext.refreshHeartbeatTime(session.getId());
        session.getBasicRemote().sendText("Reversed: " + new StringBuilder(message).reverse());
    }

    @OnError
    public void handleTransportError(Session session, Throwable exception) throws IOException {
        log.warn("WebSocket connection error. " + exception.getMessage());
        clientContext.removeAndKillClient(session.getId());
    }

    @OnClose
    public void afterConnectionClosed(Session session, CloseReason closeReason) {
        log.info("WebSocket connection closed. " + closeReason);
        clientContext.removeAndKillClient(session.getId());
    }
}