package com.exam.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/inventory")
@Slf4j
public class InventoryController {


		/*
			관리자가
			상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
			개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
			행사 등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블
			지점 등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
		*/

	InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	//인벤토리 전체보기
	@GetMapping("/findAllInventory")
	public ResponseEntity<List<InventoryDTO>> findAllInventory() {
		List<InventoryDTO> inventoryDTOList = inventoryService.findAllInventory();
		return ResponseEntity.status(200).body(inventoryDTOList);
	}

	//productCode 별 재고 볼 수 있도록 설정 (상품코드별 재고 합산느낌)
	@GetMapping("/findByProductCode/{productCode}")
	public ResponseEntity<Map<String, Object>> findInventoryByProductCode(@PathVariable String productCode) {
		List<InventoryDTO> inventoryDTOList = inventoryService.findByProductCode(productCode);
		// quantity 합산
		int totalQuantity = inventoryDTOList.stream()
			.mapToInt(InventoryDTO::getQuantity)  // InventoryDTO의 getQuantity()를 통해 quantity 값을 가져옴
			.sum();  // 합산

		// 결과를 반환
		Map<String, Object> response = new HashMap<>();
		response.put("inventory", inventoryDTOList);
		response.put("totalQuantity", totalQuantity);  // totalQuantity 추가

		return ResponseEntity.status(200).body(response);
	}

	@GetMapping("/findByProductCodeAndBranchName/{productCode}/{branchName}")
	public ResponseEntity<InventoryDTO> getInventoryByProductCodeAndBranchName(
		@PathVariable String productCode,
		@PathVariable String branchName) {

		try {
			InventoryDTO inventoryDTO = inventoryService.findByProductCodeAndBranchName(productCode, branchName);

			// Return a 200 OK response with the InventoryDTO object
			return ResponseEntity.ok(inventoryDTO);

		} catch (Exception e) {
			// Handle errors, for example, productCode and branchName may not exist
			return ResponseEntity.status(404).body(null); // You can customize this as per your error handling logic
		}
	}
}
