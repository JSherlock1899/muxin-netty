package com.sherlock.push;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/8/7 17:10
 */
@Component
public class AsyncCenter {

    @Async
    public void sendPush(String title, String text, String cid) {
        MsgPush.send(title, text, cid);
    }
}
