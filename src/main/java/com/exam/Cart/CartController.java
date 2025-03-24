package com.exam.Cart;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	// 현재 인증된 사용자의 ID 가져오기
	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
		}
		return authentication.getName(); // JWT에서 추출된 사용자 ID
	}

	// 장바구니 조회
	@GetMapping("/items")
	public List<Cart> getCartItems() {
		String userId = getAuthenticatedUserId();  // JWT에서 사용자 ID 가져오기
		return cartService.getCartItems(userId);
	}




	// 장바구니에 상품 추가
	@PostMapping("/add")
	public Cart addToCart(@RequestBody CartDTO cartDTO) {
		String userId = getAuthenticatedUserId();
		cartDTO.setUserId(userId);
		return cartService.addToCart(cartDTO);
	}


	//삭제하기
	@DeleteMapping("/{productCode}")
	public ResponseEntity<?> removeFromCart(@PathVariable String productCode) {
		String userId = getAuthenticatedUserId();  // 현재 로그인한 사용자 ID 가져오기
		cartService.removeFromCart(userId, productCode);
		return ResponseEntity.ok("상품 삭제 완료");
	}


	//수량증가
	// 장바구니 상품 수량 변경
	@PatchMapping("/{productCode}")
	public ResponseEntity<Cart> updateCartItemQuantity(
		@PathVariable String productCode,
		@RequestParam int quantity) {

		String userId = getAuthenticatedUserId();  // 현재 로그인한 사용자 ID 가져오기
		Cart updatedCartItem = cartService.updateCartItemQuantity(userId, productCode, quantity);

		return ResponseEntity.ok(updatedCartItem);
	}

}
