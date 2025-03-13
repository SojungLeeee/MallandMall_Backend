package com.exam.review;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "reviews")  // ✅ 테이블명 명시
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reviewId;  // 리뷰 ID (자동 증가)

	@Column(nullable = false)
	private String userId;  // 리뷰 작성자 (FK: user 테이블)

	@Column(nullable = false)
	private String productCode;  // 상품 코드 (FK: products 테이블)

	private int rating;  // 별점 (1~5)

	private String reviewText;  // 리뷰 내용

	@Column(nullable = false)
	private LocalDateTime reviewDate;  // 작성일

	@PrePersist
	protected void onCreate() {
		this.reviewDate = LocalDateTime.now();  // 자동으로 현재 시간 설정
	}
}
