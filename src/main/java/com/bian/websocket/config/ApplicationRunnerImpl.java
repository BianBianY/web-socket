package com.bian.websocket.config;

import com.bian.websocket.biosocket.SocketServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//@Component
@Log4j2
public class ApplicationRunnerImpl implements ApplicationRunner {
    private final SocketServer server;

    public ApplicationRunnerImpl(SocketServer server) {
        this.server = server;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.warn("init something......");
        server.startServer();
        server.startClientConnectionListener();
        //server.startClientMessageListener();
        List<Socket> sockets = new ArrayList<>();
        for (int i = 0; i < 999; i++) {
            Socket socket = new Socket("127.0.0.1", 112);
            sockets.add(socket);
        }
        while (true) {
            for (Socket socket : sockets) {
                OutputStream outputStream = socket.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write("hello");
                writer.flush();
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
