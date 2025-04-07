package com.exam.inventory.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.exam.inventory.alert.AlertLogService;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryAnomalyNotificationService {

	private final InventoryAiAnalysisService aiService;       //  AI 분석용
	private final AlertLogService alertLogService;           // 알림 저장용


	// 기존에는 AI 분석은 하지않고 알림 저장만 이 코드에서 수행했음
	/* 근데 Async 어노테이션을 통해 비동기 분석시 여기서 서비스 처리하는게 맞음
	기존 AI 분석 코드 InventoryAiAnalysisServiceImpl는 지우지 말고 그대로 둬야 한다
	왜냐면 기존 컨트롤러에서 대응용으로 쓰고 있기떄문에 그대로 유지할거다
	*/
	@Async("aiExecutor")
	public void analyzeAndNotifyAsync(String productCode, String branchName) {
		try {
			//  AI 분석 수행
			InventoryAlertDTO result = aiService.analyzeBranchInventoryTrend(productCode, branchName);

			// 조건 만족 시 알림 저장
			if (result.isAnomaly() && result.getRiskScore() >= 80) {
				alertLogService.saveAlert(productCode, branchName, result);
				log.warn("[AI 알림] 상품: {}, 지점: {}, 위험도: {}", productCode, branchName, result.getRiskScore());
			}
		} catch (Exception e) {
			log.error("알림 분석 실패: {} - {}", productCode, branchName, e.getMessage());
		}
	}
}