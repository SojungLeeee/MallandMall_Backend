package com.exam.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.exam.product.Product;

public interface CartRepository extends JpaRepository<Cart, Long> {

	// 특정 사용자의 장바구니 목록 조회
	List<Cart> findByUserId(String userId);
	void deleteByUserIdAndProductCode(String userId, String productCode);
	// 특정 사용자의 장바구니에서 특정 상품 찾기
	Cart findByUserIdAndProductCode(String userId, String productCode);
}