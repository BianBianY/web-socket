package com.bian.websocket.entity;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.EncodeException;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class Client {
    private final String id;
    private final WebSocketSession session;
    private final AtomicBoolean isAlive;
    private final AtomicReference<Instant> lastHeartbeatTime;

    public Client(String id, WebSocketSession session) {
        this.id = id;
        this.session = session;
        this.isAlive = new AtomicBoolean(true);
        this.lastHeartbeatTime = new AtomicReference<>(Instant.now());
    }

    public boolean sendMessage(String message) throws IOException {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (session.isOpen()) {
            log.info("发送信息: " + message + "; session id: " + session.getId());
            session.sendMessage(new TextMessage(message));
            return true;
        }
        log.info(session + ":发送信息失败");
        return false;
    }

    public boolean sendObject(Object object) throws EncodeException, IOException {
        if (session.isOpen()) {
            log.info("发送信息: " + object + "; session id: " + session.getId());
            session.sendMessage(new TextMessage("message"));
            return true;
        }
        log.info(session + ":发送信息失败");
        return false;
    }

    public void killClient() {
        try {
            session.close();
        } catch (Exception e) {
            throw new RuntimeException("killClient fail:" + e.getMessage());
        }
        this.isAlive.set(false);
    }

    public void refreshHeartbeatTime() {
        if (!isAlive()) {
            throw new RuntimeException(id + "do not refresh Heartbeat Time");
        }
        lastHeartbeatTime.set(Instant.now());
    }

    public boolean isAlive() {
        return isAlive.get() && session.isOpen();
    }

    public String getId() {
        return id;
    }
}
