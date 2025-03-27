package com.exam.coupon;

import java.time.LocalDate;
import java.util.List;

public interface CouponService {

	//회원가입 시 쿠폰 저장
	void addNewMemberOnlineCoupon(String userId);

	void addNewMemberOfflineCoupon(String userId);

	//userid로 쿠폰 찾기
	List<CouponDTO> findByUserId(String userId);

	//쿠폰 자동삭제 로직
	void deleteExpiredCoupons(LocalDate now);
}