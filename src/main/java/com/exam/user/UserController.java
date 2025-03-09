package com.exam.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {
	UserService memberService;

	public UserController(UserService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/home")
	public ResponseEntity<String> home() {
		return ResponseEntity.status(200).body("home"); // 200 OK 상태 코드와 함께 "home" 반환
	}

    /*
	 	headers : Content-Type:application/json
	 	body :  {
				   "userid":"llsj09",
				   "passwd":"1234",
				   "username":"이소정",
				   "post": "당감주공",
				   "addr1": "205동",
				   "addr2": "605호",
				   "phoneNumber": "01077389232",
				   "email": "llsj08@naver.com"

				}
	 */

	@PostMapping("/signup")
	public ResponseEntity<UserDTO> signup(@Valid @RequestBody UserDTO dto) {
		log.info("비번 암호화전: {}", dto.getPasswd());
		//비번을 암호화 해야됨.
		String encodedPW = new BCryptPasswordEncoder().encode(dto.getPasswd());
		log.info("비번 암호화후: {}", encodedPW);

		dto.setPasswd(encodedPW);
		memberService.save(dto);

		return ResponseEntity.created(null).body(dto);  // 201 상태코드 반환됨.
	}

}
