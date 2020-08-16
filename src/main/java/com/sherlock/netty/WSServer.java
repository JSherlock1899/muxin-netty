package com.sherlock.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/26 20:05
 */
@Component
public class WSServer {

    private static class SingletonWSServer{
        private static final WSServer INSTANCE = new WSServer();
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap bootstrap = new ServerBootstrap();
    private ChannelFuture future;

    public static WSServer getInstance() {
        return SingletonWSServer.INSTANCE;
    }

    public WSServer() {
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        bootstrap.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());
    }

    public void start() {
        this.future = bootstrap.bind(8888);
        System.err.println("netty websocket server 启动完毕...");
    }
}
