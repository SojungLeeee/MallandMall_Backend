package com.exam.answer;

import com.exam.question.QuestionDTO;
import com.exam.question.QuestionService;
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
	private final QuestionService questionService;

	// 답변 추가
	@PostMapping("/add/{questionId}")
	public ResponseEntity<AnswerDTO> addAnswer(
		@PathVariable Long questionId,
		@RequestBody AnswerDTO answerDTO) {
		try {
			String adminId = getAuthenticatedUserId();
			AnswerDTO savedAnswer = answerService.addAnswer(questionId, adminId, answerDTO.getContent());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedAnswer);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// 모든 질문 조회
	@GetMapping("/all")
	public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
		try {
			List<QuestionDTO> questions = questionService.getAllQuestions();
			return ResponseEntity.ok(questions);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	// 답변 수정
	@PutMapping("/update/{answerId}")
	public ResponseEntity<AnswerDTO> updateAnswer(
		@PathVariable Long answerId,
		@RequestBody AnswerDTO answerDTO) {
		try {
			String adminId = getAuthenticatedUserId();
			AnswerDTO updatedAnswer = answerService.updateAnswer(answerId, adminId, answerDTO.getContent());
			return ResponseEntity.ok(updatedAnswer);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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
			String adminId = getAuthenticatedUserId();
			answerService.deleteAnswer(answerId, adminId);
			return ResponseEntity.ok("답변이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	// 인증된 관리자 ID를 가져오는 메서드
	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}