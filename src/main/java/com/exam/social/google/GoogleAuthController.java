package com.exam.social.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.security.JwtTokenProvider;
import com.exam.security.JwtTokenResponse;
import com.exam.user.Role;
import com.exam.user.UserDTO;
import com.exam.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("auth")
public class GoogleAuthController {
	private final GoogleAuthService googleAuthService;

	public GoogleAuthController(GoogleAuthService googleAuthService) {
		this.googleAuthService = googleAuthService;
	}


	@PostMapping("/google")
	public ResponseEntity<?> authenticateGoogle(@RequestBody GoogleLoginRequestDTO request) {
		try {
			// 서비스 호출
			JwtTokenResponse tokenResponse = googleAuthService.authenticateWithGoogle(request);

			if (tokenResponse == null) {
				return ResponseEntity.badRequest().body("Authentication failed");
			}

			return ResponseEntity.ok(tokenResponse);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error during Google authentication: " + e.getMessage());
		}
	}


	private String generateRandomPassword() {
		// 랜덤 패스워드 생성 로직 개선
		return "google" + UUID.randomUUID().toString().substring(0, 8);
	}
}
