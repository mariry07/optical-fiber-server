package com.optical.component;

import ch.qos.logback.core.net.server.ServerRunner;
import com.optical.Service.impl.OpticalServiceImpl;
import com.optical.bean.SocketProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariry on 2019/7/16.
 */

public class SocketRunner  {

    private static final Logger log = LoggerFactory.getLogger(SocketRunner.class);

    @Autowired
    private SocketProperties properties;
    @Autowired
    private OpticalServiceImpl opticalService;

    public void runrun(){
        try{
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
                pool.execute(new ServerConfig(socket, opticalService));
            }
        }catch (Exception e) {
            log.error("error! e: {}", e);
        }

    }
}
