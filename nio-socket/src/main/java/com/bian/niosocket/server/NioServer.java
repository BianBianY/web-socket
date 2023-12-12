package com.bian.niosocket.server;

import com.bian.niosocket.handler.NioDispatchHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Log4j2
public class NioServer {
    private static final int TIMEOUT = 500;
    public static final int READ_BUFF_CAPACITY = 1024 * 10;
    @Value("${nio-server.port}")
    private int port = 9999;
    @Value("${nio-server.selector-size}")
    private int selectorSize = 3;
    private final ThreadPoolTaskExecutor executor;
    private final NioDispatchHandler dispatchHandler;
    private final ServerSocketChannel serverSocketChannel;
    private final AtomicInteger channelId = new AtomicInteger(0);
    private final Selector registeredSelector;
    private final List<Selector> selectors = new ArrayList<>();

    public NioServer(ThreadPoolTaskExecutor executor, NioDispatchHandler dispatchHandler) throws IOException {
        this.executor = executor;
        this.dispatchHandler = dispatchHandler;
        registeredSelector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
    }

    public void start() throws IOException {
        start(port);
    }

    public void start(int port) throws IOException {
        if (serverSocketChannel.isRegistered()) {
            log.warn("服务器已运行");
            return;
        }

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(registeredSelector, SelectionKey.OP_ACCEPT);
        asyncStartRegisteredListener();
        for (int i = 0; i < selectorSize; i++) {
            Selector selector = Selector.open();
            selectors.add(selector);
            asyncStartReadListener(selector);
        }
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public boolean sendDataToAll(ByteBuffer byteBuffer) throws ExecutionException, InterruptedException {
        List<Future<Boolean>> result = new ArrayList<>();
        for (Selector selector : selectors) {
            result.add(asyncSend(byteBuffer, selector));
        }
        for (Future<Boolean> future : result) {
            if (!future.get()) {
                return false;
            }
        }
        return true;
    }

    private void asyncStartRegisteredListener() {
        executor.execute(() -> {
            log.info("注册器:" + registeredSelector + "监听成功");
            while (true) {
                try {
                    startRegisteredListener();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void asyncStartReadListener(Selector selector) {
        executor.execute(() -> {
            log.info("选择器:" + selector + "监听成功");
            while (true) {
                try {
                    startReadListener(selector);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startRegisteredListener() throws IOException {
        if (registeredSelector.select(TIMEOUT) == 0) {
            return;
        }
        Set<SelectionKey> selectionKeys = registeredSelector.selectedKeys();
        for (SelectionKey selectionKey : selectionKeys) {
            if (selectionKey.isAcceptable()) {
                log.info("客户端连接。。。");
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                SelectionKey register = socketChannel.register(getMinSelector(), SelectionKey.OP_READ, channelId.incrementAndGet());
                dispatchHandler.afterConnectionEstablished((int) register.attachment(), socketChannel);
            }
        }
        selectionKeys.clear();
    }


    private void startReadListener(Selector selector) throws IOException {
        if (selector.select(TIMEOUT) == 0) {
            return;
        }
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (SelectionKey selectionKey : selectionKeys) {
            try {
                if (selectionKey.isReadable()) {
                    log.info(String.format("客户端[%d]发送消息", (int) selectionKey.attachment()));
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer data = ByteBuffer.allocate(READ_BUFF_CAPACITY);
                    while (true) {
                        int length = channel.read(data);
                        if (length == -1) {
                            throw new IOException("客户端关闭");
                        }
                        if (data.hasRemaining()) {
                            break;
                        }
                        data = ByteBuffer.allocate(data.capacity() * 2).put(data.array());
                    }
                    dispatchHandler.handleMessage((int) selectionKey.attachment(), data.flip(), channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
                selectionKey.channel().close();
                selectionKey.cancel();
            }
        }
        selectionKeys.clear();
    }

    private synchronized Selector getMinSelector() {
        return selectors.stream().min(Comparator.comparingInt(o -> o.keys().size())).orElse(selectors.get(0));
    }

    private Future<Boolean> asyncSend(ByteBuffer byteBuffer, Selector selector) {
        return executor.submit(() -> {
            try {
                for (SelectionKey key : selector.keys()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    try {
                        channel.write(ByteBuffer.wrap(byteBuffer.array(), 0, byteBuffer.limit()));
                    } catch (IOException e) {
                        channel.close();
                        key.cancel();
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
