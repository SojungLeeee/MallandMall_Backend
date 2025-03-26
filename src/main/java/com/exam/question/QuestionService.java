package com.exam.question;

import java.util.List;

import com.exam.question.QuestionDTO;

public interface QuestionService  {
	void addQuestion(QuestionDTO questionDTO, String userId);
	void updateQuestion(String userId, Long questionId, QuestionDTO questionDTO); // 수정된 부분
	List<QuestionDTO> getQuestionsByUser(String userId);

}