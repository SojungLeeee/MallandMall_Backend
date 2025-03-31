package com.exam.Cart;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exam.product.Product;
import com.exam.product.ProductRepository;

@Service
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ProductRepository productRepository;

	public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
		this.cartRepository = cartRepository;
		this.productRepository = productRepository;
	}

	@Override
	public List<Cart> getCartItems(String userId) {
		List<Cart> cartItems = cartRepository.findByUserId(userId);

		for (Cart cart : cartItems) {
			Product product = productRepository.findByProductCode(cart.getProductCode());
			if (product != null) {
				cart.setProductName(product.getProductName()); // 상품명 추가
				cart.setPrice(product.getPrice()); // 가격 추가
				cart.setImage(product.getImage()); // 이미지 추가
			}
		}

		return cartItems;
	}

	@Override
	public Cart addToCart(CartDTO cartDTO) {
		System.out.println("🛒 addToCart 호출됨: " + cartDTO);

		Cart existingCart = cartRepository.findByUserIdAndProductCode(cartDTO.getUserId(), cartDTO.getProductCode());

		if (existingCart != null) {  // 기존 상품이 존재하면 업데이트
			existingCart.setQuantity(cartDTO.getQuantity());
			System.out.println("📌 기존 상품 업데이트: " + existingCart);
			return cartRepository.save(existingCart);
		} else {  // 기존 상품이 없으면 새로 추가
			Cart cart = new Cart();
			cart.setUserId(cartDTO.getUserId());
			cart.setProductCode(cartDTO.getProductCode());
			cart.setQuantity(cartDTO.getQuantity());
			System.out.println("📌 새로운 상품 추가: " + cart);
			return cartRepository.save(cart);
		}
	}

	@Override
	@Transactional // 🔥 트랜잭션 추가!
	public void removeFromCart(String userId, String productCode) {
		Cart cart = cartRepository.findByUserIdAndProductCode(userId, productCode);
		if (cart != null) {
			cartRepository.delete(cart);
		} else {
			throw new RuntimeException("장바구니에 해당 상품이 없습니다.");
		}
	}

	public Cart updateCartItemQuantity(String userId, String productCode, int quantity) {
		Cart cartItem = cartRepository.findByUserIdAndProductCode(userId, productCode);

		if (cartItem == null) {
			throw new RuntimeException("장바구니에 해당 상품이 없습니다.");
		}

		if (quantity <= 0) {
			cartRepository.deleteByUserIdAndProductCode(userId, productCode); // ✅ 바로 삭제!
			return null;
		}

		cartItem.setQuantity(quantity);
		return cartRepository.save(cartItem);
	}

	// 여러 개의 cartId를 받아서 삭제하는 메서드
	@Override
	@Transactional
	public void deleteByCartIdIn(List<Integer> cartIds) {
		// cartIds 리스트에 해당하는 cartId들을 가진 장바구니 항목들을 삭제
		cartRepository.deleteByCartIdIn(cartIds);
	}
}
