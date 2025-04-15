package com.exam.review.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewAnalysisRepository extends JpaRepository<ReviewAnalysis, Long> {

	// 상품 코드로 분석 결과 조회
	Optional<ReviewAnalysis> findByProductCode(String productCode);

	// 최신 분석 결과만 가져오기 위한 메소드
	Optional<ReviewAnalysis> findTopByProductCodeOrderByCreatedAtDesc(String productCode);

	// 분석 결과 삭제
	void deleteByProductCode(String productCode);
}