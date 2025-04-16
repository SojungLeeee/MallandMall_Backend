package com.exam.product.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

@Service
@Slf4j
public class ElasticsearchIndexingService {

	private final ElasticsearchClient esClient;
	private final JdbcTemplate jdbcTemplate;

	public ElasticsearchIndexingService(
		ElasticsearchClient esClient,
		@Qualifier("dataSource") DataSource dataSource) {
		this.esClient = esClient;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 단일 제품 인덱싱
	 */
	public void indexProduct(String productCode, String productName, String description, String category) {
		try {
			Map<String, Object> document = new HashMap<>();
			document.put("product_code", productCode);
			document.put("product_name", productName);
			document.put("description", description);
			document.put("category", category);
			document.put("updated_at", LocalDateTime.now());

			IndexRequest<Map<String, Object>> request = IndexRequest.of(r -> r
				.index("products")
				.id(productCode)
				.document(document)
			);

			IndexResponse response = esClient.index(request);
			log.info("제품 인덱싱 완료: {}, 결과: {}", productCode, response.result().toString());
		} catch (Exception e) {
			log.error("제품 인덱싱 중 오류: {}", e.getMessage(), e);
		}
	}

	/**
	 * 주기적으로 모든 제품 데이터 인덱싱 (초기 데이터 로드 및 동기화)
	 */
	@Scheduled(fixedRate = 86400000) // 24시간마다 실행
	public void indexAllProducts() {
		try {
			log.info("모든 제품 데이터 인덱싱 시작");

			// MySQL에서 제품 데이터 조회 (실제 테이블명과 컬럼명에 맞게 수정 필요)
			String sql = "SELECT productCode as product_code, productName as product_name, description, category, price, image FROM products";
			List<Map<String, Object>> products = jdbcTemplate.queryForList(sql);

			BulkRequest.Builder br = new BulkRequest.Builder();

			for (Map<String, Object> product : products) {
				String productCode = (String) product.get("product_code");

				br.operations(op -> op
					.index(idx -> idx
						.index("products")
						.id(productCode)
						.document(product)
					)
				);
			}

			BulkResponse result = esClient.bulk(br.build());

			if (result.errors()) {
				log.error("일부 제품 인덱싱 실패");
				result.items().forEach(item -> {
					if (item.error() != null) {
						log.error("인덱싱 오류: {}, 제품: {}", item.error().reason(), item.id());
					}
				});
			} else {
				log.info("모든 제품 인덱싱 완료 - 총 {}개 제품", products.size());
			}
		} catch (Exception e) {
			log.error("제품 일괄 인덱싱 중 오류: {}", e.getMessage(), e);
		}
	}

	@PostConstruct
	public void initialIndexing() {
		try {
			log.info("애플리케이션 시작 시 초기 인덱싱 실행");
			indexAllProducts();
		} catch (Exception e) {
			log.error("초기 인덱싱 중 오류 발생: {}", e.getMessage(), e);
		}
	}
}