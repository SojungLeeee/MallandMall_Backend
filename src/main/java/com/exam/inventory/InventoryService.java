package com.exam.inventory;

public interface InventoryService {

	InventoryDTO findByProductCodeAndBranchName(String productCode, String branchName);
}
