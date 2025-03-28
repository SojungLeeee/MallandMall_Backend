package com.exam.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Answer 엔티티에 대한 JPA 레포지토리
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
	// 해당 질문에 대한 모든 답변을 조회하는 쿼리 메서드
	List<Answer> findByQuestionId(Long questionId);
}
