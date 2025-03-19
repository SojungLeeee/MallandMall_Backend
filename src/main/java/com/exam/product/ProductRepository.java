package com.exam.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
	List<Product> findByCategory(String category);
	List<Product> findByProductNameContaining(String productName);  // 상품 이름으로 검색

	// 🛠 상품 코드로 찾는 메서드 추가!
	Product findByProductCode(String productCode);
}
