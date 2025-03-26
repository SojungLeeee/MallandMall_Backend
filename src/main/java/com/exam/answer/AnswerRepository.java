package com.exam.answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Answer 엔티티에 대한 JPA 레포지토리
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
	// 필요한 추가적인public interface AnswerRepository extends JpaRepository<Answer, Long> {
	//     List<Answer> findByQuestionId(Long questionId);  // 해당 질문에 대한 모든 답변을 조회
	// } 쿼리 메서드를 정의할 수 있음
	List<Answer> findByQuestionId(Long questionId);
}
