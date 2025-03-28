package com.exam.Quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {

	@Bean
	public SpringBeanJobFactory jobFactory() {
		SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
		return jobFactory;
	}

	// ExpirationDateCheckJob 클래스 정의 (Spring 관리 빈으로 등록)
	@Bean
	public JobDetail expirationDateCheckJobDetail() {
		return JobBuilder.newJob(ExpirationDateCheckJob.class)  // Job 클래스 참조
			.withIdentity("ExpirationDateCheckJob", "DEFAULT")  // Job 이름과 그룹을 정확히 지정
			.storeDurably()  // Job이 Quartz에 의해 지속적으로 저장되도록 설정
			.build();
	}

	// Trigger 정의 (10초마다 실행)
	@Bean
	public Trigger expirationDateCheckJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(expirationDateCheckJobDetail())  // JobDetail과 연결
			.withIdentity("ExpirationDateCheckJobTrigger", "DEFAULT")  // Trigger 이름과 그룹을 지정
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")) // 1시간마다 실행 (매시 정각)
			.build();
	}

	// CouponExpirationCheckJob 클래스 정의 (Spring 관리 빈으로 등록)
	@Bean
	public JobDetail couponExpirationCheckJobDetail() {
		return JobBuilder.newJob(CouponExpirationCheckJob.class)  // Job 클래스 참조
			.withIdentity("CouponExpirationCheckJob", "DEFAULT")  // Job 이름과 그룹을 정확히 지정
			.storeDurably()  // Job이 Quartz에 의해 지속적으로 저장되도록 설정
			.build();
	}

	// Trigger 정의 (매일 자정마다 실행)
	@Bean
	public Trigger couponExpirationCheckJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(couponExpirationCheckJobDetail())  // JobDetail과 연결
			.withIdentity("CouponExpirationCheckJobTrigger", "DEFAULT")  // Trigger 이름과 그룹을 지정
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?")) // 매일 자정마다 실행
			.build();
	}

	// GiveMonthCouponJob 클래스 정의 (Spring 관리 빈으로 등록)
	@Bean
	public JobDetail giveMonthCouponJobDetail() {
		return JobBuilder.newJob(GiveMonthCouponJob.class)  // Job 클래스 참조
			.withIdentity("GiveMonthCouponJob", "DEFAULT")  // Job 이름과 그룹을 정확히 지정
			.storeDurably()  // Job이 Quartz에 의해 지속적으로 저장되도록 설정
			.build();
	}

	// Trigger 정의 (10초마다 실행)
	@Bean
	public Trigger giveMonthCheckJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(giveMonthCouponJobDetail())  // JobDetail과 연결
			.withIdentity("GiveMonthCouponJobTrigger", "DEFAULT")  // Trigger 이름과 그룹을 지정
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?")) // 매월 1일마다
			.build();
		//"0 * * * * ?" 1분
		//"0 0 0 * * ?" 매일 자정
		//"0 0 1 * * ?" 매월 1일
	}

	// SchedulerFactoryBean 설정
	@Bean
	public SchedulerFactoryBean scheduler() {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

		// JobFactory 설정 (Spring 관리 빈으로 등록된 Job 객체 사용)
		schedulerFactoryBean.setJobFactory(jobFactory());

		// JobDetail과 Trigger를 스케줄러에 추가
		schedulerFactoryBean.setJobDetails(expirationDateCheckJobDetail(),
			couponExpirationCheckJobDetail(),
			giveMonthCouponJobDetail());  // JobDetails 추가
		schedulerFactoryBean.setTriggers(expirationDateCheckJobTrigger(),
			couponExpirationCheckJobTrigger(),
			giveMonthCheckJobTrigger());  // Triggers 추가

		// 추가적인 설정 (옵션 설정)
		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true); // 애플리케이션 종료시 작업 완료 대기
		schedulerFactoryBean.setOverwriteExistingJobs(true); // 기존의 Job을 덮어쓸지 여부
		schedulerFactoryBean.setStartupDelay(10); // 스케줄러 시작 지연 시간 (초)

		return schedulerFactoryBean;
	}
}