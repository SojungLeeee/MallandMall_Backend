package com.exam.mypage;


import com.exam.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MypageRepository extends JpaRepository<User, String> {
	//  userid로 회원 정보 조회
	Optional<User> findByUserid(String userid);
}
