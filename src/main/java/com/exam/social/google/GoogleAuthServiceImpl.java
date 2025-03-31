package com.exam.social.google;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exam.coupon.CouponService;
import com.exam.security.JwtTokenResponse;
import com.exam.security.JwtTokenService;
import com.exam.user.Role;
import com.exam.user.UserDTO;
import com.exam.user.UserService;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

	private final Logger logger = LoggerFactory.getLogger(GoogleAuthServiceImpl.class);

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	private final WebClient webClient;
	private final UserService userService;
	private final JwtTokenService jwtTokenService;
	private final PasswordEncoder passwordEncoder;
	private final CouponService couponService;

	public GoogleAuthServiceImpl(UserService userService, JwtTokenService jwtTokenService,
		PasswordEncoder passwordEncoder, CouponService couponService) {
		this.userService = userService;
		this.jwtTokenService = jwtTokenService;
		this.passwordEncoder = passwordEncoder;
		this.couponService = couponService;
		this.webClient = WebClient.create();
	}

	@Override
	public JwtTokenResponse authenticateWithGoogle(GoogleLoginRequestDTO request) {
		try {
			// Google의 토큰 정보 엔드포인트를 호출하여 ID 토큰 검증
			GoogleTokenInfoDTO tokenInfo = webClient.get()
				.uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getToken())
				.retrieve()
				.bodyToMono(GoogleTokenInfoDTO.class)
				.block();

			// 토큰이 유효하지 않거나 클라이언트 ID가 일치하지 않는 경우
			if (tokenInfo == null || !googleClientId.equals(tokenInfo.getAud())) {
				logger.error("Invalid Google token or client ID mismatch");
				return null;
			}

			String email = tokenInfo.getEmail();
			String name = tokenInfo.getName();

			// 이메일이 확인되지 않은 경우
			if (!tokenInfo.isEmail_verified()) {
				logger.error("Email not verified with Google: {}", email);
				return null;
			}

			// 사용자가 DB에 있는지 확인
			UserDTO user = userService.findById(email);

			if (user == null) {
				// 새 사용자 등록 로직
				UserDTO newUser = new UserDTO();
				newUser.setUserId(email); // 이메일을 ID로 사용
				newUser.setUserName(name);
				newUser.setEmail(email);

				// 랜덤 비밀번호 생성 및 인코딩
				String randomPassword = generateRandomPassword();
				newUser.setPassword(passwordEncoder.encode(randomPassword));

				// Role enum 값 설정
				newUser.setRole(Role.USER);

				// 사용자 등록 - UserService의 save 메서드 사용
				userService.save(newUser);
				logger.info("New user registered via Google: {}", email);

				user = newUser;

				System.out.println("user 정보 : " + user);

				couponService.addNewMemberOnlineCoupon(user.getUserId());
				couponService.addNewMemberOfflineCoupon(user.getUserId());
			}

			// JWT 토큰 생성
			List<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));

			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(email, null, authorities);

			// 토큰 생성
			String token = jwtTokenService.generateToken(authToken);

			// 응답 생성
			return new JwtTokenResponse(token, email, user.getRole().toString());

		} catch (Exception e) {
			logger.error("Error during Google authentication", e);
			return null;
		}
	}

	// 랜덤 패스워드 생성 메서드
	private String generateRandomPassword() {
		return "google" + UUID.randomUUID().toString().substring(0, 8);
	}
}