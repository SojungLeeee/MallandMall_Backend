package com.exam.Quartz;

import java.time.LocalDate;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.exam.coupon.CouponService;

import jakarta.transaction.Transactional;

@Component  // Spring 관리 빈으로 등록
public class CouponExpirationCheckJob implements Job {

	private final CouponService couponService;

	public CouponExpirationCheckJob(CouponService couponService) {
		this.couponService = couponService;
	}

	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LocalDate now = LocalDate.now();
		System.out.println("Checking for expired coupons at: " + now);

		// 현재 시간보다 이전 유효기간을 가진 쿠폰을 삭제
		couponService.deleteExpiredCoupons(now);

		// 실행된 로그 출력
		System.out.println("CouponExpirationCheckJob executed at: " + now);
	}
}
