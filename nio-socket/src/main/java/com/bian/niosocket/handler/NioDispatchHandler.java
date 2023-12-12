package com.bian.niosocket.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Async
@Log4j2
@Component
@EnableAsync
public class NioDispatchHandler {

    public void afterConnectionEstablished(int id, SocketChannel socketChannel) {
        log.info("Client is connected; id: " + id);
        try {
            socketChannel.write(ByteBuffer.wrap(String.format("[%d] OK\n", id).getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleMessage(int id, ByteBuffer data, SocketChannel socketChannel) throws IOException {
        log.info("handleMessage id:" + id + "; message: " + new String(data.array(), 0, data.limit()));
        socketChannel.write(ByteBuffer.wrap(String.format("[%d] 发送消息: ", id).getBytes(StandardCharsets.UTF_8)));
        socketChannel.write(data);
    }

    public void handleTransportError() {
        log.warn("WebSocket connection error. ");
    }

    public void afterConnectionClosed() {
        log.info("WebSocket connection closed. ");
    }
}
