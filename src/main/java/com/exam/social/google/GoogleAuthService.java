package com.exam.social.google;


import com.exam.security.JwtTokenResponse;

public interface GoogleAuthService {

	JwtTokenResponse authenticateWithGoogle(GoogleLoginRequestDTO request);
}