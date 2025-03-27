package com.exam.coupon;

public interface CouponService {

	//회원가입 시 쿠폰 저장
	void addNewMemberOnlineCoupon(String userId);

	void addNewMemberOfflineCoupon(String userId);
}