package com.exam.search.keyword;

import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RealTimeKeywordService {
	private final SearchKeywordRepository keywordRepository;
	private final KeywordProductMappingRepository mappingRepository;

	// 이전 순위 캐싱을 위한 맵
	private final Map<String, Integer> previousRanks = new HashMap<>();
	private LocalDateTime lastUpdatedTime = LocalDateTime.now();

	public RealTimeKeywordService(SearchKeywordRepository keywordRepository,
		KeywordProductMappingRepository mappingRepository) {
		this.keywordRepository = keywordRepository;
		this.mappingRepository = mappingRepository;
	}

	/**
	 * 실시간 인기 검색어 목록을 반환합니다.
	 */
	public List<Map<String, Object>> getTrendingKeywords(int limit) {
		List<Map<String, Object>> result = new ArrayList<>();

		// 상위 검색어 조회
		List<SearchKeyword> topKeywords = keywordRepository.findTopKeywords(PageRequest.of(0, limit));

		int rank = 1;
		for (SearchKeyword keyword : topKeywords) {
			Map<String, Object> keywordInfo = new HashMap<>();
			keywordInfo.put("rank", rank);
			keywordInfo.put("keyword", keyword.getKeyword());
			keywordInfo.put("count", keyword.getSearchCount());

			// 이전 순위와 비교하여 변동 계산
			int previousRank = previousRanks.getOrDefault(keyword.getKeyword(), 0);
			int rankChange = previousRank == 0 ? 0 : previousRank - rank;
			keywordInfo.put("rankChange", rankChange);

			result.add(keywordInfo);
			rank++;
		}

		return result;
	}

	/**
	 * 검색어를 기록하고 카운트를 증가시킵니다.
	 */
	@Transactional
	public void recordKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return;
		}

		String cleanKeyword = keyword.trim();

		// 기존 검색어가 있는지 확인
		Optional<SearchKeyword> existingKeyword = keywordRepository.findByKeyword(cleanKeyword);

		if (existingKeyword.isPresent()) {
			// 기존 검색어 카운트 증가
			SearchKeyword keywordEntity = existingKeyword.get();
			keywordEntity.setSearchCount(keywordEntity.getSearchCount() + 1);
			keywordRepository.save(keywordEntity);
			log.debug("검색어 '{}' 카운트 증가: {}", cleanKeyword, keywordEntity.getSearchCount());
		} else {
			// 새 검색어 저장
			SearchKeyword newKeyword = new SearchKeyword();
			newKeyword.setKeyword(cleanKeyword);
			keywordRepository.save(newKeyword);
			log.debug("새 검색어 '{}' 저장", cleanKeyword);
		}
	}

	/**
	 * 검색어와 상품 클릭 정보를 기록합니다.
	 */
	@Transactional
	public void recordKeywordWithProduct(String keyword, String productCode) {
		if (keyword == null || keyword.trim().isEmpty() || productCode == null) {
			return;
		}

		String cleanKeyword = keyword.trim();

		// 먼저 검색어 기록/업데이트
		recordKeyword(cleanKeyword);

		// 검색어 엔티티 조회
		SearchKeyword keywordEntity = keywordRepository.findByKeyword(cleanKeyword)
			.orElseThrow(() -> new RuntimeException("검색어 저장 후에도 찾을 수 없음: " + cleanKeyword));

		// 기존 매핑이 있는지 확인
		Optional<KeywordProductMapping> existingMapping =
			mappingRepository.findBySearchKeyword_KeywordIdAndProductCode(keywordEntity.getKeywordId(), productCode);

		if (existingMapping.isPresent()) {
			// 기존 매핑 클릭 카운트 증가
			KeywordProductMapping mapping = existingMapping.get();
			mapping.setClickCount(mapping.getClickCount() + 1);
			mappingRepository.save(mapping);
			log.debug("검색어 '{}', 상품 코드 '{}' 매핑 카운트 증가: {}",
				cleanKeyword, productCode, mapping.getClickCount());
		} else {
			// 새 매핑 저장
			KeywordProductMapping newMapping = new KeywordProductMapping();
			newMapping.setSearchKeyword(keywordEntity);
			newMapping.setProductCode(productCode);
			mappingRepository.save(newMapping);
			log.debug("새 매핑 저장: 검색어 '{}', 상품 코드 '{}'", cleanKeyword, productCode);
		}
	}

	/**
	 * 특정 검색어와 관련된 상위 상품 코드 목록을 반환합니다.
	 */
	public List<String> getRelatedProductCodes(String keyword, int limit) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return Collections.emptyList();
		}

		String cleanKeyword = keyword.trim();

		// 관련 상품 조회
		List<KeywordProductMapping> mappings =
			mappingRepository.findByKeywordOrderByClickCountDesc(cleanKeyword, PageRequest.of(0, limit));

		return mappings.stream()
			.map(KeywordProductMapping::getProductCode)
			.collect(Collectors.toList());
	}

	/**
	 * 1시간마다 실행되어 순위 변동을 계산합니다.
	 */
	@Scheduled(fixedRate = 3600000) // 1시간마다 실행
	public void updateRankings() {
		log.info("실시간 검색어 순위 업데이트 실행");

		// 현재 순위를 이전 순위로 저장
		previousRanks.clear();
		List<Map<String, Object>> currentRanking = getTrendingKeywords(100);

		for (Map<String, Object> keyword : currentRanking) {
			previousRanks.put((String) keyword.get("keyword"), (Integer) keyword.get("rank"));
		}

		lastUpdatedTime = LocalDateTime.now();
		log.info("실시간 검색어 순위 업데이트 완료: {}",
			lastUpdatedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	}
}