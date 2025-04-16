package com.exam.search.keyword;

import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SearchServiceConfig {
	private final RealTimeKeywordService keywordService;
	private final SearchKeywordRepository keywordRepository;

	/**
	 * 애플리케이션 시작 시 실행되는 초기화 메서드
	 * 개발 환경과 프로덕션 환경을 구분하여 초기화
	 */
	@Bean
	@Profile("dev") // 개발 환경에서만 실행
	public CommandLineRunner initDevSearchService() {
		return args -> {
			log.info("개발 환경 검색 서비스 초기화 시작");

			// 개발 환경에서는 샘플 데이터 초기화
			if (keywordRepository.count() == 0) {
				initializeWithSampleData();
			}

			log.info("개발 환경 검색 서비스 초기화 완료");
		};
	}

	@Bean
	@Profile("prod") // 프로덕션 환경에서만 실행
	public CommandLineRunner initProdSearchService() {
		return args -> {
			log.info("프로덕션 검색 서비스 초기화");
			// 프로덕션 환경에서는 샘플 데이터 초기화 없음
		};
	}

	/**
	 * 개발용 샘플 데이터 초기화
	 */
	private void initializeWithSampleData() {
		String[] sampleKeywords = {
			"노트북", "스마트폰", "에어팟", "태블릿", "블루투스 이어폰",
			"키보드", "마우스", "모니터", "노트북 가방", "충전기",
			"외장하드", "무선마우스", "게이밍 키보드", "노트북 거치대", "보조배터리"
		};

		Random random = new Random();

		for (String keyword : sampleKeywords) {
			SearchKeyword searchKeyword = new SearchKeyword();
			searchKeyword.setKeyword(keyword);
			searchKeyword.setSearchCount(10 + random.nextInt(91)); // 10~100 사이 임의 값
			keywordRepository.save(searchKeyword);

			log.debug("샘플 검색어 저장: {}, 카운트: {}", keyword, searchKeyword.getSearchCount());
		}

		log.info("샘플 검색어 데이터 초기화 완료");
	}
}