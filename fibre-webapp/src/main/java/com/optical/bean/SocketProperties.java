package com.optical.bean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * socket配置类
 * Created by mariry on 2019/7/16.
 */
@Component
@Configuration
public class SocketProperties {

    @Value("${socket.port}")
    private Integer port;
    @Value("${socket.pool-keep}")
    private Integer poolKeep;
    @Value("${socket.pool-core}")
    private Integer poolCore;
    @Value("${socket.pool-max}")
    private Integer poolMax;
    @Value("${socket.pool-queue-init}")
    private Integer poolQueueInit;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPoolKeep() {
        return poolKeep;
    }

    public void setPoolKeep(Integer poolKeep) {
        this.poolKeep = poolKeep;
    }

    public Integer getPoolCore() {
        return poolCore;
    }

    public void setPoolCore(Integer poolCore) {
        this.poolCore = poolCore;
    }

    public Integer getPoolMax() {
        return poolMax;
    }

    public void setPoolMax(Integer poolMax) {
        this.poolMax = poolMax;
    }

    public Integer getPoolQueueInit() {
        return poolQueueInit;
    }

    public void setPoolQueueInit(Integer poolQueueInit) {
        this.poolQueueInit = poolQueueInit;
    }
}
