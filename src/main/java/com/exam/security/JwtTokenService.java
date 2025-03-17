package com.exam.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

// token 생성하는 기능
@Component  // @Service
public class JwtTokenService {

	private final JwtEncoder jwtEncoder;

	public JwtTokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	//Spring Security에서 인증 결과인 Authentication 이용해서 token을 생성하는 메서드
	public String generateToken(Authentication authentication) {

		var scope = authentication
			.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(" "));
		// role 정보를 추출
		var roles = authentication
			.getAuthorities()
			.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toList());

		var claims = JwtClaimsSet.builder()
			.issuer("self")
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plus(90, ChronoUnit.MINUTES))
			.subject(authentication.getName())
			.claim("scope", scope)
			.claim("roles", roles) // 유지
			.claim("role", roles.get(0)) // 추가된 부분
			.build();

		return this.jwtEncoder
			.encode(JwtEncoderParameters.from(claims))
			.getTokenValue();
	}
}