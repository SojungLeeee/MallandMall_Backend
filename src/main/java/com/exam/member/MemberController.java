package com.exam.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
    MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/home")
    public ResponseEntity<String> home(){
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
    public ResponseEntity<MemberDTO> signup(@RequestBody MemberDTO dto) {
        try {
            // 비밀번호 암호화
            String encodedPW = new BCryptPasswordEncoder().encode(dto.getPasswd());
            dto.setPasswd(encodedPW);  // 암호화된 비밀번호로 DTO 업데이트

            // 회원가입 서비스 호출
            memberService.save(dto);  // 예외가 발생하지 않으면 성공으로 간주

            return ResponseEntity.created(null).body(dto);  // 201 Created

        } catch (Exception e) {
            // 회원가입 처리 실패 시
            return ResponseEntity.status(400).body(dto);  // 400 Bad Request
        }
    }


}
