package com.exam.review;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.orderinfo.OrderInfoRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

	private final ReviewRepository reviewRepository;
	private final OrderInfoRepository orderInfoRepository;

	public ReviewServiceImpl(ReviewRepository reviewRepository, OrderInfoRepository orderInfoRepository) {
		this.reviewRepository = reviewRepository;
		this.orderInfoRepository = orderInfoRepository;
	}

	// 리뷰 작성 (구매한 사용자만 가능)
	@Override
	@Transactional
	public void addReview(ReviewDTO dto, String userId) {
		boolean hasPurchased = orderInfoRepository.existsByUserIdAndProductCode(userId, dto.getProductCode());

		if (!hasPurchased) {
			throw new IllegalArgumentException("구매한 상품에만 리뷰를 작성할 수 있습니다.");
		}

		Review review = Review.builder()
			.userId(userId)
			.productCode(dto.getProductCode())
			.rating(dto.getRating())
			.reviewText(dto.getReviewText())
			.build();

		reviewRepository.save(review);
	}

	//  리뷰 수정 (본인만 가능)
	@Override
	@Transactional
	public void updateReview(Long reviewId, ReviewDTO dto, String userId) {
		Review review = reviewRepository.findByReviewIdAndUserId(reviewId, userId);
		if (review == null) {
			throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
		}

		review.setRating(dto.getRating());
		review.setReviewText(dto.getReviewText());

		reviewRepository.save(review);
	}

	//  리뷰 삭제 (본인만 가능)
	@Override
	@Transactional
	public void deleteReview(Long reviewId, String userId) {
		Review review = reviewRepository.findByReviewIdAndUserId(reviewId, userId);
		if (review == null) {
			throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
		}

		reviewRepository.delete(review);
	}

	//  해당 상품의 모든 리뷰 조회
	@Override
	public List<ReviewDTO> getReviewsByProduct(String productCode) {
		List<Review> reviews = reviewRepository.findByProductCode(productCode);
		return reviews.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	//  특정 사용자가 작성한 모든 리뷰 조회
	@Override
	public List<ReviewDTO> getReviewsByUser(String userId) {
		List<Review> reviews = reviewRepository.findByUserId(userId);
		return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// 특정 상품의 평균 별점 조회
	@Override
	public double getAverageRating(String productCode) {
		return reviewRepository.getAverageRating(productCode);
	}

	// 특정 상품의 별점 별 리뷰 조회
	@Override
	public List<ReviewDTO> findByProductCodeAndRating(String productCode, int rating) {
		List<Review> reviews = reviewRepository.findByProductCodeAndRating(productCode, rating);
		return reviews.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	//  Entity → DTO 변환 메서드
	private ReviewDTO convertToDTO(Review review) {
		return ReviewDTO.builder()
			.reviewId(review.getReviewId())
			.userId(review.getUserId())
			.productCode(review.getProductCode())
			.rating(review.getRating())
			.reviewText(review.getReviewText())
			.reviewDate(review.getReviewDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			.build();
	}
}
