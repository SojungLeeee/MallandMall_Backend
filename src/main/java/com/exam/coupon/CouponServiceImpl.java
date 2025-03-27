package com.exam.coupon;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class CouponServiceImpl implements CouponService {

	CouponRepository couponRepository;

	public CouponServiceImpl(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	// 쿠폰 생성 공통 메소드
	private Coupon createCoupon(String userId, String couponName, String minPrice, String benefits, String couponType) {
		// 오늘 날짜에서 한 달 후의 날짜 계산
		LocalDate expirationDate = LocalDate.now().plusMonths(1);  // 오늘 날짜 기준으로 한 달 후

		// Coupon 객체 생성
		return Coupon.builder()
			.userId(userId)  // 회원의 userId를 전달받아서 설정
			.couponName(couponName)  // 쿠폰명
			.minPrice(minPrice)  // 최소 구매 금액
			.expirationDate(expirationDate)  // 한 달 후의 유효 기간
			.benefits(benefits)  // 쿠폰 혜택
			.couponType(couponType)
			.build();
	}

	// 온라인 쿠폰 발급
	@Override
	public void addNewMemberOnlineCoupon(String userId) {
		String couponName = "[온라인] 전상품 20% 할인 쿠폰";
		String minPrice = "30000";
		String benefits = "20% 할인";
		String couponType = "online";

		Coupon coupon = createCoupon(userId, couponName, minPrice, benefits, couponType);
		couponRepository.save(coupon);
	}

	// 오프라인 쿠폰 발급
	@Override
	public void addNewMemberOfflineCoupon(String userId) {
		String couponName = "[오프라인] 전상품 30% 할인 쿠폰";
		String minPrice = "30000";
		String benefits = "30% 할인";
		String couponType = "offline";

		Coupon coupon = createCoupon(userId, couponName, minPrice, benefits, couponType);
		couponRepository.save(coupon);
	}

}
