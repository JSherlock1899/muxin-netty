package com.sherlock.netty;

import java.io.Serializable;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/8/5 19:13
 */
public class ChatMsg implements Serializable {

    private static final long serialVersionUID = 3611169682695799175L;

    private String senderId; //发送者Id
    private String receiverId; //接受者Id
    private String msg; //消息内容
    private String msgId; //用于消息的签收


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public String toString() {
        return "ChatMsg{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", msg='" + msg + '\'' +
                ", msgId='" + msgId + '\'' +
                '}';
    }
}
