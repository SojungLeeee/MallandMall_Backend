package com.exam.coupon;

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

}
