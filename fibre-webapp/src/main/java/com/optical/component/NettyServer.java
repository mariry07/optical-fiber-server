package com.optical.component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gjing
 * <p>
 * 服务启动监听器
 **/
@Slf4j
public class NettyServer {
    private final BlockingQueue<String> list;
    //队列大小，先这么丑陋的写一下
    private final int maxSize;
    public NettyServer(BlockingQueue list, int maxSize) {
        this.list = list;
        this.maxSize = maxSize;
    }

    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();
    private static Map<String, String> messageMap = new ConcurrentHashMap<String, String>();
//    private static Map<String, byte[]> messageMap = new ConcurrentHashMap<String, byte[]>();

    public static Map<String, Channel> getMap() {
        return map;
    }

    public static void setMap(Map<String, Channel> map) {
        NettyServer.map = map;
    }

    public static Map<String, String> getMessageMap() {
        return messageMap;
    }

    public static void setMessageMap(Map<String, String> messageMap) {
        NettyServer.messageMap = messageMap;
    }

    public void start(InetSocketAddress socketAddress) {
        //new 一个主线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //new 一个工作线程组
        EventLoopGroup workGroup = new NioEventLoopGroup(200);
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerChannelInitializer(list, maxSize))
                .localAddress(socketAddress)
                //设置队列大小
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 两小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        //绑定端口,开始接收进来的连接
        try {
            ChannelFuture future = bootstrap.bind(socketAddress).sync();
            log.info("服务器启动开始监听端口: {}", socketAddress.getPort());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭主线程组
            bossGroup.shutdownGracefully();
            //关闭工作线程组
            workGroup.shutdownGracefully();
        }
    }
}
