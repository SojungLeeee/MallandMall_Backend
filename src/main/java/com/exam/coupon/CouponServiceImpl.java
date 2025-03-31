package com.exam.coupon;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		String couponName = "[온라인] 5% 할인 쿠폰";
		String minPrice = "30000";
		String benefits = "5% 할인";
		String couponType = "online";

		Coupon coupon = createCoupon(userId, couponName, minPrice, benefits, couponType);
		couponRepository.save(coupon);
	}

	// 오프라인 쿠폰 발급
	@Override
	public void addNewMemberOfflineCoupon(String userId) {
		String couponName = "[오프라인] 10% 할인 쿠폰";
		String minPrice = "30000";
		String benefits = "10% 할인";
		String couponType = "offline";

		Coupon coupon = createCoupon(userId, couponName, minPrice, benefits, couponType);
		couponRepository.save(coupon);
	}

	//userId로 쿠폰 list 받아오기
	@Override
	public List<CouponDTO> findByUserId(String userId) {
		List<Coupon> couponList = couponRepository.findByUserId(userId);
		//Stream API 의 Map 이용
		List<CouponDTO> couponDTOList =
			couponList.stream().map(c -> { //c는 Coupon
				CouponDTO dto = CouponDTO.builder()
					.couponId(c.getCouponId())
					.userId(c.getUserId())
					.couponName(c.getCouponName())
					.minPrice(c.getMinPrice())
					.expirationDate(c.getExpirationDate())
					.benefits(c.getBenefits())
					.couponType(c.getCouponType())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return couponDTOList;
	}

	// 유효기간이 지난 쿠폰 삭제
	public void deleteExpiredCoupons(LocalDate now) {
		// 유효기간이 지난 쿠폰을 삭제하는 로직
		couponRepository.deleteByExpirationDateBefore(now);
	}

	//월1일 쿠폰 지급
	@Override
	public void addMonthCoupon(String userId) {
		String couponName = "[오프라인] 20% 할인 쿠폰";
		String minPrice = "30000";
		String benefits = "20% 할인";
		String couponType = "offline";

		Coupon coupon = createCoupon(userId, couponName, minPrice, benefits, couponType);
		couponRepository.save(coupon);
	}

	@Override
	@Transactional
	public void deleteByCouponId(Integer couponId) {
		couponRepository.deleteByCouponId(couponId);
	}

}
