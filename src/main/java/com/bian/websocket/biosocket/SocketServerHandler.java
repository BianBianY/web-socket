package com.bian.websocket.biosocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class SocketServerHandler {

    @Async
    public void afterConnectionEstablished(Socket client) throws IOException {
        log.info("Client is connected: " + client);
        while (true) {
            InputStream inputStream = client.getInputStream();
            int length;
            byte[] data = new byte[1024];
            if ((length = inputStream.read(data)) != -1) {
                handleMessage(client, data, length);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    public void handleMessage(Socket client, String message) throws IOException {
        log.info("handleMessage: " + message + " " + client);
    }

    @Async
    public void handleMessage(Socket client, byte[] message, int length) throws IOException {
        log.info("handleMessage: " + new String(message, 0, length) + " " + client);
        OutputStream outputStream = client.getOutputStream();
        outputStream.write(message, 0, length);
        outputStream.flush();
    }

    @Async
    public void handleTransportError(Socket client, Throwable exception) throws IOException {
        log.warn("WebSocket connection error. " + exception.getMessage());
    }

    @Async
    public void afterConnectionClosed(Socket client) {
        log.info("WebSocket connection closed. ");
    }
}
