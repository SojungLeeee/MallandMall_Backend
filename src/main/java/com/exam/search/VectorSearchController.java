package com.exam.search;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/vector-search")
@RequiredArgsConstructor
@Slf4j
public class VectorSearchController {
	private final SearchRankingVectorService rankingService;

	@GetMapping("/suggestions")
	public ResponseEntity<List<Map<String, Object>>> getSuggestions(
		@RequestParam String query,
		@RequestParam(defaultValue = "5") int limit) {

		log.info("벡터 기반 서치바 추천 요청: {}", query);
		List<Map<String, Object>> results = rankingService.getRealTimeSuggestions(query, limit);

		return ResponseEntity.ok(results);
	}

	@GetMapping("/trending")
	public ResponseEntity<List<Map<String, Object>>> getTrendingByFeature(
		@RequestParam String feature,
		@RequestParam(defaultValue = "true") boolean positive,
		@RequestParam(defaultValue = "10") int limit) {

		log.info("특성 기반 트렌딩 제품 요청: {}, 긍정적: {}", feature, positive);
		List<Map<String, Object>> results = rankingService.getTrendingByFeature(feature, positive, limit);

		return ResponseEntity.ok(results);
	}

	@GetMapping("/combined-ranking")
	public ResponseEntity<List<Map<String, Object>>> getCombinedRanking(
		@RequestParam String query,
		@RequestParam(defaultValue = "") String feature,
		@RequestParam(defaultValue = "true") boolean positive,
		@RequestParam(defaultValue = "10") int limit) {

		// 쿼리와 특성을 결합한 검색 (가중치 적용 가능)
		// 실제 구현은 쿼리와 특성 임베딩을 결합하여 검색
		List<Map<String, Object>> results;

		if (feature.isEmpty()) {
			results = rankingService.getRealTimeSuggestions(query, limit);
		} else {
			// 여기서는 기본적으로 쿼리만 사용
			// 추후 쿼리와 특성 임베딩을 결합하여 사용하기 위해 고려
			results = rankingService.getRealTimeSuggestions(query + " " + feature, limit);
		}

		return ResponseEntity.ok(results);
	}
}