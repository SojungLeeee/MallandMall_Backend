package com.exam.social;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.exam.security.JwtTokenService;
import com.exam.security.JwtTokenResponse;
import com.exam.user.User;
import com.exam.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {

	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final JwtTokenService jwtTokenService;

	@Value("${kakao.client-id}")
	private String clientId;

	@Value("${kakao.redirect-uri}")
	private String redirectUri;

	//  카카오 액세스 토큰 요청
	private String getKakaoAccessToken(String code) {
		String tokenUrl = "https://kauth.kakao.com/oauth/token";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", clientId);
		params.add("redirect_uri", redirectUri.trim()); // 공백 제거
		params.add("code", code);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		try {
			System.out.println("🔹 카카오 요청 - client_id: " + clientId);
			System.out.println("🔹 카카오 요청 - redirect_uri: " + redirectUri.trim());
			System.out.println("🔹 카카오 요청 - code: " + code);

			ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
			System.out.println("🔹 카카오 토큰 응답: " + response.getBody());

			if (response.getStatusCode() == HttpStatus.OK) {
				return (String) response.getBody().get("access_token");
			} else {
				throw new RuntimeException(" 카카오 액세스 토큰 요청 실패: " + response.getBody());
			}
		} catch (Exception e) {
			throw new RuntimeException(" 카카오 API 요청 실패: " + e.getMessage());
		}
	}

	// 카카오 사용자 정보 요청
	private KakaoUserDTO getKakaoUserInfo(String accessToken) {
		String userUrl = "https://kapi.kakao.com/v2/user/me";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);

		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(userUrl, HttpMethod.GET, request, Map.class);

		Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

		KakaoUserDTO kakaoUser = new KakaoUserDTO();
		kakaoUser.setId((Long) response.getBody().get("id"));
		kakaoUser.setEmail(kakaoAccount.getOrDefault("email", "").toString());
		kakaoUser.setNickname(profile.getOrDefault("nickname", "카카오 사용자").toString());

		return kakaoUser;
	}

	//  카카오 로그인 처리
	@Override
	@Transactional
	public JwtTokenResponse kakaoLogin(String code) {
		try {
			String accessToken = getKakaoAccessToken(code);
			KakaoUserDTO kakaoUser = getKakaoUserInfo(accessToken);

			User user = userRepository.findByKakaoId(kakaoUser.getId()).orElse(null);

			if (user == null) {
				user = new User();
				user.setUserId("kakao_" + kakaoUser.getId());
				user.setPassword(UUID.randomUUID().toString());
				user.setUserName(kakaoUser.getNickname());
				user.setEmail(kakaoUser.getEmail());
				user.setKakaoId(kakaoUser.getId());
				userRepository.save(user);
			}

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(user.getUserId(), null,
					Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));

			String token = jwtTokenService.generateToken(authentication);

			// JwtTokenResponse로 리턴
			return new JwtTokenResponse(token, user.getUserId(), user.getRole().name());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("카카오 로그인 처리 중 오류 발생: " + e.getMessage());
		}
	}
}
