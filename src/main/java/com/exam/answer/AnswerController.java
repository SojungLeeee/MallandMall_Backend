package com.exam.answer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/answers")
@RequiredArgsConstructor
public class AnswerController {

	private final AnswerService answerService;

	// 답변 추가
	@PostMapping("/add/{questionId}")
	public ResponseEntity<String> addAnswer(@PathVariable Long questionId,
		@RequestBody String content) {
		try {
			String adminId = getAuthenticatedUserId();  // 인증된 관리자 ID 가져오기
			answerService.addAnswer(questionId, adminId, content);
			return ResponseEntity.status(HttpStatus.CREATED).body("답변이 성공적으로 등록되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	// 답변 수정
	@PutMapping("/update/{answerId}")
	public ResponseEntity<String> updateAnswer(@PathVariable Long answerId,
		@RequestBody String content) {
		try {
			String adminId = getAuthenticatedUserId();  // 인증된 관리자 ID 가져오기
			answerService.updateAnswer(answerId, adminId, content);
			return ResponseEntity.ok("답변이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	// 특정 질문에 대한 답변 조회
	@GetMapping("/question/{questionId}")
	public ResponseEntity<List<AnswerDTO>> getAnswersByQuestion(@PathVariable Long questionId) {
		try {
			List<AnswerDTO> answers = answerService.getAnswersByQuestion(questionId);
			return ResponseEntity.ok(answers);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	// 답변 삭제
	@DeleteMapping("/delete/{answerId}")
	public ResponseEntity<String> deleteAnswer(@PathVariable Long answerId) {
		try {
			String adminId = getAuthenticatedUserId();  // 인증된 관리자 ID 가져오기
			answerService.deleteAnswer(answerId, adminId);  // userId 추가
			return ResponseEntity.ok("답변이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}


	// 인증된 관리자 ID를 가져오는 메서드
	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();  // Spring Security에서 인증된 사용자의 이름(아이디) 반환
	}
}
