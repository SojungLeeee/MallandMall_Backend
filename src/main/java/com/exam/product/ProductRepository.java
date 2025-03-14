package com.exam.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {

	List<Product> findByCategory(String category);

	List<Product> findByProductName(String productName);  // 상품 이름으로 검색
}
