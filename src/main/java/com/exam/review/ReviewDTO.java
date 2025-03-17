package com.exam.review;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ReviewDTO {

	private Long reviewId;
	private String userId;

	private String productCode;
	private int rating;
	private String reviewText;
	private String reviewDate;
}
