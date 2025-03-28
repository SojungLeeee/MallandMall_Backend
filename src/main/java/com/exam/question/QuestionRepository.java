package com.exam.question;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findByUserId(String userId);
	List<Question> findByStatus(Question.QuestionStatus status);


}
