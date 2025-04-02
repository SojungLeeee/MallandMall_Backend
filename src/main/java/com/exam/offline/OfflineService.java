package com.exam.offline;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exam.product.ProductDTO;

public interface OfflineService {
	List<OfflinePriceDTO> getPriceHistory(String productCode);

	List<ProductDTO> getDiscountedProductsByUserCategory(String userId);
	// 사용자의 선호 카테고리에 맞는 할인된 상품 조회


}
