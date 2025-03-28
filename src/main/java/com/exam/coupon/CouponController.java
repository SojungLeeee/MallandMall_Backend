package com.exam.coupon;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupon")
public class CouponController {

	CouponService couponService;

	public CouponController(CouponService couponService) {
		this.couponService = couponService;
	}

	//새로 가입시 온라인 쿠폰 발급
	@GetMapping("/newMemberOnlineCoupon/{userId}")
	public void newMemberOnlineCoupon(@PathVariable String userId) {
		couponService.addNewMemberOnlineCoupon(userId);
	}

	//새로 가입시 오프라인 쿠폰 발급
	@GetMapping("/newMemberOfflineCoupon/{userId}")
	public void newMemberOfflineCoupon(@PathVariable String userId) {
		couponService.addNewMemberOfflineCoupon(userId);
	}

	//userId로 쿠폰 list 가져오기
	@GetMapping("/list")
	public ResponseEntity<?> findAllCouponsByUserId() {
		String userId = getAuthenticatedUserId(); // JWT에서 userId 가져오기
		if (userId == null) {
			return ResponseEntity.status(401).body("인증되지 않은 사용자");
		}
		List<CouponDTO> couponDTOList = couponService.findByUserId(userId);
		return ResponseEntity.status(200).body(couponDTOList);
	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

	//월초 쿠폰 발급 / 얘는 지금은 없어도 되는건가..?
	//@GetMapping("/monthReward/{userId}")
	// public void newMonthCoupon(@PathVariable String userId) {
	// 	couponService.addMonthCoupon(userId);
	// }

}
