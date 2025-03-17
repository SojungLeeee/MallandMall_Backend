package com.exam.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	UserService userService;
	UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public void save(UserDTO dto) {
		// UserDTO -> User 변환
		User user = User.builder()
			.userId(dto.getUserId())
			.password(dto.getPassword())
			.userName(dto.getUserName())
			.post(dto.getPost())
			.addr1(dto.getAddr1())
			.addr2(dto.getAddr2())
			.phoneNumber(dto.getPhoneNumber())
			.email(dto.getEmail())
			.role(dto.getRole())  // Role Enum을 바로 사용
			.build();

		userRepository.save(user);
	}

	@Override
	public UserDTO findById(String userId) {
		User user = userRepository.findById(userId).orElse(null);
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	// 로그인 기능
	@Override
	public UserDTO findByuserId(String userId) {
		User user = userRepository.findByuserIdAndPassword(userId, ""); // 더미 비밀번호
		if (user == null)
			return null;

		return convertToDTO(user);
	}

	// Entity → DTO 변환 메서드
	private UserDTO convertToDTO(User user) {
		return UserDTO.builder()
			.userId(user.getUserId())
			.password(user.getPassword()) // 비밀번호도 함께 반환
			.userName(user.getUserName())
			.post(user.getPost())
			.addr1(user.getAddr1())
			.addr2(user.getAddr2())
			.phoneNumber(user.getPhoneNumber())
			.email(user.getEmail())
			.role(user.getRole())  // Role Enum 값을 그대로 사용
			.build();
	}

	@Override
	public UserDTO findByUserNameAndEmail(String userName, String email) {
		User user = userRepository.findByUserNameAndEmail(userName, email);
		if (user == null) {
			throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
		}
		return UserDTO.builder()
			.userId(user.getUserId())
			.createDate(user.getCreateDate())  // createDate 포함
			.build();
	}

	@Override
	@Transactional
	public boolean resetPassword(String userId, String phoneNumber, String newPassword) {
		User user = userRepository.findByuserIdAndPhoneNumber(userId, phoneNumber);
		if (user != null) {
			String encodedPassword = new BCryptPasswordEncoder().encode(newPassword);
			user.setPassword(encodedPassword); // 비밀번호 변경
			return true; // 변경 성공
		}

		return false; // 일치하는 정보가 없으면 false 반환
	}
}
