package com.exam.question;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {
	private final QuestionService questionService;

	@PostMapping("/add")
	public ResponseEntity<?> addQuestion(@RequestBody QuestionDTO questionDTO) {
		try {
			String userId = getAuthenticatedUserId(); // 인증된 사용자 ID 가져오기
			questionService.addQuestion(questionDTO, userId);
			return ResponseEntity.status(HttpStatus.CREATED).body("질문이 성공적으로 등록되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@PutMapping("/update/{questionId}")
	public ResponseEntity<String> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionDTO dto) {
		String userId = getAuthenticatedUserId();  // 인증된 사용자의 ID를 가져옴
		questionService.updateQuestion(userId, questionId, dto);  // 순서에 맞게 수정된 파라미터를 전달
		return ResponseEntity.ok("질문 수정 완료");
	}

	@DeleteMapping("/delete/{questionId}")
	public ResponseEntity<String> deleteQuestion(@PathVariable Long questionId) {
		try {
			String userId = getAuthenticatedUserId(); // 인증된 사용자 ID 가져오기
			questionService.deleteQuestion(userId, questionId);  // 삭제 처리
			return ResponseEntity.ok("질문이 성공적으로 삭제되었습니다.");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}


	// 특정 사용자가 쓴 질문만 조회
	@GetMapping("/{userId}")
	public ResponseEntity<List<QuestionDTO>> getQuestionsByUser(@PathVariable String userId) {
		String authenticatedUserId = getAuthenticatedUserId();
		if (!authenticatedUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		List<QuestionDTO> questions = questionService.getQuestionsByUser(userId);
		return ResponseEntity.ok(questions);
	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
