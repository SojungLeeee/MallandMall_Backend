package com.exam.orderinfo;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderInfoController {

	private final OrderInfoService orderInfoService;

	public OrderInfoController(OrderInfoService orderInfoService) {
		this.orderInfoService = orderInfoService;
	}

	// 사용자 주문 내역 조회
	@GetMapping("/myorder")
	public ResponseEntity<List<OrderInfoDTO>> getMyOrders() {
		String userId = getAuthenticatedUserId(); // JWT에서 userId 가져오기
		List<OrderInfoDTO> orders = orderInfoService.getOrdersByUserId(userId);
		return ResponseEntity.ok(orders);
	}


	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}
}
