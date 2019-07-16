package com.optical.component;

import ch.qos.logback.core.net.server.ServerRunner;
import com.optical.bean.SocketProperties;
import com.sun.xml.internal.ws.api.policy.PolicyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariry on 2019/7/16.
 */
@Component
public class SocketRunner {
    //public class SocketRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SocketRunner.class);

    @Autowired
    private SocketProperties properties;

    //@Override
    public void run(String... strings) throws Exception {
        ServerSocket server = null;
        Socket socket = null;
        server = new ServerSocket(properties.getPort());
        log.info("you know what? there is a socket server started! listening on port:{}. "
                , properties.getPort());

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                properties.getPoolCore(),
                properties.getPoolMax(),
                properties.getPoolKeep(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(properties.getPoolQueueInit()),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        while(true) {
            socket = server.accept();
            pool.execute(new ServerConfig(socket));
        }



    }
}
