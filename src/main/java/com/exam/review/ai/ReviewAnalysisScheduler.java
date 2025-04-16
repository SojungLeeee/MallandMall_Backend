package com.exam.review.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exam.product.Product;
import com.exam.product.ProductRepository;
import com.exam.review.Review;
import com.exam.review.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisScheduler {

	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewAnalysisService reviewAnalysisService;
	private final ReviewAnalysisRepository reviewAnalysisRepository;

	// 분석 임계값 설정
	private static final int INCREMENTAL_ANALYSIS_THRESHOLD = 100; // 리뷰가 이 수 이상이면 증분 분석 사용
	private static final int REVIEW_COUNT_CHANGE_THRESHOLD = 5; // 리뷰 수가 이 수 이상 변경되면 분석 시작

	/**
	 * 매일 새벽 3시에 실행되는 스케줄러
	 * 마지막 분석 후 일주일이 지났거나 리뷰 수에 변화가 있는 상품의 리뷰 분석을 업데이트
	 */
	@Scheduled(cron = "0 0 3 * * ?")
	public void updateReviewAnalysis() {
		log.info("리뷰 분석 정기 업데이트 시작: {}", LocalDateTime.now());

		try {
			// 모든 상품 조회
			List<Product> allProducts = productRepository.findAll();
			log.info("전체 상품 수: {}", allProducts.size());

			int updatedCount = 0;

			// 각 상품마다 마지막 분석 시간 확인
			for (Product product : allProducts) {
				String productCode = product.getProductCode();

				// 현재 리뷰 수 확인
				long currentReviewCount = reviewRepository.countByProductCode(productCode);

				// 리뷰가 없으면 분석 건너뛰기
				if (currentReviewCount == 0) {
					continue;
				}

				// 마지막 분석 결과 조회
				Optional<ReviewAnalysis> lastAnalysisOpt =
					reviewAnalysisRepository.findTopByProductCodeOrderByCreatedAtDesc(productCode);

				if (lastAnalysisOpt.isPresent()) {
					ReviewAnalysis lastAnalysis = lastAnalysisOpt.get();
					int previousReviewCount = lastAnalysis.getReviewCount();

					// 리뷰 수 변화 확인
					int reviewCountChange = (int)currentReviewCount - previousReviewCount;

					// 리뷰 수가 충분히 변경되었거나 마지막 분석 후 일주일 이상 지났으면 분석 수행
					boolean needsUpdate = reviewCountChange >= REVIEW_COUNT_CHANGE_THRESHOLD ||
						lastAnalysis.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7));

					if (needsUpdate) {
						try {
							log.info("상품 {} 리뷰 분석 업데이트 중... (리뷰 수 변화: {})",
								productCode, reviewCountChange);

							// 분석 수행 - 리뷰가 많으면 증분 분석 자동 적용
							reviewAnalysisService.analyzeReviews(productCode);
							updatedCount++;

							// 과부하 방지를 위한 지연
							TimeUnit.SECONDS.sleep(2);
						} catch (Exception e) {
							log.error("상품 {} 리뷰 분석 업데이트 실패: {}", productCode, e.getMessage());
						}
					}
				} else {
					// 분석 결과가 없는 경우 새로 분석
					try {
						log.info("상품 {} 최초 리뷰 분석 중...", productCode);
						reviewAnalysisService.analyzeReviews(productCode);
						updatedCount++;

						// 과부하 방지를 위한 지연
						TimeUnit.SECONDS.sleep(2);
					} catch (Exception e) {
						log.error("상품 {} 리뷰 분석 실패: {}", productCode, e.getMessage());
					}
				}
			}

			log.info("리뷰 분석 정기 업데이트 완료: 업데이트된 상품 수 {}", updatedCount);
		} catch (Exception e) {
			log.error("리뷰 분석 정기 업데이트 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	/**
	 * 대용량 리뷰 처리를 위한 정시 분석 스케줄러 (주 1회 일요일 새벽 4시)
	 * 리뷰 수가 많은 상품은 샘플링하여 전체 분석 수행
	 */
	@Scheduled(cron = "0 0 4 * * 0")
	public void performFullAnalysisForHighVolumeProducts() {
		log.info("대용량 리뷰 정기 전체 분석 시작: {}", LocalDateTime.now());

		try {
			// 리뷰 수가 많은 상품 조회 (상위 20개)
			List<Object[]> highVolumeProducts =
				reviewRepository.findProductsWithMostReviews(PageRequest.of(0, 20));

			log.info("대용량 리뷰 상품 수: {}", highVolumeProducts.size());

			for (Object[] productInfo : highVolumeProducts) {
				String productCode = (String) productInfo[0];
				Long reviewCount = (Long) productInfo[1];

				if (reviewCount > 500) { // 500개 이상의 리뷰가 있는 경우
					try {
						log.info("대용량 상품 {} 전체 리뷰 분석 중... (리뷰 수: {})",
							productCode, reviewCount);

						// 전체 분석 수행 (서비스에 샘플링 로직 구현 필요)
						// reviewAnalysisService.analyzeReviewsWithSampling(productCode, 500);

						// 과부하 방지를 위한 지연
						TimeUnit.SECONDS.sleep(5);
					} catch (Exception e) {
						log.error("대용량 상품 {} 리뷰 분석 실패: {}", productCode, e.getMessage());
					}
				}
			}

			log.info("대용량 리뷰 정기 전체 분석 완료");
		} catch (Exception e) {
			log.error("대용량 리뷰 정기 전체 분석 중 오류 발생: {}", e.getMessage(), e);
		}
	}
}