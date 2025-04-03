package com.exam.inventory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
// AdminServiceImpl에 재고 추가, 삭제 시 로그기록하는 메소드 넣어놨음
@Service
public class InventoryLogServiceImpl implements InventoryLogService {

	private final InventoryLogRepository inventoryLogRepository;

	public InventoryLogServiceImpl(InventoryLogRepository inventoryLogRepository) {
		this.inventoryLogRepository = inventoryLogRepository;
	}

	@Override
	public void saveLog(InventoryLogDTO logDTO) {
		List<InventoryLog> existingLogs = inventoryLogRepository
			.findByProductCodeAndBranchNameOrderByChangeDateAsc(
				logDTO.getProductCode(), logDTO.getBranchName());

		// 현재까지 재고량 계산
		int currentStock = 0;
		for (InventoryLog log : existingLogs) {
			currentStock += (log.getChangeType() == ChangeType.IN) ? log.getQuantity() : -log.getQuantity();
		}

		// 이번 변경 적용
		int change = (logDTO.getChangeType() == ChangeType.IN) ? logDTO.getQuantity() : -logDTO.getQuantity();
		int newRemainingStock = currentStock + change;

		InventoryLog log = InventoryLog.builder()
			.productCode(logDTO.getProductCode())
			.branchName(logDTO.getBranchName())
			.changeType(logDTO.getChangeType())
			.quantity(logDTO.getQuantity())
			.changeDate(logDTO.getChangeDate() != null ? logDTO.getChangeDate() : java.time.LocalDateTime.now())
			.remainingStock(newRemainingStock)
			.build();

		inventoryLogRepository.save(log);
	}


	// 관리자를 위한 지점 + 상품코드를 통한 상품 재고 추이
	@Override
	public List<InventoryLogDTO> getStockHistoryByProductAndBranch(String productCode, String branchName) {
		List<InventoryLog> logs = inventoryLogRepository
			.findByProductCodeAndBranchNameOrderByChangeDateAsc(productCode, branchName);

		// 날짜별 정렬 후 재고 잔량 누적
		List<InventoryLogDTO> result = new ArrayList<>();
		int currentStock = 0;

		for (InventoryLog log : logs) {
			if (log.getChangeType() == ChangeType.IN) {
				currentStock += log.getQuantity();
			} else {
				currentStock -= log.getQuantity();
			}

			result.add(InventoryLogDTO.builder()
				.logId(log.getLogId())
				.productCode(log.getProductCode())
				.branchName(log.getBranchName())
				.changeType(ChangeType.IN) // 의미없지만 유지
				.quantity(currentStock) //  현재 잔량
				.remainingStock(log.getRemainingStock())
				.changeDate(log.getChangeDate())
				.build());
		}

		return result;
	}


	// 마이그레이션용 코드
	@Override
	public void migrateRemainingStock() {
		List<InventoryLog> logs = inventoryLogRepository.findAllByOrderByChangeDateAsc();

		// 상품+지점별 누적 재고량 계산을 위한 Map
		Map<String, Integer> stockMap = new HashMap<>();

		for (InventoryLog log : logs) {
			String key = log.getProductCode() + "_" + log.getBranchName();

			int currentStock = stockMap.getOrDefault(key, 0);
			int change = (log.getChangeType() == ChangeType.IN) ? log.getQuantity() : -log.getQuantity();
			int updatedStock = currentStock + change;

			log.setRemainingStock(updatedStock);
			stockMap.put(key, updatedStock);
		}

		inventoryLogRepository.saveAll(logs);
	}




	@Override
	public List<InventoryLogDTO> getAverageStockHistoryByProduct(String productCode) {
		List<InventoryLog> logs = inventoryLogRepository
			.findByProductCodeOrderByChangeDateAsc(productCode);

		// 날짜별로 로그 분류
		Map<LocalDate, List<InventoryLog>> logsByDate = logs.stream()
			.collect(Collectors.groupingBy(log -> log.getChangeDate().toLocalDate()));

		List<InventoryLogDTO> result = new ArrayList<>();

		for (LocalDate date : logsByDate.keySet().stream().sorted().toList()) {
			List<InventoryLog> dayLogs = logsByDate.get(date);

			// 지점별 가장 마지막 로그만 추출
			Map<String, InventoryLog> latestPerBranch = new HashMap<>();
			for (InventoryLog log : dayLogs) {
				latestPerBranch.put(log.getBranchName(), log); // 가장 마지막 것이 덮어씌워짐
			}

			// 지점별 마지막 로그의 remainingStock 합산
			int totalStock = latestPerBranch.values().stream()
				.mapToInt(log -> log.getRemainingStock() != null ? log.getRemainingStock() : 0)
				.sum();

			result.add(InventoryLogDTO.builder()
				.productCode(productCode)
				.branchName("전국") // 지점이 아님을 명시
				.changeType(ChangeType.IN) // 의미 없음, 차트용
				.quantity(totalStock)
				.remainingStock(totalStock)
				.changeDate(date.atStartOfDay())
				.build());
		}

		return result;
	}


}
