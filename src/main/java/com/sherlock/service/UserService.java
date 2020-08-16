package com.sherlock.service;

import com.sherlock.netty.ChatMsg;
import com.sherlock.pojo.Users;
import com.sherlock.pojo.vo.FriendRequestVO;
import com.sherlock.pojo.vo.MyFriendsVO;

import java.io.IOException;
import java.util.List;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/27 17:04
 */
public interface UserService {

    /**
     * @Description: 判断用户名是否存在
     */
    boolean queryUsernameIsExist(String username);

    /**
     * @Description: 查询用户是否存在
     */
    Users queryUserForLogin(String username, String pwd);

    /**
     * @Description: 用户注册
     */
    Users saveUser(Users user) throws IOException;

    /**
     * @Description: 更新用户信息
     */
    Users updateUserInfo(Users users);

    /**
     * @Description: 搜索用户的前置条件判断
     */
    Integer preconditionSearchFriends(String myUserId, String friendUsername);

    /**
     * @Description: 根据用户名查询用户信息
     */
    Users queryUserInfoByUsername(String username);

    /**
     * @Description: 添加好友请求记录，保存到数据库
     */
    void sendFriendRequest(String myUserId, String friendUsername);

    /**
     @Description: 查询好友请求
     */
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    /**
     * @Description: 删除好友请求记录
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * @Description: 通过好友请求
     * 				1. 保存好友
     * 				2. 逆向保存好友
     * 				3. 删除好友请求记录
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     @Description: 查询好友列表
     */
    List<MyFriendsVO> queryMyFriends(String myUserId);

    /**
     @Description: 保存消息记录
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * @Description: 批量签收消息
     */
    void updateMsgSigned(List<String> msgIdList);

    /**
     * @Description: 获取未签收的消息列表
     */
    List<com.sherlock.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
