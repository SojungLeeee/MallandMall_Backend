package com.exam.question;

import java.util.List;

public interface QuestionService {
	void addQuestion(QuestionDTO questionDTO, String userId);

	// 답변 삭제
	void deleteAnswer(Long answerId, String userId);

	void updateQuestion(String userId, Long questionId, QuestionDTO questionDTO); // 수정된 부분
	List<QuestionDTO> getQuestionsByUser(String userId);
	void deleteQuestion(String userId, Long questionId); // 삭제 기능 추가

	List<QuestionDTO> getAllQuestions();
}
