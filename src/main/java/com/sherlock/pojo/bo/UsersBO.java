package com.sherlock.pojo.bo;

public class UsersBO {
    private String userId;
    private String faceData;
    private String nickname;
    
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getFaceData() {
		return faceData;
	}
	public void setFaceData(String faceData) {
		this.faceData = faceData;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Override
	public String toString() {
		return "UsersBO{" +
				"userId='" + userId + '\'' +
				", faceData='" + faceData + '\'' +
				", nickname='" + nickname + '\'' +
				'}';
	}
}