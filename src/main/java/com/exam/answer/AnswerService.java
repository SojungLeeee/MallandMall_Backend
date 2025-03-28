package com.exam.answer;

import com.exam.question.QuestionDTO;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

public interface AnswerService {


	// 답변 추가
	@Transactional
	AnswerDTO addAnswer(Long questionId, String userId, String content, String status);

	AnswerDTO updateAnswer(Long answerId, String adminId, String content);
	void deleteAnswer(Long answerId, String adminId);
	List<AnswerDTO> getAnswersByQuestion(Long questionId);
	List<QuestionDTO> getAllQuestions(); // 모든 질문 조회 메서드
}
