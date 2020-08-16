package com.sherlock.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author: sherlock
 * @description: 用于心跳检测的handle
 * @date: 2020/8/6 19:12
 */
public class HeartBeatHandle extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判断evt是否是IdleStateEvent，用于触发用户事件（包含读空闲、写空闲、读写空闲）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("进入读空闲");
            }else if (event.state() == IdleState.WRITER_IDLE) {
                System.out.println("进入写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {
                System.out.println("进入读写空闲");
                ctx.channel().close();
            }
        }
    }
}
