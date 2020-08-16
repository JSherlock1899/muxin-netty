package com.sherlock;

import com.sherlock.netty.WSServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/26 20:11
 */
@Component
public class NettyBooter implements ApplicationListener<ContextRefreshedEvent> {

    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            WSServer.getInstance().start();
        }
    }
}
