package com.exam.answer;

import java.util.List;

import com.exam.question.QuestionDTO;

public interface AnswerService {
	AnswerDTO addAnswer(Long questionId, String adminId, String content);
	AnswerDTO updateAnswer(Long answerId, String adminId, String content);
	void deleteAnswer(Long answerId, String adminId);
	List<AnswerDTO> getAnswersByQuestion(Long questionId);
	List<QuestionDTO> getAllQuestions(); // 추가된 메서드
}
