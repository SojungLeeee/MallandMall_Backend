package com.exam.coupon;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

	//쿠폰 저장 : save(entity) 사용하면 될 듯
	//findByUserId로 쿠폰 목록 찾기
	List<Coupon> findByUserId(String userId);

	// 유효기간이 주어진 날짜 이전인 쿠폰을 삭제
	void deleteByExpirationDateBefore(LocalDate now);
}
