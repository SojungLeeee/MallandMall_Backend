package com.exam.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

	//쿠폰 저장 : save(entity) 사용하면 될 듯
}
