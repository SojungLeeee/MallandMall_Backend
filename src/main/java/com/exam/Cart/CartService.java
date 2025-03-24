package com.exam.Cart;

import java.util.List;

public interface CartService {
	List<Cart> getCartItems(String userId); // 장바구니 조회
	Cart addToCart(CartDTO cartDTO); // 장바구니 추가
	void removeFromCart(String userId, String productCode); // 장바구니 삭제

	// 🛒 장바구니 상품 수량 변경 (추가된 부분)
	Cart updateCartItemQuantity(String userId, String productCode, int quantity);
}
