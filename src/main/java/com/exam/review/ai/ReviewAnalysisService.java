package com.exam.review.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.product.Product;
import com.exam.product.ProductRepository;
import com.exam.review.Review;
import com.exam.review.ReviewRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisService {

	private final ReviewRepository reviewRepository;
	private final ProductRepository productRepository;
	private final ReviewAnalysisRepository reviewAnalysisRepository;
	private final ChatClient chatClient;

	// 벡터DB 사용을 위한 의존성 주입
	private final ReviewAnalysisVectorRepository reviewAnalysisVectorRepository;
	private final ReviewAnalysisVectorService vectorService;

	// 샘플링 관련 상수
	private static final int MAX_REVIEWS_FOR_FULL_ANALYSIS = 500;  // 전체 분석 시 최대 리뷰 수
	private static final int MAX_REVIEWS_FOR_INCREMENTAL = 100;    // 증분 분석 시 최대 리뷰 수
	private static final int REVIEW_LENGTH_THRESHOLD = 10000;      // 전체 리뷰 텍스트 길이 임계값

	/**
	 * 리뷰 분석을 수행하고 결과를 반환 (수정된 메서드)
	 * @param productCode 상품 코드
	 * @return 리뷰 분석 결과 DTO
	 */
	@Cacheable(value = "reviewAnalysis", key = "#productCode")
	@Transactional
	public ReviewAnalysisResponseDTO analyzeReviews(String productCode) {
		log.info("Analyzing reviews for product code: {}", productCode);

		// 상품 정보 가져오기
		Product product = productRepository.findByProductCode(productCode);
		if (product == null) {
			throw new RuntimeException("상품을 찾을 수 없습니다: " + productCode);
		}

		// 기존 분석 결과 조회
		Optional<ReviewAnalysis> existingAnalysis =
			reviewAnalysisRepository.findTopByProductCodeOrderByCreatedAtDesc(productCode);

		// 리뷰 목록 가져오기
		List<Review> reviews = reviewRepository.findByProductCode(productCode);

		// 리뷰가 없는 경우 빈 응답 반환
		if (reviews.isEmpty()) {
			log.info("No reviews found for product: {}", productCode);
			return createEmptyResponse(product);
		}

		// 평균 별점 계산
		double averageRating = reviewRepository.getAverageRating(productCode);

		// 리뷰 수 확인
		int reviewCount = reviews.size();

		// 리뷰 수가 많은 경우 (500개 이상) 샘플링 분석 수행
		if (reviewCount >= MAX_REVIEWS_FOR_FULL_ANALYSIS) {
			log.info("Large number of reviews detected ({}), using sampling analysis", reviewCount);
			return analyzeReviewsWithSampling(product, reviews, averageRating, MAX_REVIEWS_FOR_FULL_ANALYSIS);
		}

		// 기존 분석이 있고 리뷰 수가 임계값 이상이면 증분 분석 수행
		if (existingAnalysis.isPresent() && reviewCount >= 100) {
			log.info("Using incremental analysis for product with many reviews: {}", productCode);
			return performIncrementalAnalysis(product, existingAnalysis.get(), reviews, averageRating);
		}

		// 기존 분석 결과가 있고, 리뷰 개수가 같다면 기존 결과 사용
		if (existingAnalysis.isPresent() &&
			existingAnalysis.get().getReviewCount() == reviewCount &&
			Math.abs(existingAnalysis.get().getAverageRating() - averageRating) < 0.01) {

			log.info("Using existing analysis for product: {}", productCode);
			return convertEntityToDTO(existingAnalysis.get());
		}

		// 리뷰 텍스트 결합
		String reviewTexts = reviews.stream()
			.map(r -> "별점: " + r.getRating() + "/5, 리뷰: " + r.getReviewText())
			.collect(Collectors.joining("\n"));

		// AI 모델에 리뷰 분석 요청
		String aiResponse = requestAiAnalysis(product.getProductName(), reviewTexts);

		// AI 응답을 JSON (DTO) 형태로 파싱
		ReviewAnalysisResponseDTO responseDTO = parseAiResponse(aiResponse);
		responseDTO.setProductCode(productCode);
		responseDTO.setProductName(product.getProductName());
		responseDTO.setAverageRating(averageRating);
		responseDTO.setReviewCount(reviewCount);

		// MySQL에 분석 결과 저장
		ReviewAnalysis analysisEntity = saveReviewAnalysis(responseDTO, reviewTexts);

		// 벡터 검색을 위한 벡터 저장
		try {
			vectorService.saveReviewAnalysisVector(
				analysisEntity.getAnalysisId().intValue(), // 저장된 엔티티의 ID 사용
				productCode,
				product.getProductName(),
				reviewTexts,
				responseDTO.getSummary(),
				responseDTO.getKeyPositivePoints(),
				responseDTO.getKeyNegativePoints()
			);
			log.info("Vector embeddings saved for product: {}", productCode);
		} catch (Exception e) {
			log.error("Error saving vector embeddings: {}", e.getMessage(), e);
			// 벡터 저장 실패해도 전체 작업은 실패하지 않도록 함
		}

		log.info("Review analysis completed for product: {}", productCode);
		return responseDTO;
	}

	/**
	 * 샘플링을 이용한 리뷰 분석 메서드
	 */
	private ReviewAnalysisResponseDTO analyzeReviewsWithSampling(
		Product product,
		List<Review> allReviews,
		double averageRating,
		int sampleSize) {

		log.info("Analyzing with sampling for product: {}, sample size: {}",
			product.getProductCode(), sampleSize);

		// 총 리뷰 수
		int totalReviewCount = allReviews.size();

		// 샘플링 수행
		List<Review> sampledReviews = sampleReviews(allReviews, sampleSize);

		// 샘플 리뷰 텍스트 결합
		String reviewTexts = sampledReviews.stream()
			.map(r -> "별점: " + r.getRating() + "/5, 리뷰: " + r.getReviewText())
			.collect(Collectors.joining("\n"));

		// AI 모델에 리뷰 분석 요청 - 샘플링 정보 포함
		String aiResponse = requestSampledAnalysis(
			product.getProductName(),
			reviewTexts,
			sampledReviews.size(),
			totalReviewCount
		);

		// AI 응답을 JSON (DTO) 형태로 파싱
		ReviewAnalysisResponseDTO responseDTO = parseAiResponse(aiResponse);
		responseDTO.setProductCode(product.getProductCode());
		responseDTO.setProductName(product.getProductName());
		responseDTO.setAverageRating(averageRating);
		responseDTO.setReviewCount(totalReviewCount);

		// 가장 최신 리뷰 ID 가져오기
		Long latestReviewId = reviewRepository.findLatestReviewId(product.getProductCode());

		// 분석 결과 저장 - 샘플링 메타데이터 포함
		ReviewAnalysis analysisEntity = ReviewAnalysis.builder()
			.productCode(product.getProductCode())
			.productName(product.getProductName())
			.averageRating(averageRating)
			.reviewCount(totalReviewCount)
			.summary(responseDTO.getSummary())
			.sentimentPositive(responseDTO.getSentimentAnalysis().getOrDefault("positive", 0.0))
			.sentimentNegative(responseDTO.getSentimentAnalysis().getOrDefault("negative", 0.0))
			.sentimentNeutral(responseDTO.getSentimentAnalysis().getOrDefault("neutral", 0.0))
			.keyPositivePointsJson(JsonUtils.toJson(responseDTO.getKeyPositivePoints()))
			.keyNegativePointsJson(JsonUtils.toJson(responseDTO.getKeyNegativePoints()))
			.reviewCategoriesJson(JsonUtils.toJson(responseDTO.getReviewCategories()))
			.recommendationsJson(JsonUtils.toJson(responseDTO.getRecommendations()))
			.lastReviewId(latestReviewId) // 새로 추가된 필드
			.createdAt(LocalDateTime.now())
			.build();

		ReviewAnalysis savedEntity = reviewAnalysisRepository.save(analysisEntity);

		// 벡터 저장
		try {
			vectorService.saveReviewAnalysisVector(
				savedEntity.getAnalysisId().intValue(),
				product.getProductCode(),
				product.getProductName(),
				reviewTexts, // 샘플 리뷰 텍스트만 벡터화
				responseDTO.getSummary(),
				responseDTO.getKeyPositivePoints(),
				responseDTO.getKeyNegativePoints()
			);
			log.info("Vector embeddings saved for sampled product analysis: {}", product.getProductCode());
		} catch (Exception e) {
			log.error("Error saving vector embeddings: {}", e.getMessage(), e);
		}

		return responseDTO;
	}

	/**
	 * 증분 분석을 수행하는 메서드
	 */
	private ReviewAnalysisResponseDTO performIncrementalAnalysis(
		Product product,
		ReviewAnalysis existingAnalysis,
		List<Review> currentReviews,
		double currentAverageRating) {

		log.info("Performing incremental analysis for product: {}", product.getProductCode());

		// 기존 분석의 리뷰 수와 현재 리뷰 수를 비교하여 새 리뷰만 추출
		int existingReviewCount = existingAnalysis.getReviewCount();
		int currentReviewCount = currentReviews.size();

		// 새 리뷰가 없다면 기존 결과를 그대로 반환
		if (existingReviewCount == currentReviewCount) {
			log.info("No new reviews found, using existing analysis");
			return convertEntityToDTO(existingAnalysis);
		}

		// 기존에 분석된 마지막 리뷰 ID 가져오기
		Long lastAnalyzedReviewId = existingAnalysis.getLastReviewId();

		// 마지막 리뷰 ID 기준으로 새로운 리뷰 가져오기
		List<Review> newReviews;
		if (lastAnalyzedReviewId != null) {
			newReviews = reviewRepository.findNewerReviews(product.getProductCode(), lastAnalyzedReviewId);
		} else {
			// 마지막 리뷰 ID가 없는 경우, 최신 N개의 리뷰 가져오기
			int newReviewsCount = currentReviewCount - existingReviewCount;
			newReviews = reviewRepository.findByProductCodeOrderByReviewDateDesc(
				product.getProductCode(), PageRequest.of(0, newReviewsCount));
		}

		log.info("Found {} new reviews for incremental analysis", newReviews.size());

		// 새 리뷰만 분석
		String newReviewTexts = newReviews.stream()
			.map(r -> "별점: " + r.getRating() + "/5, 리뷰: " + r.getReviewText())
			.collect(Collectors.joining("\n"));

		// 기존 분석 요약 정보를 함께 제공하여 컨텍스트 유지
		String contextInfo = "이전 분석 요약: " + existingAnalysis.getSummary() + "\n" +
			"이전 긍정적 포인트: " + existingAnalysis.getKeyPositivePointsJson() + "\n" +
			"이전 부정적 포인트: " + existingAnalysis.getKeyNegativePointsJson();

		// 증분 분석용 AI 요청
		String aiResponse = requestIncrementalAiAnalysis(
			product.getProductName(),
			newReviewTexts,
			contextInfo,
			existingReviewCount,
			newReviews.size()
		);

		// AI 응답을 파싱
		ReviewAnalysisResponseDTO incrementalDTO = parseAiResponse(aiResponse);

		// 최종 응답 DTO 생성 (기존 + 증분)
		ReviewAnalysisResponseDTO mergedDTO = mergeAnalysisResults(
			convertEntityToDTO(existingAnalysis),
			incrementalDTO,
			existingReviewCount,
			newReviews.size()
		);

		mergedDTO.setProductCode(product.getProductCode());
		mergedDTO.setProductName(product.getProductName());
		mergedDTO.setAverageRating(currentAverageRating);
		mergedDTO.setReviewCount(currentReviewCount);

		// 가장 최신 리뷰 ID 가져오기
		Long latestReviewId = reviewRepository.findLatestReviewId(product.getProductCode());

		// 병합된 결과를 MySQL에 저장
		ReviewAnalysis mergedEntity = ReviewAnalysis.builder()
			.productCode(product.getProductCode())
			.productName(product.getProductName())
			.averageRating(currentAverageRating)
			.reviewCount(currentReviewCount)
			.summary(mergedDTO.getSummary())
			.sentimentPositive(mergedDTO.getSentimentAnalysis().getOrDefault("positive", 0.0))
			.sentimentNegative(mergedDTO.getSentimentAnalysis().getOrDefault("negative", 0.0))
			.sentimentNeutral(mergedDTO.getSentimentAnalysis().getOrDefault("neutral", 0.0))
			.keyPositivePointsJson(JsonUtils.toJson(mergedDTO.getKeyPositivePoints()))
			.keyNegativePointsJson(JsonUtils.toJson(mergedDTO.getKeyNegativePoints()))
			.reviewCategoriesJson(JsonUtils.toJson(mergedDTO.getReviewCategories()))
			.recommendationsJson(JsonUtils.toJson(mergedDTO.getRecommendations()))
			.lastReviewId(latestReviewId) // 최신 리뷰 ID 저장
			.createdAt(LocalDateTime.now())
			.build();

		ReviewAnalysis savedEntity = reviewAnalysisRepository.save(mergedEntity);

		// 벡터 검색을 위한 벡터 저장
		try {
			// 전체 리뷰 텍스트 재구성 필요시
			String allReviewTexts = getRepresentativeReviewTexts(product.getProductCode(), 200);

			vectorService.saveReviewAnalysisVector(
				savedEntity.getAnalysisId().intValue(),
				product.getProductCode(),
				product.getProductName(),
				allReviewTexts,
				mergedDTO.getSummary(),
				mergedDTO.getKeyPositivePoints(),
				mergedDTO.getKeyNegativePoints()
			);
			log.info("Vector embeddings updated for product: {}", product.getProductCode());
		} catch (Exception e) {
			log.error("Error updating vector embeddings: {}", e.getMessage(), e);
		}

		log.info("Incremental analysis completed for product: {}", product.getProductCode());
		return mergedDTO;
	}

	/**
	 * 리뷰 목록에서 샘플링하는 메서드
	 */
	private List<Review> sampleReviews(List<Review> allReviews, int sampleSize) {
		if (allReviews.size() <= sampleSize) {
			return allReviews; // 리뷰가 샘플 크기보다 적으면 전체 반환
		}

		List<Review> sampledReviews = new ArrayList<>();

		// 최신 리뷰 30% 포함
		int recentCount = (int)(sampleSize * 0.3);
		for (int i = 0; i < recentCount && i < allReviews.size(); i++) {
			sampledReviews.add(allReviews.get(i));
		}

		// 별점별 층화 샘플링 50%
		int perRatingCount = (int)(sampleSize * 0.1); // 각 별점당 10%
		Map<Integer, List<Review>> ratingGroups = allReviews.stream()
			.collect(Collectors.groupingBy(Review::getRating));

		for (int rating = 1; rating <= 5; rating++) {
			List<Review> ratingReviews = ratingGroups.getOrDefault(rating, Collections.emptyList());
			if (!ratingReviews.isEmpty()) {
				// 이미 샘플링된 리뷰 제외
				ratingReviews = ratingReviews.stream()
					.filter(r -> !sampledReviews.contains(r))
					.collect(Collectors.toList());

				// 해당 별점의 리뷰 무작위 샘플링
				Collections.shuffle(ratingReviews);
				int toTake = Math.min(perRatingCount, ratingReviews.size());
				sampledReviews.addAll(ratingReviews.subList(0, toTake));
			}
		}

		// 남은 20%는 랜덤 샘플링
		List<Review> remainingReviews = allReviews.stream()
			.filter(r -> !sampledReviews.contains(r))
			.collect(Collectors.toList());

		Collections.shuffle(remainingReviews);
		int remainingCount = sampleSize - sampledReviews.size();
		if (remainingCount > 0 && !remainingReviews.isEmpty()) {
			int toTake = Math.min(remainingCount, remainingReviews.size());
			sampledReviews.addAll(remainingReviews.subList(0, toTake));
		}

		return sampledReviews;
	}

	/**
	 * 대표 리뷰 텍스트를 가져오는 메서드 (벡터 저장용)
	 */
	private String getRepresentativeReviewTexts(String productCode, int limit) {
		// 최신 리뷰와 각 별점별 대표 리뷰를 포함
		List<Review> representativeReviews = new ArrayList<>();

		// 최신 리뷰
		List<Review> recentReviews = reviewRepository.findByProductCodeOrderByReviewDateDesc(
			productCode, PageRequest.of(0, limit / 5));
		representativeReviews.addAll(recentReviews);

		// 별점별 대표 리뷰
		for (int rating = 1; rating <= 5; rating++) {
			List<Review> ratingReviews = reviewRepository.findByProductCodeAndRating(productCode, rating);
			if (!ratingReviews.isEmpty()) {
				// 각 별점별로 일부만 추가
				int toTake = Math.min(limit / 5, ratingReviews.size());
				representativeReviews.addAll(ratingReviews.subList(0, toTake));
			}
		}

		// 중복 제거
		List<Review> uniqueReviews = representativeReviews.stream()
			.distinct()
			.limit(limit)
			.collect(Collectors.toList());

		// 텍스트로 변환
		return uniqueReviews.stream()
			.map(r -> "별점: " + r.getRating() + "/5, 리뷰: " + r.getReviewText())
			.collect(Collectors.joining("\n"));
	}

	/**
	 * 증분 분석을 위한 AI 요청 메서드
	 */
	private String requestIncrementalAiAnalysis(
		String productName,
		String newReviewTexts,
		String contextInfo,
		int existingReviewCount,
		int newReviewCount) {

		log.info("Requesting incremental AI analysis for product: {}", productName);

		// 시스템 메시지 수정 - 증분 분석용
		SystemMessage systemMessage = new SystemMessage(
			"당신은 제품 리뷰 증분 분석 전문가입니다. 기존 분석 결과와 함께 새로운 리뷰만을 분석하여 전체 결과를 업데이트해야 합니다.\n" +
				"기존 분석 정보와 새 리뷰를 고려하여 다음 정보를 JSON 형식으로 반환하세요:\n" +
				"1. sentimentAnalysis: 전체 리뷰의 긍정/부정/중립 감정 비율 (예: {\"positive\": 0.7, \"negative\": 0.2, \"neutral\": 0.1})\n"
				+
				"2. keyPositivePoints: 주요 긍정적 포인트 리스트 (최대 5개)\n" +
				"3. keyNegativePoints: 주요 부정적 포인트 리스트 (최대 5개)\n" +
				"4. summary: 전체 리뷰 요약 (100자 내외)\n" +
				"5. recommendations: 구매자에게 도움이 될 추천사항 리스트 (최대 3개)\n" +
				"6. reviewCategories: 다음 5가지 카테고리로 리뷰를 분류하고 각 카테고리별 언급 횟수를 계산하세요: \n" +
				"   - {\"category\": \"맛/품질\", \"count\": 0, \"emoji\": \"😋\"}\n" +
				"   - {\"category\": \"가성비\", \"count\": 0, \"emoji\": \"💰\"}\n" +
				"   - {\"category\": \"신선도\", \"count\": 0, \"emoji\": \"🌱\"}\n" +
				"   - {\"category\": \"양/크기\", \"count\": 0, \"emoji\": \"🍽️\"}\n" +
				"   - {\"category\": \"주차편의성\", \"count\": 0, \"emoji\": \"🅿️\"}\n" +
				"JSON 형식으로 응답해야 합니다. 다른 텍스트나 설명은 포함하지 마세요."
		);

		// 사용자 메시지 생성 - 컨텍스트 포함
		UserMessage userMessage = new UserMessage(
			"다음은 '" + productName + "' 제품에 대한 증분 분석 요청입니다.\n\n" +
				"기존 리뷰 수: " + existingReviewCount + "\n" +
				"새 리뷰 수: " + newReviewCount + "\n\n" +
				"이전 분석 정보:\n" + contextInfo + "\n\n" +
				"새로운 리뷰:\n" + newReviewTexts + "\n\n" +
				"이전 분석과 새 리뷰를 종합하여 전체 리뷰에 대한 업데이트된 분석 결과를 제공해주세요."
		);

		// 프롬프트 생성
		List<Message> messages = Arrays.asList(systemMessage, userMessage);
		Prompt prompt = new Prompt(messages);

		try {
			// AI 호출
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("Error during incremental AI analysis: {}", e.getMessage(), e);
			throw new RuntimeException("증분 AI 분석 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 샘플링된 리뷰를 분석하기 위한 AI 요청 메서드
	 */
	private String requestSampledAnalysis(
		String productName,
		String reviewTexts,
		int sampleSize,
		int totalReviewCount) {

		log.info("Requesting AI analysis for sampled reviews of product: {}", productName);

		// 시스템 메시지 - 샘플링 정보 포함
		SystemMessage systemMessage = new SystemMessage(
			"당신은 제품 리뷰 분석 전문가입니다. 제공된 리뷰 샘플을 기반으로 전체 리뷰 집합을 분석하세요.\n" +
				"다음 정보를 JSON 형식으로 반환하세요:\n" +
				"1. sentimentAnalysis: 긍정/부정/중립 감정 비율 (예: {\"positive\": 0.7, \"negative\": 0.2, \"neutral\": 0.1})\n" +
				"2. keyPositivePoints: 주요 긍정적 포인트 리스트 (최대 5개)\n" +
				"3. keyNegativePoints: 주요 부정적 포인트 리스트 (최대 5개)\n" +
				"4. summary: 전체 리뷰 요약 (100자 내외)\n" +
				"5. recommendations: 구매자에게 도움이 될 추천사항 리스트 (최대 3개)\n" +
				"6. reviewCategories: 다음 5가지 카테고리로 리뷰를 분류하고 각 카테고리별 언급 횟수를 계산하세요: \n" +
				"   - {\"category\": \"맛/품질\", \"count\": 0, \"emoji\": \"😋\"}\n" +
				"   - {\"category\": \"가성비\", \"count\": 0, \"emoji\": \"💰\"}\n" +
				"   - {\"category\": \"신선도\", \"count\": 0, \"emoji\": \"🌱\"}\n" +
				"   - {\"category\": \"양/크기\", \"count\": 0, \"emoji\": \"🍽️\"}\n" +
				"   - {\"category\": \"주차편의성\", \"count\": 0, \"emoji\": \"🅿️\"}\n" +
				"JSON 형식으로 응답해야 합니다. 다른 텍스트나 설명은 포함하지 마세요."
		);

		// 사용자 메시지 생성 - 샘플링 정보 포함
		UserMessage userMessage = new UserMessage(
			"다음은 '" + productName + "' 제품에 대한 리뷰 샘플입니다.\n" +
				"총 리뷰 수: " + totalReviewCount + "\n" +
				"샘플 크기: " + sampleSize + "\n\n" +
				"리뷰 샘플:\n" + reviewTexts + "\n\n" +
				"이 샘플을 기반으로 전체 리뷰에 대한 분석 결과를 제공해주세요."
		);

		// 프롬프트 생성
		List<Message> messages = Arrays.asList(systemMessage, userMessage);
		Prompt prompt = new Prompt(messages);

		try {
			// AI 호출
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("Error during sampled AI analysis: {}", e.getMessage(), e);
			throw new RuntimeException("샘플 AI 분석 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 기존 분석 결과와 증분 분석 결과를 병합하는 메서드
	 */
	private ReviewAnalysisResponseDTO mergeAnalysisResults(
		ReviewAnalysisResponseDTO existing,
		ReviewAnalysisResponseDTO incremental,
		int existingReviewCount,
		int newReviewCount) {

		log.info("Merging analysis results");

		// 가중치 계산 (리뷰 수 기반)
		double existingWeight = (double)existingReviewCount / (existingReviewCount + newReviewCount);
		double newWeight = (double)newReviewCount / (existingReviewCount + newReviewCount);

		// 감정 분석 병합 (가중 평균)
		Map<String, Double> mergedSentiment = new HashMap<>();
		for (String key : existing.getSentimentAnalysis().keySet()) {
			double existingValue = existing.getSentimentAnalysis().getOrDefault(key, 0.0);
			double incrementalValue = incremental.getSentimentAnalysis().getOrDefault(key, 0.0);
			mergedSentiment.put(key, existingValue * existingWeight + incrementalValue * newWeight);
		}

		// 리뷰 카테고리 병합
		List<ReviewCategoryDTO> mergedCategories = new ArrayList<>();
		Map<String, ReviewCategoryDTO> categoryMap = new HashMap<>();

		// 기존 카테고리를 맵에 추가
		for (ReviewCategoryDTO category : existing.getReviewCategories()) {
			categoryMap.put(category.getCategory(), category);
		}

		// 새 카테고리와 병합
		for (ReviewCategoryDTO category : incremental.getReviewCategories()) {
			if (categoryMap.containsKey(category.getCategory())) {
				ReviewCategoryDTO existingCategory = categoryMap.get(category.getCategory());
				// 카운트 업데이트 (가중 합산)
				int mergedCount = (int)(existingCategory.getCount() * existingWeight +
					category.getCount() * newWeight *
						(existingReviewCount + newReviewCount) / newReviewCount);
				existingCategory.setCount(mergedCount);
			} else {
				categoryMap.put(category.getCategory(), category);
			}
		}

		// 맵에서 리스트로 변환
		mergedCategories.addAll(categoryMap.values());

		// 결과 DTO 생성
		ReviewAnalysisResponseDTO mergedDTO = new ReviewAnalysisResponseDTO();
		mergedDTO.setSentimentAnalysis(mergedSentiment);

		// 키 포인트는 증분 분석 결과 우선 사용 (최신 정보 반영)
		mergedDTO.setKeyPositivePoints(incremental.getKeyPositivePoints());
		mergedDTO.setKeyNegativePoints(incremental.getKeyNegativePoints());

		// 요약과 추천도 증분 분석 결과 우선 사용
		mergedDTO.setSummary(incremental.getSummary());
		mergedDTO.setRecommendations(incremental.getRecommendations());
		mergedDTO.setReviewCategories(mergedCategories);

		return mergedDTO;
	}

	/**
	 * 분석 결과를 MySQL에 저장
	 */
	@Transactional
	public ReviewAnalysis saveReviewAnalysis(ReviewAnalysisResponseDTO dto, String reviewTexts) {
		log.info("Saving review analysis to MySQL for product: {}", dto.getProductCode());

		// 기존 분석 결과 조회
		Optional<ReviewAnalysis> existingAnalysis =
			reviewAnalysisRepository.findByProductCode(dto.getProductCode());

		// 감정 분석 데이터 추출
		Map<String, Double> sentimentMap = dto.getSentimentAnalysis();
		Double positive = sentimentMap.getOrDefault("positive", 0.0);
		Double negative = sentimentMap.getOrDefault("negative", 0.0);
		Double neutral = sentimentMap.getOrDefault("neutral", 0.0);

		// 최신 리뷰 ID 가져오기
		Long latestReviewId = reviewRepository.findLatestReviewId(dto.getProductCode());

		ReviewAnalysis entity;

		if (existingAnalysis.isPresent()) {
			// 기존 분석 결과 업데이트
			entity = existingAnalysis.get();
			entity.setProductName(dto.getProductName());
			entity.setAverageRating(dto.getAverageRating());
			entity.setReviewCount(dto.getReviewCount());
			entity.setSummary(dto.getSummary());
			entity.setSentimentPositive(positive);
			entity.setSentimentNegative(negative);
			entity.setSentimentNeutral(neutral);
			entity.setKeyPositivePointsJson(JsonUtils.toJson(dto.getKeyPositivePoints()));
			entity.setKeyNegativePointsJson(JsonUtils.toJson(dto.getKeyNegativePoints()));
			entity.setReviewCategoriesJson(JsonUtils.toJson(dto.getReviewCategories()));
			entity.setRecommendationsJson(JsonUtils.toJson(dto.getRecommendations()));
			entity.setLastReviewId(latestReviewId);
			entity.setUpdatedAt(LocalDateTime.now());

			log.info("Updated existing analysis for product: {}", dto.getProductCode());
		} else {
			// 새 분석 결과 생성
			entity = ReviewAnalysis.builder()
				.productCode(dto.getProductCode())
				.productName(dto.getProductName())
				.averageRating(dto.getAverageRating())
				.reviewCount(dto.getReviewCount())
				.summary(dto.getSummary())
				.sentimentPositive(positive)
				.sentimentNegative(negative)
				.sentimentNeutral(neutral)
				.keyPositivePointsJson(JsonUtils.toJson(dto.getKeyPositivePoints()))
				.keyNegativePointsJson(JsonUtils.toJson(dto.getKeyNegativePoints()))
				.reviewCategoriesJson(JsonUtils.toJson(dto.getReviewCategories()))
				.recommendationsJson(JsonUtils.toJson(dto.getRecommendations()))
				.lastReviewId(latestReviewId)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

			log.info("Created new analysis for product: {}", dto.getProductCode());
		}

		return reviewAnalysisRepository.save(entity);
	}

	/**
	 * 엔티티를 DTO로 변환
	 */
	private ReviewAnalysisResponseDTO convertEntityToDTO(ReviewAnalysis entity) {
		// sentimentAnalysis 맵 생성
		Map<String, Double> sentimentMap = new HashMap<>();
		sentimentMap.put("positive", entity.getSentimentPositive());
		sentimentMap.put("negative", entity.getSentimentNegative());
		sentimentMap.put("neutral", entity.getSentimentNeutral());

		return ReviewAnalysisResponseDTO.builder()
			.productCode(entity.getProductCode())
			.productName(entity.getProductName())
			.averageRating(entity.getAverageRating())
			.reviewCount(entity.getReviewCount())
			.summary(entity.getSummary())
			.sentimentAnalysis(sentimentMap)
			.keyPositivePoints(JsonUtils.parseJsonToStringList(entity.getKeyPositivePointsJson()))
			.keyNegativePoints(JsonUtils.parseJsonToStringList(entity.getKeyNegativePointsJson()))
			.reviewCategories(JsonUtils.parseJsonToCategories(entity.getReviewCategoriesJson()))
			.recommendations(JsonUtils.parseJsonToStringList(entity.getRecommendationsJson()))
			.build();
	}

	/**
	 * 일반 AI 분석 요청 메서드
	 */
	private String requestAiAnalysis(String productName, String reviewTexts) {
		log.info("Requesting AI analysis for product: {}", productName);

		// 시스템 메시지 생성
		SystemMessage systemMessage = new SystemMessage(
			"당신은 제품 리뷰 분석 전문가입니다. 사용자가 제공하는 제품 리뷰들을 분석하여 다음 정보를 JSON 형식으로 반환해야하고, ```json 등의 마크다운 코드는 포함하지 마세요:\n" +
				"1. sentimentAnalysis: 긍정/부정/중립 감정 비율 (예: {\"positive\": 0.7, \"negative\": 0.2, \"neutral\": 0.1})\n" +
				"2. keyPositivePoints: 주요 긍정적 포인트 리스트 (최대 5개)\n" +
				"3. keyNegativePoints: 주요 부정적 포인트 리스트 (최대 5개)\n" +
				"4. summary: 전체 리뷰 요약 (100자 내외)\n" +
				"5. recommendations: 구매자에게 도움이 될 추천사항 리스트 (최대 3개)\n" +
				"6. reviewCategories: 다음 5가지 카테고리로 리뷰를 분류하고 각 카테고리별 언급 횟수를 계산하세요: \n" +
				"   - {\"category\": \"맛/품질\", \"count\": 0, \"emoji\": \"😋\"}\n" +
				"   - {\"category\": \"가성비\", \"count\": 0, \"emoji\": \"💰\"}\n" +
				"   - {\"category\": \"신선도\", \"count\": 0, \"emoji\": \"🌱\"}\n" +
				"   - {\"category\": \"양/크기\", \"count\": 0, \"emoji\": \"🍽️\"}\n" +
				"   - {\"category\": \"주차편의성\", \"count\": 0, \"emoji\": \"🅿️\"}\n" +
				"JSON 형식으로 응답해야 합니다. 다른 텍스트나 설명은 포함하지 마세요."
		);

		// 사용자 메시지 생성
		UserMessage userMessage = new UserMessage(
			"다음은 '" + productName + "' 제품에 대한 사용자 리뷰입니다. 분석해주세요:\n\n" + reviewTexts
		);

		// 프롬프트 생성
		List<Message> messages = Arrays.asList(systemMessage, userMessage);
		Prompt prompt = new Prompt(messages);

		try {
			// AI 호출
			return chatClient.call(prompt).getResult().getOutput().getContent();
		} catch (Exception e) {
			log.error("Error during AI analysis: {}", e.getMessage(), e);
			throw new RuntimeException("AI 분석 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private ReviewAnalysisResponseDTO createEmptyResponse(Product product) {
		// 감정 분석을 위한 빈 맵 생성
		Map<String, Double> emptyMap = new HashMap<>();
		emptyMap.put("positive", 0.0);
		emptyMap.put("negative", 0.0);
		emptyMap.put("neutral", 0.0);

		return ReviewAnalysisResponseDTO.builder()
			.productCode(product.getProductCode())
			.productName(product.getProductName())
			.reviewCount(0)
			.averageRating(0)
			.summary("아직 리뷰가 없습니다.")
			.sentimentAnalysis(emptyMap)
			.keyPositivePoints(Collections.emptyList())
			.keyNegativePoints(Collections.emptyList())
			.reviewCategories(Collections.emptyList())
			.recommendations(Collections.emptyList())
			.build();
	}

	private ReviewAnalysisResponseDTO parseAiResponse(String jsonResponse) {
		try {
			log.info("AI 응답 원문: {}", jsonResponse);
			log.info("Parsing AI response");
			ReviewAnalysisResponseDTO dto = new ReviewAnalysisResponseDTO();
			JSONObject json = new JSONObject(jsonResponse);

			// 감정 분석
			if (json.has("sentimentAnalysis")) {
				JSONObject sentiment = json.getJSONObject("sentimentAnalysis");
				Map<String, Double> sentimentMap = new HashMap<>();
				for (String key : sentiment.keySet()) {
					sentimentMap.put(key, sentiment.getDouble(key));
				}
				dto.setSentimentAnalysis(sentimentMap);
			}

			// 긍정적 포인트
			if (json.has("keyPositivePoints")) {
				JSONArray positivePoints = json.getJSONArray("keyPositivePoints");
				List<String> positiveList = new ArrayList<>();
				for (int i = 0; i < positivePoints.length(); i++) {
					positiveList.add(positivePoints.getString(i));
				}
				dto.setKeyPositivePoints(positiveList);
			}

			// 부정적 포인트
			if (json.has("keyNegativePoints")) {
				JSONArray negativePoints = json.getJSONArray("keyNegativePoints");
				List<String> negativeList = new ArrayList<>();
				for (int i = 0; i < negativePoints.length(); i++) {
					negativeList.add(negativePoints.getString(i));
				}
				dto.setKeyNegativePoints(negativeList);
			}

			// 요약
			if (json.has("summary")) {
				dto.setSummary(json.getString("summary"));
			}

			// 추천 사항
			if (json.has("recommendations")) {
				JSONArray recommendations = json.getJSONArray("recommendations");
				List<String> recommendationsList = new ArrayList<>();
				for (int i = 0; i < recommendations.length(); i++) {
					recommendationsList.add(recommendations.getString(i));
				}
				dto.setRecommendations(recommendationsList);
			}

			// 리뷰 카테고리 파싱
			if (json.has("reviewCategories")) {
				JSONArray categories = json.getJSONArray("reviewCategories");
				List<ReviewCategoryDTO> categoryList = new ArrayList<>();

				for (int i = 0; i < categories.length(); i++) {
					JSONObject category = categories.getJSONObject(i);
					ReviewCategoryDTO categoryDTO = new ReviewCategoryDTO(
						category.getString("category"),
						category.getInt("count"),
						category.has("emoji") ? category.getString("emoji") : ""
					);
					categoryList.add(categoryDTO);
				}

				dto.setReviewCategories(categoryList);
			}

			return dto;
		} catch (Exception e) {
			log.error("Error parsing AI response: {}", e.getMessage(), e);
			throw new RuntimeException("AI 응답 파싱 중 오류 발생: " + e.getMessage());
		}
	}
}