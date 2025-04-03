package com.exam.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

	// 지점 + 상품번호로 조회
	List<InventoryLog> findByProductCodeAndBranchNameOrderByChangeDateAsc(String productCode, String branchName);

	// 상품 번호로만 조회
	List<InventoryLog> findByProductCodeOrderByChangeDateAsc(String productCode);




	// 마이그레이션용
	List<InventoryLog> findAllByOrderByChangeDateAsc();
}