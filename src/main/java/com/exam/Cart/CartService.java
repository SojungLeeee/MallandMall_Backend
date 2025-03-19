package com.exam.Cart;

import java.util.List;

public interface CartService {
	List<Cart> getCartItems(String userId); // 장바구니 조회
	Cart addToCart(CartDTO cartDTO); // 장바구니 추가
	void removeFromCart(String userId, String productCode);
}
