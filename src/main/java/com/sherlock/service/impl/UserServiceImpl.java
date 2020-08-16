package com.sherlock.service.impl;

import com.sherlock.enums.MsgActionEnum;
import com.sherlock.enums.MsgSignFlagEnum;
import com.sherlock.enums.SearchFriendsStatusEnum;
import com.sherlock.mapper.*;
import com.sherlock.netty.ChatMsg;
import com.sherlock.netty.DataContent;
import com.sherlock.netty.UserChannelRel;
import com.sherlock.pojo.FriendsRequest;
import com.sherlock.pojo.MyFriends;
import com.sherlock.pojo.Users;
import com.sherlock.pojo.vo.FriendRequestVO;
import com.sherlock.pojo.vo.MyFriendsVO;
import com.sherlock.push.AsyncCenter;
import com.sherlock.service.UserService;
import com.sherlock.utils.FastDFSClient;
import com.sherlock.utils.FileUtils;
import com.sherlock.utils.JsonUtils;
import com.sherlock.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/27 17:05
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private AsyncCenter asyncCenter;


    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean queryUsernameIsExist(String username) {
        Users users = new Users();
        users.setUsername(username);
        Users result = usersMapper.selectOne(users);
        return result != null ? true : false;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserForLogin(String username, String pwd) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);
        Users result = usersMapper.selectOneByExample(example);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Users saveUser(Users user) throws IOException {
        String userId = sid.nextShort();
        //为某个用户生成一个唯一的二维码
        String qrCodePath = "/qrcode/" + userId + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "muxin_qrcode:" + user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = fastDFSClient.uploadQRCode(qrcodeFile);
        user.setQrcode(qrCodeUrl);
        user.setId(userId);
        usersMapper.insert(user);
        return user;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Users updateUserInfo(Users users) {
        usersMapper.updateByPrimaryKeySelective(users);
        return selectUserInfo(users.getId());
    }

    /**
     * @Description查询用户信息
     * @param userId
     * @return
     */
    private Users selectUserInfo(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        //1. 搜索的用户若不存在，则返回无此用户...
        Users user = queryUserInfoByUsername(friendUsername);
        if (user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2. 搜索的用户若是自己，则返回不能添加你自己...
        if (myUserId.equalsIgnoreCase(user.getId())) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3. 若搜索的用户已经与自己的好友，则返回该用户已经是你的好友...
        Example example = new Example(MyFriends.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId", myUserId);
        criteria.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriends = myFriendsMapper.selectOneByExample(example);
        if (myFriends != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        return usersMapper.selectOneByExample(example);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {
        //通过用户名查询出待添加好友的用户信息
        Users user = queryUserInfoByUsername(friendUsername);
        //查询好友请求数据表
        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId", myUserId);
        criteria.andEqualTo("acceptUserId", user.getId());
        FriendsRequest request = friendsRequestMapper.selectOneByExample(example);
        //若不存在好友记录，则新增好友请求记录
        if (request == null) {
            FriendsRequest friendsRequest = new FriendsRequest();
            friendsRequest.setId(sid.nextShort());
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(user.getId());
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }
        //异步发送好友请求的推送通知
        asyncCenter.sendPush("好友请求", "有一个新朋友请求添加您为好友", user.getCid());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId", sendUserId);
        criteria.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(example);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        // 1.保存好友
        saveFriends(acceptUserId, sendUserId);
        // 2. 逆向保存好友
        saveFriends(sendUserId, acceptUserId);
        // 3. 删除好友记录
        deleteFriendRequest(sendUserId, acceptUserId);
        // 4.使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
        Channel channel = UserChannelRel.get(sendUserId);
        if (channel != null) {
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFriends(String sendUserId, String acceptUserId) {
        MyFriends myFriends = new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(acceptUserId);
        myFriends.setMyFriendUserId(sendUserId);
        myFriendsMapper.insert(myFriends);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriends(String myUserId) {
        List<MyFriendsVO> myFirends = usersMapperCustom.queryMyFriends(myUserId);
        return myFirends;
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.sherlock.pojo.ChatMsg msg = new com.sherlock.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msg.setId(msgId);
        msg.setSendUserId(chatMsg.getSenderId());
        msg.setAcceptUserId(chatMsg.getReceiverId());
        msg.setMsg(chatMsg.getMsg());
        msg.setCreateTime(new Date());
        msg.setSignFlag(MsgSignFlagEnum.unsign.type);
        chatMsgMapper.insert(msg);
        return msgId;

    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public List<com.sherlock.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        Example example = new Example(com.sherlock.pojo.ChatMsg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("signFlag", 0);
        criteria.andEqualTo("acceptUserId", acceptUserId);
        List<com.sherlock.pojo.ChatMsg> result = chatMsgMapper.selectByExample(example);
        return result;
    }


}
