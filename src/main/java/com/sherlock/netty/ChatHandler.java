package com.sherlock.netty;

import com.sherlock.SpringUtil;
import com.sherlock.enums.MsgActionEnum;
import com.sherlock.service.UserService;
import com.sherlock.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/26 15:49
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    Logger logger = LoggerFactory.getLogger(ChatHandler.class);
    //用来记录和管理所有客户端的channel
    private static ChannelGroup user = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        //客户端传过来的信息
//        String content = msg.text();
//        System.out.println("接收到的消息为：" + content);
//        user.writeAndFlush(new TextWebSocketFrame("服务器收到消息的时间：" + LocalDateTime.now()
//        + "，收到的消息为： " + content));

        // 1. 接受客户端穿过来的消息，并转化成实体类
        String content = msg.text();
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        //获取当前信道
        Channel currentChannel = ctx.channel();
        // 2.判断消息类型，根据不同的消息类型来处理不同的业务
        Integer action = dataContent.getAction();
        if (action == MsgActionEnum.CONNECT.type) {
            // 2.1 当websocket第一次open，初始化channel，并把channel和userId关联起来
            // 初始化channel已经在handlerAdded中完成
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);

            //测试
            for (Channel channel : user) {
                logger.info(channel.id().asLongText());
            }
            UserChannelRel.output();
        } else if (action == MsgActionEnum.CHAT.type){
            // 2.2 若是聊天类型的消息，则把聊天记录保存到数据库，并标记消息的签收状态为未签收
            //获取消息实体类
            ChatMsg chatMsg = dataContent.getChatMsg();
            //从Spring容器中获取userService
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();

            //发送消息
            //从全局用户Channel关系中获取接收方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null) {
                //若receiverChannel为空，则代表用户离线，推送消息
                logger.info("用户离线，推送消息");
            } else {
                //在ChannelGroup中
                // 当receiverChannel不为空的时候，从ChannelGroup去查找对应的channel是否存在
                Channel findChannel = user.find(receiverChannel.id());
                if (findChannel != null) {
                    //用户在线，发送消息
                    logger.info("用户在线，发送消息");
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
                } else {
                    //用户离线，推送消息
                    logger.info("用户离线，推送消息");
                }

            }
        } else if (action == MsgActionEnum.SIGNED.type) {
            // 2.3 若是签收消息类型，针对具体的消息进行签收，并修改数据库中对应的消息签收状态为已签收
            //此处的签收不是用户行为，不是指用户的已读未读，而是指消息已经发送到了对方的手机上
            //扩展字段在signed类型的消息中，代表需要去签收的消息id，用逗号间隔
            String extend = dataContent.getExtend();
            logger.info("extend = " + extend);
            String[] msgIds = extend.split(",");
            //将这些消息id进行去空处理,添加到List中
            List<String> msgIdList = new ArrayList<>();
            for (String msgId : msgIds) {
                if (StringUtils.isNotBlank(msgId)) {
                    msgIdList.add(msgId);
                }
            }
            logger.info("签收消息");
            Stream.of(msgIdList).forEach(System.out::println);
            //获取userService
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                userService.updateMsgSigned(msgIdList);
            }
        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            // 2.4 心跳消息
            System.out.println("收到来自channel为[" + currentChannel + "]的心跳包...");
        }
    }

    /**
     * 客户端连接服务端后，获取channle并放到channelGroup中管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        user.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除，channelId为：" + channelId);
        //当触发handlerRemoved时，ChannelGroup会自动移除对应客户端的channel，此处只是为了代码的优雅
        user.remove(ctx.channel());
//        System.out.println("客户端断开，channle对应的长id为："
//                + ctx.channel().id().asLongText());
//        System.out.println("客户端断开，channle对应的短id为："
//                + ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生异常之后关闭连接（关闭channel），随后从channelGroup中移除
        ctx.channel().close();
        user.remove(ctx.channel());
    }
}
