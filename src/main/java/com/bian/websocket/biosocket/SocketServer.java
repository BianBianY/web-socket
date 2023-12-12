package com.bian.websocket.biosocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class SocketServer {
    public static final int PORT = 112;
    private static ServerSocket SERVER;
    private final SocketServerHandler serverHandler;
    private static final Map<String, Socket> CLIENTS = new ConcurrentHashMap<>();

    public SocketServer(SocketServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public void startServer() throws IOException {
        SERVER = new ServerSocket(PORT);
        log.info("socket server start");
    }

    @Async
    public void startClientConnectionListener() {
        log.info("start Client Connection Listener");
        while (true) {
            try {
                Socket client = SERVER.accept();
                CLIENTS.put(String.format("%s:%d", client.getInetAddress().toString(), client.getPort()), client);
                serverHandler.afterConnectionEstablished(client);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Async
    public void startClientMessageListener() throws IOException {
        log.info("start Client Message Listener");
        while (true) {
            for (Socket client : CLIENTS.values()) {
                InputStream inputStream = client.getInputStream();
                int length;
                byte[] data = new byte[1024];
                if ((length = inputStream.read(data)) != -1) {
                    serverHandler.handleMessage(client, data, length);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            //Java为非阻塞设置的类
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    //表示没人连接
                    System.out.println("正在等待客户端请求连接...");
                } else {
                    System.out.println("当前接收到客户端请求连接...");
                }
                if (socketChannel != null) {
                    //设置为非阻塞
                    socketChannel.configureBlocking(false);
                    byteBuffer.flip();
                    //切换模式 写-->读
                    int effective = socketChannel.read(byteBuffer);
                    if (effective != 0) {
                        String content = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                        System.out.println(content);
                    } else {
                        System.out.println("当前未收到客户端消息");
                    }
                }
            }
        } catch (IOException e) {
            //TODO Auto -generated catch blocke.printStackTrace();
        }
    }

    public static void main1(String[] args) throws InterruptedException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        List<SocketChannel> socketList = new ArrayList<>();
        try {
            //Java为非阻塞设置的类
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    Thread.sleep(5000);
                } else {
                    System.out.println("当前接收到客户端请求连接...");
                    socketList.add(socketChannel);
                }
                for (SocketChannel socket : socketList) {
                    socket.configureBlocking(false);
                    int effective = socket.read(byteBuffer);
                    if (effective != 0) {
                        byteBuffer.flip();
                        //切换模式 写-->读
                        String content = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                        System.out.println("接收到消息:" + content);
                        byteBuffer.clear();
                    } else {
                        System.out.println("当前未收到客户端消息");
                    }
                }
            }
        } catch (IOException e) {
            //TODO Auto -generated catch blocke.printStackTrace();
        }
    }
}
