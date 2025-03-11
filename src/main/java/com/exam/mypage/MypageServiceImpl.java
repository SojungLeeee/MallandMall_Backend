package com.exam.mypage;

import com.exam.user.User;

import org.springframework.stereotype.Service;

@Service
public class MypageServiceImpl implements MypageService {

	private final MypageRepository mypageRepository;

	public MypageServiceImpl(MypageRepository mypageRepository) {
		this.mypageRepository = mypageRepository;
	}

	@Override
	public MypageDTO getMypage(String userid) {

		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			return null;
		}
		return convertToDTO(user);
	}

	@Override
	public void updateMypage(String userid, MypageDTO dto) {
		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		user.setUsername(dto.getUsername());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setEmail(dto.getEmail());
		user.setPost(dto.getPost());
		user.setAddr1(dto.getAddr1());
		user.setAddr2(dto.getAddr2());

		mypageRepository.save(user);
	}

	@Override
	public void deleteMypage(String userid) {
		User user = mypageRepository.findByUserid(userid).orElse(null);
		if (user == null) {
			throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
		}

		mypageRepository.deleteById(userid);
	}

	private MypageDTO convertToDTO(User User) {
		return MypageDTO.builder()
			.userid(User.getUserid())
			.username(User.getUsername())
			.post(User.getPost())
			.addr1(User.getAddr1())
			.addr2(User.getAddr2())
			.phoneNumber(User.getPhoneNumber())
			.email(User.getEmail())
			.role(User.getRole())
			.build();
	}
}
