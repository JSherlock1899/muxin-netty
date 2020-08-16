package com.sherlock.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/26 15:46
 */
public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        //websocket基于http协议，所以要有编解码器
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        //大数据流的处理
        pipeline.addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
        //对HTTPMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
        pipeline.addLast("HttpObjectAggregator", new HttpObjectAggregator(64 * 1024));
        // ====================== 以上是用于支持http协议    ======================
        //======================= 以下用来进行心跳检测 =========================
        pipeline.addLast("IdleStateHandler", new IdleStateHandler(60, 60, 120));
        pipeline.addLast("HeartBeatHandle", new HeartBeatHandle());
        // ====================== 以下是支持httpWebsocket ======================
        /**
         * websocket服务器处理的协议，用于指定客户端连接的路由
         * 会处理握手和心跳等
         * 对应websocket来讲，都是以frames来传输的，不同数据类型对应的frames也不一样
         */
        pipeline.addLast("", new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast("ChatHandler", new ChatHandler());

    }
}
