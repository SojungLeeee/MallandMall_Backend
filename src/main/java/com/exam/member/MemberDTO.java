package com.exam.member;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
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
public class MemberDTO {
	String userid;  // 아이디 (primary key)
	String passwd;  // 비밀번호
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
