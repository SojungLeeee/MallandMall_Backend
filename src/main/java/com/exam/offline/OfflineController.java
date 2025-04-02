package com.exam.offline;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.product.ProductDTO;

@RestController
@RequestMapping("/offprice")
public class OfflineController {


	private final OfflineService offlineService;


	public OfflineController(OfflineService offlineService) {
		this.offlineService = offlineService;
	}



	// 사용자의 선호 카테고리 기반으로 할인된 상품 목록 조회
	@GetMapping("/discount")
	public ResponseEntity<List<ProductDTO>> getDiscountedProducts() {
		// SecurityContext에서 userId 추출
		String userId = (String) SecurityContextHolder.getContext().getAuthentication().getName();

		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		// 해당 userId의 선호 카테고리 기반 할인 상품을 조회하는 서비스 호출
		List<ProductDTO> discountedProducts = offlineService.getDiscountedProductsByUserCategory(userId);

		// 할인된 상품 목록을 반환
		return ResponseEntity.ok(discountedProducts);
	}

	// 상품 코드로 가격 변동 이력 조회
	@GetMapping("/history/{productCode}")
	public List<OfflinePriceDTO> getPriceHistory(@PathVariable String productCode) {
		return offlineService.getPriceHistory(productCode);
	}
}