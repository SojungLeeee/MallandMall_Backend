package com.exam.inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {

	InventoryDTO findByProductCodeAndBranchName(String productCode, String branchName);

	List<InventoryDTO> findAllInventory();

	List<InventoryDTO> findByProductCode(String productCode);

	// 지점별 특정 상품 수량 조회 메소드
	Map<String, Integer> findQuantityByProductCode(String productCode);
	// 재고 업데이트 메서드
	boolean updateInventory(String branchName, String productCode, int quantityChange);
}
