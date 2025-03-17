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

//용도: 입력된 id와 pw 이용해서 DB와 연동해서 검증작업후 최종적으로  token 반환해주는 역할.

@Component
public class JwtTokenProvider {

	UserService userService;
	JwtTokenService tokenService;

	public JwtTokenProvider(UserService userService, JwtTokenService tokenService) {
		this.userService = userService;
		this.tokenService = tokenService;
	}

	// JwtTokenProvider.java의 authenticate 메서드
	public JwtTokenResponse authenticate(Map<String, String> map) {
		String encodedtoken = null;
		String userId = map.get("userId");
		String password = map.get("password");

		UserDTO dto = userService.findById(userId);

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		UsernamePasswordAuthenticationToken token = null;
		if (dto != null && passwordEncoder.matches(password, dto.getPassword())) {
			// 비밀번호가 일치할 때만 토큰 생성
			// ... 코드 생략 ...
			encodedtoken = tokenService.generateToken(token);
		}//end if

		// 문제: 비밀번호가 일치하지 않아도 JwtTokenResponse를 반환함
		return new JwtTokenResponse(encodedtoken, userId, dto.getRole().toString());
	}
}