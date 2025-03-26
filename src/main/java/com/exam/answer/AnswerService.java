package com.exam.answer;

import java.util.List;

public interface AnswerService {
	void addAnswer(Long questionId, String adminId, String content);
	void updateAnswer(Long answerId, String adminId, String content);
	void deleteAnswer(Long answerId, String adminId);
	List<AnswerDTO> getAnswersByQuestion(Long questionId);
}
