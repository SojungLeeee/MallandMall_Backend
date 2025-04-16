package com.exam.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.product.elasticsearch.ElasticsearchSearchService;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {

	private final ProductService productService;
	private final ElasticsearchSearchService elasticsearchSearchService;

	public ProductController(ProductService productService, ElasticsearchSearchService elasticsearchSearchService) {
		this.productService = productService;
		this.elasticsearchSearchService = elasticsearchSearchService;
	}

	//  전체 상품 목록 조회
	@GetMapping("/home")
	public ResponseEntity<List<ProductDTO>> getAllProducts() {
		List<ProductDTO> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	//  특정 상품 상세 조회
	@GetMapping("/detail/{productCode}")
	public ResponseEntity<ProductDTO> getProductDetail(@PathVariable String productCode) {
		ProductDTO product = productService.getProductByCode(productCode);
		return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
	}

	//카테고리별 상품 조회
	@GetMapping("/{category}")
	public ResponseEntity<List<ProductDTO>> getCategoryProducts(@PathVariable String category) {
		List<ProductDTO> products = productService.getProductsByCategory(category);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/search/{productName}")
	public ResponseEntity<List<ProductDTO>> searchProductsByName(@PathVariable String productName) {
		List<ProductDTO> products = productService.getProductsByName(productName);
		return (products.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

	@GetMapping("/likecategories")
	public ResponseEntity<?> getAllProductsByCategory() {
		Authentication authentication =
			SecurityContextHolder.getContext().getAuthentication();

		String userId = authentication.getName();
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}
		System.out.println(userId);

		List<ProductDTO> productDTOs = productService.getProductsByUserId(userId);
		return ResponseEntity.ok(productDTOs);
	}

	// 다중 상품 코드로 상품 정보 조회 API 추가
	@GetMapping("/multiple")
	public ResponseEntity<List<ProductDTO>> getMultipleProducts(
		@RequestParam("productCodes") String productCodesStr) {

		// 콤마로 구분된 상품 코드 문자열을 리스트로 변환
		List<String> productCodes = Arrays.asList(productCodesStr.split(","))
			.stream()
			.map(String::trim)
			.filter(code -> !code.isEmpty())
			.collect(Collectors.toList());

		// 상품 코드 목록이 비어있으면 빈 목록 반환
		if (productCodes.isEmpty()) {
			return ResponseEntity.ok(Collections.emptyList());
		}

		// 서비스를 통해 여러 상품 정보 조회
		List<ProductDTO> products = productService.getProductsByProductCodes(productCodes);

		if (products.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(products);
	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductDTO>> searchProducts(
		@RequestParam String searchType,
		@RequestParam String keyword) {

		List<ProductDTO> products;

		if ("productName".equals(searchType)) {
			// 기존의 상품명으로 검색하는 서비스 메소드 활용
			products = productService.getProductsByName(keyword);
		} else if ("category".equals(searchType)) {
			// 기존의 카테고리별 상품 조회 서비스 메소드 활용
			products = productService.getProductsByCategory(keyword);
		} else {
			// 유효하지 않은 검색 타입인 경우
			return ResponseEntity.badRequest().build();
		}

		return (products.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

	@GetMapping
	public ResponseEntity<List<Product>> getProducts(@RequestParam(defaultValue = "default") String sort) {
		List<Product> products = productService.getProductsSorted(sort);
		return ResponseEntity.ok(products);
	}


}