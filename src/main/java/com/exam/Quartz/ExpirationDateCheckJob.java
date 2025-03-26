package com.exam.Quartz;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.exam.admin.GoodsService;

import jakarta.transaction.Transactional;

@Component  // Spring 관리 빈으로 등록
public class ExpirationDateCheckJob implements Job {

	private final GoodsService goodsService;

	public ExpirationDateCheckJob(GoodsService goodsService) {
		this.goodsService = goodsService;
	}

	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LocalDateTime now = LocalDateTime.now();
		System.out.println("Checking for expired goods at: " + now);

		// 현재 시간보다 이전 유통기한을 가진 상품을 삭제
		goodsService.deleteExpiredGoods(now);

		// 실행된 로그 출력
		System.out.println("ExpirationDateCheckJob executed at: " + now);
	}
}
