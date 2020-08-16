package com.sherlock.controller;

import com.sherlock.enums.OperatorFriendRequestTypeEnum;
import com.sherlock.enums.SearchFriendsStatusEnum;
import com.sherlock.pojo.Users;
import com.sherlock.pojo.bo.UsersBO;
import com.sherlock.pojo.vo.FriendRequestVO;
import com.sherlock.pojo.vo.MyFriendsVO;
import com.sherlock.pojo.vo.UsersVO;
import com.sherlock.service.UserService;
import com.sherlock.utils.FastDFSClient;
import com.sherlock.utils.FileUtils;
import com.sherlock.utils.JSONResult;
import com.sherlock.utils.MD5Utils;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: sherlock
 * @description:
 * @date: 2020/7/27 16:59
 */
@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @RequestMapping("/hello")
    public String hello() {
        System.out.println("hello fuckman");
        return "heloo";
    }
    @PostMapping("/registOrLogin")
    public JSONResult registOrLogin(@RequestBody Users user) throws Exception {
        System.out.println(user.toString());
        // 0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return JSONResult.errorMsg("用户名或密码不能为空...");
        }

        // 1. 判断用户名是否存在，如果存在就登录，如果不存在则注册
        boolean result = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (result) {
            // 1.1 登录
            userResult = userService.queryUserForLogin(user.getUsername(),
                    MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return JSONResult.errorMsg("用户名或密码不正确...");
            }
        } else {
            // 1.2 注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);

        return JSONResult.ok(userVO);
    }

    @PostMapping("/uploadFaceBase64")
    public JSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {
        //获取从前端传过来的BASE64字符串，然后转化为文件对象上传
        String faceData = usersBO.getFaceData();
        String userFacePath = "/BASE64/" + usersBO.getUserId() + "userFace64.png";
        FileUtils.base64ToFile(userFacePath, faceData);

        //上传文件到FastDFS
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(multipartFile);

        //获取缩略图的url
        String[] arr = url.split("\\.");
        String thumb = "_80x80.";
        String thumbUrl = arr[0] + thumb + arr[1];

        //生成新的user对象
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(thumbUrl);
        user.setFaceImageBig(url);

        //在数据库中更新用户信息
        Users newUser = userService.updateUserInfo(user);
        return JSONResult.ok(newUser);

    }

    /**
     * @Description: 设置用户昵称
     */
    @PostMapping("/setNickname")
    public JSONResult setNickname(@RequestBody UsersBO userBO)  {

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return JSONResult.ok(result);
    }

    /**
     * @Description: 搜索用户接口，根据账号做匹配查询而不是模糊查询
     */
    @PostMapping("/search")
    public JSONResult search(String myUserId, String friendUsername) {
        //判断myUserId和friendUsername是否为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }
        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer result = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (result == SearchFriendsStatusEnum.SUCCESS.status) {
            Users friend = userService.queryUserInfoByUsername(friendUsername);
            UsersVO usersVO = new UsersVO();
            BeanUtils.copyProperties(friend, usersVO);
            return JSONResult.ok(usersVO);
        } else {
            return JSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(result));
        }
    }

    @PostMapping("/addFriendRequest")
    public JSONResult addFriendRequest(String myUserId, String friendUsername) {
        //判断myUserId和friendUsername是否为空
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }
        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer result = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (result == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            return JSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(result));
        }
        return JSONResult.ok();
    }

    /**
     * 查询添加好友的请求
     */
    @PostMapping("/queryFriendRequests")
    public JSONResult queryFriendRequests(String userId) {
        //判断是否为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        // 查询用户接受到的朋友申请
        return JSONResult.ok(userService.queryFriendRequestList(userId));
    }

    /**
     * 操作好友请求
     */
    @PostMapping("/operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) {
        // 1.判断参数不能为空
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId)) {
            return JSONResult.errorMsg("");
        }
        // 2.若operType没有对应的枚举值，则直接抛出空错误信息
        if (OperatorFriendRequestTypeEnum.getMsgByType(operType) == null) {
            return JSONResult.errorMsg("");
        }
        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 3.判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(acceptUserId, sendUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type){
            // 4.判断如果通过好友请求，则互相添加好友，并删除好友请求记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        // 5. 返回好友列表
        List<MyFriendsVO> friends = userService.queryMyFriends(acceptUserId);
        return JSONResult.ok(friends);
    }

    /**
     * @Description: 查询我的好友列表
     */
    @PostMapping("/myFriends")
    public JSONResult myFriends(String userId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(userId);

        return JSONResult.ok(myFirends);
    }

    /**
     * @Description: 用户手机端获取未签收的消息列表
     */
    @PostMapping("/getUnReadMsgList")
    public JSONResult getUnReadMsgList(String acceptUserId) {
        if (StringUtils.isBlank(acceptUserId)) {
            JSONResult.errorMsg("");
        }
        // 查询列表
        List<com.sherlock.pojo.ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);
        return JSONResult.ok(unreadMsgList);
    }

}
