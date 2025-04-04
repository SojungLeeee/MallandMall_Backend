package com.exam.review.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review-analysis")
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisController {

	private final ReviewAnalysisService reviewAnalysisService;

	@GetMapping("/{productCode}")
	public ResponseEntity<ReviewAnalysisResponseDTO> analyzeReviews(@PathVariable String productCode) {
		log.info("Received review analysis request for product: {}", productCode);

		try {
			ReviewAnalysisResponseDTO response = reviewAnalysisService.analyzeReviews(productCode);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error during review analysis: {}", e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}
}