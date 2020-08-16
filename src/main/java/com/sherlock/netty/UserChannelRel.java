package com.sherlock.netty;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author: sherlock
 * @description: 用户id和channel的关联关系处理
 * @date: 2020/8/5 19:42
 */
public class UserChannelRel {

    static Logger logger = LoggerFactory.getLogger(UserChannelRel.class);

    private static HashMap<String, Channel> map = new HashMap();

    public static String put(String senderId, Channel channel) {
        map.put(senderId, channel);
        return senderId;
    }

    public static Channel get(String senderId) {
        return map.get(senderId);
    }

    public static void output() {
        for (HashMap.Entry<String, Channel> entry : map.entrySet()) {
            logger.info("userId: " + entry.getKey() + ","
                                + "channelId: " + entry.getValue().id().asLongText());
        }
    }
}
