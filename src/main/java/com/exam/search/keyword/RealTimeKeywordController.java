package com.exam.search.keyword;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class RealTimeKeywordController {
	private final RealTimeKeywordService keywordService;

	/**
	 * 실시간 인기 검색어 목록 조회
	 */
	@GetMapping("/trending-keywords")
	public ResponseEntity<List<Map<String, Object>>> getTrendingKeywords(
		@RequestParam(defaultValue = "10") int limit) {

		log.info("실시간 인기 검색어 요청, limit={}", limit);
		List<Map<String, Object>> keywords = keywordService.getTrendingKeywords(limit);

		return ResponseEntity.ok(keywords);
	}

	/**
	 * 검색어 등록 (검색 실행 시 호출됨)
	 */
	@PostMapping("/record-keyword")
	public ResponseEntity<Map<String, Object>> recordKeyword(
		@RequestBody Map<String, String> request) {

		String keyword = request.get("keyword");
		log.info("검색어 등록 요청: {}", keyword);

		keywordService.recordKeyword(keyword);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);

		return ResponseEntity.ok(response);
	}

	/**
	 * 검색어 및 상품 클릭 정보 등록
	 */
	@PostMapping("/record-keyword-product")
	public ResponseEntity<Map<String, Object>> recordKeywordWithProduct(
		@RequestBody Map<String, String> request) {

		String keyword = request.get("keyword");
		String productCode = request.get("productCode");

		log.info("검색어 및 상품 등록 요청: keyword={}, productCode={}", keyword, productCode);

		keywordService.recordKeywordWithProduct(keyword, productCode);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);

		return ResponseEntity.ok(response);
	}

	/**
	 * 검색어 관련 상품 조회
	 */
	@GetMapping("/related-products")
	public ResponseEntity<List<String>> getRelatedProducts(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "5") int limit) {

		log.info("검색어 관련 상품 조회 요청: keyword={}, limit={}", keyword, limit);
		List<String> productCodes = keywordService.getRelatedProductCodes(keyword, limit);

		return ResponseEntity.ok(productCodes);
	}
}