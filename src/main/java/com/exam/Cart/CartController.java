package com.exam.Cart;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

	//주문 완료 후 장바구니에 있던 물품 삭제시키기
	@DeleteMapping("/deleteAfterBuy")
	public ResponseEntity<String> deleteCarts(@RequestBody List<Integer> cartIds) {
		// cartIds를 사용하여 장바구니 항목 삭제
		try {
			cartService.deleteByCartIdIn(cartIds);
			return ResponseEntity.ok("장바구니 항목이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("장바구니 항목 삭제에 실패했습니다.");
		}
	}

}
