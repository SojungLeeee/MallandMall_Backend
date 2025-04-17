package com.exam.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.exam.user.UserDTO;
import com.exam.user.UserService;

import lombok.extern.slf4j.Slf4j;

//용도: 입력된 id와 pw 이용해서 DB와 연동해서 검증작업후 최종적으로  token 반환해주는 역할.

@Component
@Slf4j
public class JwtTokenProvider {

	UserService userService;
	JwtTokenService tokenService;

	public JwtTokenProvider(UserService userService, JwtTokenService tokenService) {
		this.userService = userService;
		this.tokenService = tokenService;
	}

	public JwtTokenResponse authenticate(Map<String, String> map) {
		String encodedtoken = null;
		String userId = map.get("userId");
		String password = map.get("password");

		UserDTO dto = userService.findById(userId);

		// 사용자가 존재하지 않는 경우
		if (dto == null) {
			return null; // 또는 예외 발생
		}

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		UsernamePasswordAuthenticationToken token = null;

		log.info("저장된 비번: {}", dto.getPassword());
		log.info("입력된 비번: {}", password);
		log.info("일치 여부: {}", passwordEncoder.matches(password, dto.getPassword()));
		if (passwordEncoder.matches(password, dto.getPassword())) {
			// 비밀번호가 일치할 때만 토큰 생성 및 응답 반환
			UserDTO new_dto = new UserDTO();
			new_dto.setUserId(userId);
			new_dto.setNewPassword(password);
			new_dto.setUserName(dto.getUserName());
			new_dto.setRole(dto.getRole());

			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(dto.getRole().toString()));

			token = new UsernamePasswordAuthenticationToken(userId, null, authorities);
			encodedtoken = tokenService.generateToken(token);

			return new JwtTokenResponse(encodedtoken, userId, dto.getRole().toString());
		} else {
			// 비밀번호가 일치하지 않는 경우
			return null; // 또는 예외 발생
		}
	}

	// 기존 JWT 방식은 평문화된 비밀번호를 사용, 소셜로그인에서는 암호화된 비밀번호를 반환하기 소셜 로그인용 메서드 제작
	public JwtTokenResponse authenticateWithSocial(String userId) {
		UserDTO dto = userService.findById(userId);

		// 사용자가 존재하지 않는 경우
		if (dto == null) {
			return null;
		}

		String encodedtoken = null;

		// 비밀번호 검증 없이 토큰 생성
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(dto.getRole().toString()));

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(userId, null, authorities);
		encodedtoken = tokenService.generateToken(token);

		return new JwtTokenResponse(encodedtoken, userId, dto.getRole().toString());
	}
}
