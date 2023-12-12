package com.bian.niosocket.controller;

import com.bian.niosocket.server.NioServer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@RestController
public class NioController {

    private final NioServer server;

    public NioController(NioServer server) {
        this.server = server;
    }

    @RequestMapping("/send/all/{message}")
    public String sendMessageForAllConnect(@PathVariable String message) throws ExecutionException, InterruptedException {
        return server.sendDataToAll(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8))) ? "OK" : "ERROR";
    }
}
