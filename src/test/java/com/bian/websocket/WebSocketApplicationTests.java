package com.bian.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.Socket;

@SpringBootTest
class WebSocketApplicationTests {

    @Test
    void contextLoads() throws IOException {
        Socket socket = new Socket("127.0.0.1", 112);
    }

}
