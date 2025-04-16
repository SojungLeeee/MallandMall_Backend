package com.exam.search;

import java.util.*;

import org.springframework.stereotype.Service;

import com.exam.review.ai.vector.ReviewAnalysisVectorRepository;
import com.exam.review.ai.vector.ReviewAnalysisVectorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchRankingVectorService {
	private final ReviewAnalysisVectorService vectorService;
	private final ReviewAnalysisVectorRepository vectorRepository;

	// 실시간으로 사용자 입력에 가장 유사한 제품 찾기
	public List<Map<String, Object>> getRealTimeSuggestions(String userInput, int limit) {
		// 사용자 입력을 임베딩으로 변환
		float[] inputEmbedding = vectorService.createEmbedding(userInput);
		String vectorStr = vectorService.formatVector(inputEmbedding);

		// 요약 임베딩과의 유사도 기반 검색
		List<Object[]> results = vectorRepository.findSimilarByEmbedding(vectorStr, limit);

		// 결과를 표시용 형식으로 변환
		return convertToRankingResults(results);
	}

	// 특정 감정/특성 기반 순위 제공
	public List<Map<String, Object>> getTrendingByFeature(String feature, boolean isPositive, int limit) {
		List<Object[]> results;
		float[] featureEmbedding = vectorService.createEmbedding(feature);
		String vectorStr = vectorService.formatVector(featureEmbedding);

		if (isPositive) {
			// 긍정적 특성 기반 검색
			results = vectorRepository.findSimilarByPositiveEmbedding(vectorStr, limit);
		} else {
			// 부정적 특성 기반 검색
			results = vectorRepository.findSimilarByNegativeEmbedding(vectorStr, limit);
		}

		return convertToRankingResults(results);
	}

	// 결과를 순위 정보가 포함된 형식으로 변환
	private List<Map<String, Object>> convertToRankingResults(List<Object[]> results) {
		List<Map<String, Object>> rankings = new ArrayList<>();
		int rank = 1;

		for (Object[] result : results) {
			Map<String, Object> item = new HashMap<>();
			item.put("rank", rank++);
			item.put("analysis_id", result[0]);
			item.put("product_code", result[1]);
			item.put("product_name", result[2]); // 상품명 추가
			item.put("similarity", result[3]); // 인덱스가 하나 증가했으므로 유사도는 3번 인덱스에 있음

			// 임의의 순위 변동 (실제로는 이전 검색 결과와 비교해야 함)
			double randomChange = Math.random() * 6 - 3; // -3 ~ +3
			item.put("rankChange", (int)randomChange);

			rankings.add(item);
		}

		return rankings;
	}
}