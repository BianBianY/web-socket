package com.bian.websocket.controller;

import com.bian.websocket.context.ClientContext;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Controller
public class WebSocketController {
    private final ClientContext clientContext;
    public final ApplicationContext applicationContext;

    public WebSocketController(ApplicationContext applicationContext) {
        this.clientContext = ClientContext.getClientContext();
        this.applicationContext = applicationContext;
    }

    @RequestMapping("/achieve-1")
    public String reverse() {
        return "websocket/achieve-1";
    }

    @SneakyThrows
    @RequestMapping("/send/{id}/{message}")
    @ResponseBody
    public String sendMessage(@PathVariable("id") String id, @PathVariable("message") String message) {
        if (clientContext.sendMessageByClientId(id, message)) {
            return "ok";
        }
        return "error";
    }

    @SneakyThrows
    @RequestMapping("/send/all/{message}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessageForAll(@PathVariable("message") String message) {
        Map<String, Object> result = new HashMap<>(2);
        int success = 0, failure = 0;
        List<Future<Boolean>> futures = clientContext.sendMessageForAll(message);
        for (var future : futures) {
            if (future.get()) {
                success++;
            } else {
                failure++;
            }
        }
        result.put("status", failure == 0 ? "Ok" : "Error");
        result.put("success", success);
        result.put("failure", failure);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/online")
    @ResponseBody
    public int getOnline() {
        return clientContext.getOnlineSize();
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greeting")
    public String greeting(String greeting) {
        return "Hello," + greeting + "!";
    }
}
