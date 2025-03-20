package com.exam.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

}
