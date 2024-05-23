package com.bian.websocket.niosocket.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Socket> sockets = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            TimeUnit.MICROSECONDS.sleep(500);
            Socket socket = new Socket("127.0.0.1", 9999);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("你好呀".getBytes());
            sockets.add(socket);
        }
        while (true) {
            TimeUnit.MICROSECONDS.sleep(500);
        }
    }
}
