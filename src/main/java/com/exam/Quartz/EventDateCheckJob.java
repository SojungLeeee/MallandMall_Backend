package com.exam.Quartz;

import java.time.LocalDateTime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.exam.adminbranch.EventService;

import jakarta.transaction.Transactional;

@Component  // Spring 관리 빈으로 등록
public class EventDateCheckJob implements Job {

	private final EventService eventService;

	public EventDateCheckJob(EventService eventService) {
		this.eventService = eventService;
	}

	@Override
	@Transactional
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LocalDateTime now = LocalDateTime.now();
		System.out.println("Checking for expired event at: " + now);

		// 현재 시간보다 이전 유효기간을 가진 쿠폰을 삭제
		eventService.deleteExpiredEvent(now);

		// 실행된 로그 출력
		System.out.println("EventDateCheckJob executed at: " + now);
	}
}
