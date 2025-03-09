package com.exam.user;

public interface UserService {

	// 회원가입
	public void save(UserDTO dto);

	// mypage
	public UserDTO findById(String userid);

	// 로그인
	public UserDTO findByUserid(String userid);
}
