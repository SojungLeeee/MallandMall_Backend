package com.exam.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	//  특정 상품의 모든 리뷰 조회
	List<Review> findByProductCode(String productCode);

	// 특정 사용자 모든 리뷰 조회
	List<Review> findByUserId(String userId);

	//  특정 사용자가 작성한 특정 리뷰 조회
	Review findByReviewIdAndUserId(Long reviewId, String userId);

	// 특정 상품의 평균 별점 계산
	@Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.productCode = :productCode")
	double getAverageRating(String productCode);

	// 특정 상품의 별점 별 리뷰 조회
	List<Review> findByProductCodeAndRating(String productCode, int rating);
}
