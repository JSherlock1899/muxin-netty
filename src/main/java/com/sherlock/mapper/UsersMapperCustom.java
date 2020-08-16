package com.sherlock.mapper;


import com.sherlock.pojo.Users;
import com.sherlock.pojo.vo.FriendRequestVO;
import com.sherlock.pojo.vo.MyFriendsVO;
import com.sherlock.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {
	
	List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	List<MyFriendsVO> queryMyFriends(String userId);
	
	void batchUpdateMsgSigned(List<String> msgIdList);
	
}