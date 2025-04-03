package com.exam.inventory;

import java.util.List;

public interface InventoryLogService {

	void saveLog(InventoryLogDTO logDTO);

	List<InventoryLogDTO> getStockHistoryByProductAndBranch(String productCode, String branchName);

	List<InventoryLogDTO> getAverageStockHistoryByProduct(String productCode);

	// 마이그레이션용
	void migrateRemainingStock();


}
