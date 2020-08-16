package com.sherlock.push;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.AppMessage;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.style.Style0;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: sherlock
 * @description: 消息推送
 * @date: 2020/8/7 16:54
 */
public class MsgPush {

    private static final String appId = "u0PZ5VYgPWA8hFhIxZejY6";
    private static final String appkey = "IQnojrHBb27gQLV9UZsejA";
    private static final String mastersecret = "heGkytnbPs8eiI4OCR1uD7";

    public static void main(String[] args) {
        send("好友请求1", "有一个新朋友请求添加您为好友1", "e1366ad6ffaa9fd2e6c5d29e41e06e4f");
    }
    public static void send(String title, String text, String cid) {
        IGtPush push = new IGtPush(appkey, mastersecret);

        // 定义"点击链接打开通知模板"，并设置标题、内容、链接
        NotificationTemplate template = notificationTemplateDemo(title, text, cid);

        List<String> appIds = new ArrayList<String>();
        appIds.add(appId);

        // 定义"AppMessage"类型消息对象，设置消息内容模板、发送的目标App列表、是否支持离线发送、以及离线消息有效期(单位毫秒)
        AppMessage message = new AppMessage();
        message.setData(template);
        message.setAppIdList(appIds);
        message.setOffline(true);
        message.setOfflineExpireTime(1000 * 600);

        IPushResult ret = push.pushMessageToApp(message);
        System.out.println(ret.getResponse().toString());
    }



    public static NotificationTemplate notificationTemplateDemo(String title, String text, String cid) {
        NotificationTemplate template = new NotificationTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appkey);
        // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
        template.setTransmissionType(1);
        template.setTransmissionContent(title);
        Style0 style = new Style0();
        // 设置通知栏标题与内容
        style.setTitle(title);
        style.setText(text);
        // 配置通知栏图标
//        style.setLogo("icon.png");
        // 配置通知栏网络图标
        style.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        style.setRing(true);
        style.setVibrate(true);
        style.setClearable(true);
        template.setStyle(style);
        return template;
    }
}
