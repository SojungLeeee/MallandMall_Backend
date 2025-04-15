package com.exam.inventory.ai;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.exam.inventory.InventoryLogRepository;
import com.exam.inventory.ProductBranchKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAnomalyMonitorService {

	private final InventoryLogRepository inventoryLogRepository;
	private final InventoryAiAnalysisService aiService;
	private final InventoryAnomalyNotificationService notificationService;

	@Scheduled(cron = "0 0/30 * * * *")
	// 매 5분마다 실행
	// 매 10초마다 // 매 30분마다
	public void analyzeAllBranches() {
		log.info("AI 감지 지점별 재고 이상 감시 시작");

		List<ProductBranchKey> targets = inventoryLogRepository.findAllProductBranchPairs();

		for (ProductBranchKey key : targets) {
			try {
				InventoryAlertDTO result = aiService.analyzeBranchInventoryTrend(key.getProductCode(),
					key.getBranchName());

				// TRUE 그리고 위험지수가 80이상이면 알림 발생하는다
				if (result.isAnomaly() && result.getRiskScore() >= 80) {
					notificationService.analyzeAndNotifyAsync(key.getProductCode(), key.getBranchName());
				}
			} catch (Exception e) {
				log.error("AI 분석 실패 - 상품: {}, 지점: {}, 오류: {}", key.getProductCode(), key.getBranchName(),
					e.getMessage());
			}
		}

		log.info("[AI 감지] 재고 감시 완료");
	}
}