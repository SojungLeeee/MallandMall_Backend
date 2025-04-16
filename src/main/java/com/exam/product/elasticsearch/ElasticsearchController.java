package com.exam.product.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.product.ProductDTO;
import com.exam.product.ProductService;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/product")
@Slf4j
public class ElasticsearchController {
	private final ProductService productService;
	private final ElasticsearchSearchService elasticsearchSearchService;

	public ElasticsearchController(ProductService productService, ElasticsearchSearchService elasticsearchSearchService) {
		this.productService = productService;
		this.elasticsearchSearchService = elasticsearchSearchService;
	}

	//Elasticsearch 기능 추가
	@GetMapping("/elastic-search")
	public ResponseEntity<List<ProductDTO>> elasticSearch(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "10") int limit) {

		try {
			// Elasticsearch 서비스를 통해 검색 실행
			List<Map<String, Object>> searchResults = elasticsearchSearchService.searchProducts(keyword, limit);

			// 검색 결과가 없는 경우
			if (searchResults.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			// 검색 결과에서 제품 코드 추출
			List<String> productCodes = searchResults.stream()
				.map(result -> (String) result.get("product_code"))
				.collect(Collectors.toList());

			// 제품 서비스를 통해 상세 제품 정보 조회
			List<ProductDTO> products = productService.getProductsByProductCodes(productCodes);

			// 검색 결과의 순서대로 제품 정렬
			List<ProductDTO> sortedProducts = new ArrayList<>();
			for (String productCode : productCodes) {
				products.stream()
					.filter(p -> p.getProductCode().equals(productCode))
					.findFirst()
					.ifPresent(sortedProducts::add);
			}

			return ResponseEntity.ok(sortedProducts);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	// 스마트 검색 API 추가 (자동완성 및 추천)
	@GetMapping("/smart-search")
	public ResponseEntity<?> smartSearch(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "10") int limit) {

		try {
			// Elasticsearch 스마트 검색 실행
			Map<String, Object> result = elasticsearchSearchService.searchWithSuggestion(keyword, limit);

			// 검색 결과 처리
			List<Map<String, Object>> searchResults = (List<Map<String, Object>>) result.get("results");

			// 검색 결과가 없고 제안이 없는 경우
			if (searchResults.isEmpty() && !result.containsKey("suggestions")) {
				return ResponseEntity.noContent().build();
			}

			// 제품 코드 추출 및 정보 조회 로직
			if (!searchResults.isEmpty()) {
				List<String> productCodes = searchResults.stream()
					.map(r -> (String) r.get("product_code"))
					.collect(Collectors.toList());

				List<ProductDTO> products = productService.getProductsByProductCodes(productCodes);
				result.put("products", products); // 검색 결과에 상세 제품 정보 추가
			}

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> errorResponse = Map.of(
				"error", true,
				"message", "검색 처리 중 오류가 발생했습니다."
			);
			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	// 검색어 제안 API 추가
	@GetMapping("/suggestions")
	public ResponseEntity<List<String>> getSuggestions(
		@RequestParam String query) {

		try {
			log.info("검색어 제안 요청: {}", query);

			// 오타 교정 쿼리 구성 - 간소화된 방식으로 작성
			Query suggestionQuery = Query.of(q -> q
				.fuzzy(f -> f
					.field("product_name")
					.value(query)
					.fuzziness("AUTO")
				)
			);

			SearchRequest suggestionRequest = SearchRequest.of(r -> r
				.index("products")
				.query(suggestionQuery)
				.size(5)
			);

			SearchResponse<Map> suggestionResponse = elasticsearchSearchService.getElasticsearchClient().search(suggestionRequest, Map.class);

			List<String> suggestions = new ArrayList<>();
			for (Hit<Map> hit : suggestionResponse.hits().hits()) {
				Map<String, Object> source = hit.source();
				if (source != null && source.containsKey("product_name")) {
					suggestions.add((String) source.get("product_name"));
				}
			}

			return ResponseEntity.ok(suggestions);
		} catch (Exception e) {
			log.error("검색어 제안 처리 중 오류: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body(new ArrayList<>());
		}
	}
}
