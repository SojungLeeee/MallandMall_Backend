package com.exam.user;

public interface UserService {

	// 회원가입
	public void save(UserDTO dto);

	// mypage
	public UserDTO findById(String userId);

	// 로그인 아이디 중복 확인
	public UserDTO findByuserId(String userId);

	public UserDTO findByUserNameAndEmail(String userName, String email);

	//비밀번호재설정
	public boolean resetPassword(String userId, String phoneNumber, String newPassword);
}