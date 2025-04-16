package com.exam.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 기존 메서드들
	// 특정 상품의 모든 리뷰 조회
	List<Review> findByProductCode(String productCode);

	// 특정 사용자 모든 리뷰 조회
	List<Review> findByUserId(String userId);

	// 특정 사용자가 작성한 특정 리뷰 조회
	Review findByReviewIdAndUserId(Long reviewId, String userId);

	// 특정 상품의 평균 별점 계산
	@Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.productCode = :productCode")
	double getAverageRating(String productCode);

	// 특정 상품의 별점 별 리뷰 조회
	List<Review> findByProductCodeAndRating(String productCode, int rating);


	/* 증분 분석을 위한 추가 메서드들
		 특정 상품의 리뷰 개수 조회 */
	long countByProductCode(String productCode);

	// 특정 날짜 이후의 리뷰만 가져오기
	List<Review> findByProductCodeAndReviewDateAfter(
		String productCode,
		LocalDateTime dateTime
	);

	// 날짜 기준 최신 리뷰 가져오기
	List<Review> findByProductCodeOrderByReviewDateDesc(
		String productCode,
		Pageable pageable
	);

	// 리뷰 ID 기준 최신 N개 리뷰 가져오기 (마지막 분석 이후의 신규 리뷰)
	@Query("SELECT r FROM Review r WHERE r.productCode = :productCode AND r.reviewId > :lastReviewId ORDER BY r.reviewId ASC")
	List<Review> findNewerReviews(
		@Param("productCode") String productCode,
		@Param("lastReviewId") Long lastReviewId
	);

	// 가장 최근 리뷰의 ID 가져오기
	@Query("SELECT MAX(r.reviewId) FROM Review r WHERE r.productCode = :productCode")
	Long findLatestReviewId(@Param("productCode") String productCode);

	// 리뷰 수가 많은 상위 N개 상품 조회
	@Query("SELECT r.productCode, COUNT(r.reviewId) as reviewCount FROM Review r GROUP BY r.productCode ORDER BY reviewCount DESC")
	List<Object[]> findProductsWithMostReviews(Pageable pageable);

	// 특정 기간 내 리뷰 조회
	List<Review> findByProductCodeAndReviewDateBetween(
		String productCode,
		LocalDateTime startDate,
		LocalDateTime endDate
	);

	// 특정 ID 범위의 리뷰 조회
	@Query("SELECT r FROM Review r WHERE r.productCode = :productCode AND r.reviewId BETWEEN :startId AND :endId ORDER BY r.reviewId ASC")
	List<Review> findReviewsByIdRange(
		@Param("productCode") String productCode,
		@Param("startId") Long startId,
		@Param("endId") Long endId
	);

	// 리뷰 샘플링을 위한 메서드
	@Query(value = "SELECT * FROM reviews WHERE product_code = :productCode ORDER BY RAND() LIMIT :limit", nativeQuery = true)
	List<Review> findRandomSampleByProductCode(
		@Param("productCode") String productCode,
		@Param("limit") int limit
	);
}