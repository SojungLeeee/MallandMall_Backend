package com.exam.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	InventoryLogService inventoryLogService;

	public InventoryController(InventoryService inventoryService, InventoryLogService inventoryLogService) {
		this.inventoryService = inventoryService;
		this.inventoryLogService = inventoryLogService;
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

	// 특정 상품 코드에 대한 지점별 수량 조회 API
	@GetMapping("/product/{productCode}/branches")
	public ResponseEntity<Map<String, Integer>> getQuantityByProductCode(@PathVariable String productCode) {
		Map<String, Integer> branchQuantities = inventoryService.findQuantityByProductCode(productCode);
		return ResponseEntity.ok(branchQuantities);
	}

	// 특정 지점의 특정 상품 재고 확인
	@GetMapping("/product/{productCode}/branch/{branchName}")
	public ResponseEntity<Integer> getProductQuantityInBranch(
		@PathVariable String productCode,
		@PathVariable String branchName) {

		try {
			InventoryDTO inventory = inventoryService.findByProductCodeAndBranchName(productCode, branchName);
			return ResponseEntity.ok(inventory.getQuantity());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
		}
	}

	// 재고 업데이트 (차감)
	@PostMapping("/update")
	public ResponseEntity<?> updateInventory(@RequestBody InventoryDTO inventoryDTO) {
		try {
			boolean success = inventoryService.updateInventory(
				inventoryDTO.getBranchName(),
				inventoryDTO.getProductCode(),
				inventoryDTO.getQuantity()  // 양수: 재고 추가, 음수: 재고 차감
			);

			if (success) {
				return ResponseEntity.ok().body("재고가 성공적으로 업데이트되었습니다");
			} else {
				return ResponseEntity.badRequest().body("재고 업데이트 실패");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("재고 업데이트 중 오류 발생: " + e.getMessage());
		}
	}

	// 상품별 지점별 재고 히스토리 조회 (관리자용)
	@GetMapping("/log/{productCode}/{branchName}")
	public ResponseEntity<List<InventoryLogDTO>> getStockHistoryByProductAndBranch(
		@PathVariable String productCode,
		@PathVariable String branchName) {

		List<InventoryLogDTO> logs = inventoryLogService.getStockHistoryByProductAndBranch(productCode, branchName);
		return ResponseEntity.ok(logs);
	}

	//전국 재고량
	@GetMapping("/log/avg/{productCode}")
	public ResponseEntity<List<InventoryLogDTO>> getAverageStockHistory(
		@PathVariable String productCode) {

		List<InventoryLogDTO> logs = inventoryLogService.getAverageStockHistoryByProduct(productCode);
		return ResponseEntity.ok(logs);
	}

	// 마이그레이션 용 , 신경 쓸 필요 없습니다 궁금하면 정승호에게 물어봐주세요
	@GetMapping("/migrate-log-remaining")
	public ResponseEntity<String> migrateRemainingStock() {
		inventoryLogService.migrateRemainingStock();
		return ResponseEntity.ok("로그 마이그레이션 완료");

	}

}
