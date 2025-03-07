package com.exam.member;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Alias("MemberDTO")
public class MemberDTO {

	@NotBlank(message = "아이디 입력 필수")
	String userid;  // 아이디 (primary key)

	@NotBlank(message = "비밀번호 입력 필수")
	String passwd;  // 비밀번호

	@NotBlank(message = "사용자명 입력 필수")
	String username;  // 사용자 이름

	String post;  // 주소
	String addr1;  // 주소1
	String addr2;  // 주소2
	String phoneNumber;  // 전화번호

	String email;  // 이메일

	String role = "USER";  // 역할, 기본값 'USER'

	@CreationTimestamp
	@Column(updatable = false) //저장할때만 자동저장O 수정할때는 저장 X 을위한것
	LocalDate createDate; //저장할때만 자동저장O 수정할때는 저장 X
}
