package com.exam.social;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserDTO {
	private Long id;  // 카카오 고유 ID
	private String email;
	private String nickname;
}
