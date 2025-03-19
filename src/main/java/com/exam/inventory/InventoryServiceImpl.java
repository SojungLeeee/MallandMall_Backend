package com.exam.inventory;

import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

	InventoryRepository inventoryRepository;

	public InventoryServiceImpl(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	@Override
	public InventoryDTO findByProductCodeAndBranchName(String productCode, String branchName) {
		Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(productCode, branchName);

		//Inventory -> InventoryDTO
		InventoryDTO inventoryDTO = InventoryDTO.builder()
			.productCode(productCode)
			.quantity(inventory.getQuantity())
			.branchName(branchName)
			.build();

		return inventoryDTO;
	}

}
