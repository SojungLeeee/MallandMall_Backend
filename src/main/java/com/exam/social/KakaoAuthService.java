package com.exam.social;

import com.exam.security.JwtTokenResponse;

public interface KakaoAuthService {
	JwtTokenResponse kakaoLogin(String code);
}
