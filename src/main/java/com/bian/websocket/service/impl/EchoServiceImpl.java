package com.bian.websocket.service.impl;

import com.bian.websocket.service.EchoService;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
public class EchoServiceImpl implements EchoService {
    @Override
    public String echo(String message) {
        return MessageFormat.format("Did you say \"{0}\"?", message);
    }
}
