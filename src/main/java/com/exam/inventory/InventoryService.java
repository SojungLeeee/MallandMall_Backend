package com.exam.inventory;

import java.util.List;

public interface InventoryService {

	InventoryDTO findByProductCodeAndBranchName(String productCode, String branchName);

	List<InventoryDTO> findAllInventory();
}
