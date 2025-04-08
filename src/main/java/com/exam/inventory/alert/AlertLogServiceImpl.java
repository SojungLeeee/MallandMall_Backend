package com.exam.inventory.alert;

import com.exam.inventory.ai.InventoryAlertDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertLogServiceImpl implements AlertLogService {

	private final AlertLogRepository alertLogRepository;

	@Override
	public void saveAlert(String productCode, String branchName, InventoryAlertDTO dto) {
		AlertLog log = AlertLog.builder()
			.productCode(productCode)
			.branchName(branchName)
			.anomaly(dto.isAnomaly())
			.trendSummary(dto.getTrendSummary())
			.recommendation(dto.getRecommendation())
			.riskScore(dto.getRiskScore())
			.alertTime(LocalDateTime.now())
			.build();

		alertLogRepository.save(log);
	}

	@Override
	public List<AlertLog> getAllAlerts() {
		return alertLogRepository.findAllByOrderByAlertTimeDesc();
	}

	@Override
	public List<AlertLog> getAlertsByProduct(String productCode) {
		return alertLogRepository.findByProductCodeOrderByAlertTimeDesc(productCode);
	}
}