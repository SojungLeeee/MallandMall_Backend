package com.exam.Quartz;

import java.time.LocalDate;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.exam.coupon.CouponService;
import com.exam.orderinfo.OrderInfoService;
import com.exam.orderinfo.UserOrderInfo;

import jakarta.transaction.Transactional;

@Component  // Spring 관리 빈으로 등록
public class GiveMonthCouponJob implements Job {

	private final CouponService couponService;
	private final OrderInfoService orderInfoService;  // OrderInfoService 추가

	public GiveMonthCouponJob(CouponService couponService, OrderInfoService orderInfoService) {
		this.couponService = couponService;
		this.orderInfoService = orderInfoService;
	}

	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {

		LocalDate now = LocalDate.now();

		// 1. 이전 달의 첫날과 마지막 날 계산
		LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);  // 이전 달 첫날
		LocalDate lastDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1).plusMonths(1).minusDays(1);  // 이전 달 마지막 날

		// 2. 이전 달에 대한 모든 사용자별 구매 금액 합산
		List<UserOrderInfo> userOrderInfoList = orderInfoService.getTotalPriceByUserForPeriod(firstDayOfLastMonth,
			lastDayOfLastMonth);

		// 3. 사용자별로 총 구매 금액이 5만원 이상인 경우 쿠폰을 지급
		for (UserOrderInfo userOrderInfo : userOrderInfoList) {
			if (userOrderInfo.getTotalPrice() >= 50000) {
				couponService.addMonthCoupon(userOrderInfo.getUserId());  // 쿠폰 지급 로직
				System.out.println(
					"Coupon issued to user " + userOrderInfo.getUserId() + " due to total price >= 50000");
			}
		}

		// 실행된 로그 출력
		System.out.println("GiveMonthCouponJob executed at: " + now);
	}
}
