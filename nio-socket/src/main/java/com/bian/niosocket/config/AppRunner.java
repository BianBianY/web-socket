package com.bian.niosocket.config;

import com.bian.niosocket.server.NioServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class AppRunner implements ApplicationRunner {

    private final NioServer nioServer;

    public AppRunner(NioServer nioServer) {
        this.nioServer = nioServer;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        nioServer.start();
        log.info("nio server started");
    }
}
