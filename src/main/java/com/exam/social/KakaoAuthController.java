package com.exam.social;

import java.util.Map;

import com.exam.security.JwtTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class KakaoAuthController {

	private final KakaoAuthService kakaoAuthService;

	@PostMapping
	public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> requestBody) {
		try {
			String code = requestBody.get("code");
			System.out.println(" 받은 인가 코드: " + code);

			if (code == null || code.isEmpty()) {
				throw new IllegalArgumentException(" 인가 코드가 비어 있습니다.");
			}

			JwtTokenResponse jwtToken = kakaoAuthService.kakaoLogin(code);

			//  프론트로 응답 (token, userId, role)
			return ResponseEntity.ok(jwtToken);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "카카오 로그인 중 오류 발생", "message", e.getMessage()));
		}
	}
}
