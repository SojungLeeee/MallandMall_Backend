package com.exam.social.google;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenInfoDTO {

	private String iss;
	private String azp;
	private String aud;
	private String sub;
	private String email;
	private String name;
	private String picture;
	private boolean email_verified;
}