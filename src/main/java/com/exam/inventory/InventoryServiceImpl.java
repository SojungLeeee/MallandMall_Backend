package com.exam.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
			.productCode(inventory.getProductCode())
			.quantity(inventory.getQuantity())
			.branchName(inventory.getBranchName())
			.build();

		return inventoryDTO;
	}

	@Override
	public List<InventoryDTO> findAllInventory() {
		List<Inventory> inventoryList = inventoryRepository.findAll();
		//Stream API 의 Map 이용
		List<InventoryDTO> inventoryDTOList =
			inventoryList.stream().map(i -> {
				InventoryDTO dto = InventoryDTO.builder()
					.productCode(i.getProductCode())
					.quantity(i.getQuantity())
					.branchName(i.getBranchName())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return inventoryDTOList;
	}

	@Override
	public List<InventoryDTO> findByProductCode(String productCode) {
		List<Inventory> inventoryList = inventoryRepository.findByProductCode(productCode);
		List<InventoryDTO> inventoryDTOList =
			inventoryList.stream().map(i -> {
				InventoryDTO dto = InventoryDTO.builder()
					.productCode(i.getProductCode())
					.quantity(i.getQuantity())
					.branchName(i.getBranchName())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return inventoryDTOList;
	}


	// 특정 상품의 지점별 수량 조회 메소드
	@Override
	public Map<String, Integer> findQuantityByProductCode(String productCode) {
		List<Inventory> inventories = inventoryRepository.findByProductCode(productCode);
		Map<String, Integer> branchQuantities = new HashMap<>();

		for (Inventory inventory : inventories) {
			branchQuantities.put(inventory.getBranchName(), inventory.getQuantity());
		}

		return branchQuantities;
	}



}
