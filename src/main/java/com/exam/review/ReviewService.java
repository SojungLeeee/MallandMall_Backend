package com.exam.review;

import java.util.List;

public interface ReviewService {

	//  리뷰 작성
	void addReview(ReviewDTO dto, String userId);

	//  리뷰 수정
	void updateReview(Long reviewId, ReviewDTO dto, String userId);

	//  리뷰 삭제
	void deleteReview(Long reviewId, String userId);

	//  특정 상품의 리뷰 목록 조회
	List<ReviewDTO> getReviewsByProduct(String productCode);

	//  특정 상품의 평균 별점 조회
	double getAverageRating(String productCode);


	List<ReviewDTO> getReviewsByUser(String userId);
}
