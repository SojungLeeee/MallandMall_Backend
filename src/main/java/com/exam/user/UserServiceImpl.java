package com.exam.user;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	//repository 생성자주입
	UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void save(UserDTO dto) {
		// MemberDTO --> Member 로 변환하는 작업 필요
		User user = User.builder()
			.userid(dto.getUserid())
			.passwd(dto.getPasswd())
			.username(dto.getUsername())
			.post(dto.getPost())
			.addr1(dto.getAddr1())
			.addr2(dto.getAddr2())
			.phoneNumber(dto.getPhoneNumber())
			.email(dto.getEmail())
			.role(dto.getRole())
			.build();

		userRepository.save(user);
	}

	@Override
	public UserDTO findById(String userid) {
		User user = userRepository.findById(userid).orElse(null);
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	//  로그인 기능
	@Override
	public UserDTO findByUserid(String userid) {
		User user = userRepository.findByUseridAndPasswd(userid, ""); // 더미 비밀번호
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	//  Entity → DTO 변환 메서드
	private UserDTO convertToDTO(User user) {
		return UserDTO.builder()
			.userid(user.getUserid())
			.passwd(user.getPasswd()) // 비밀번호도 함께 반환
			.username(user.getUsername())
			.post(user.getPost())
			.addr1(user.getAddr1())
			.addr2(user.getAddr2())
			.phoneNumber(user.getPhoneNumber())
			.email(user.getEmail())
			.role(user.getRole())
			.build();
	}
}