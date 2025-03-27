package com.exam.adminbranch;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*") // 실제 환경에서는 특정 도메인으로 제한하도록 해야한다. 나중에 설정
public class MapApiController {

	private final BranchService branchService;

	public MapApiController(BranchService branchService) {
		this.branchService = branchService;
	}

	/**
	 * 모든 지점의 위치와 상품 수를 반환하는 API
	 */
	@GetMapping("/branches")
	public ResponseEntity<List<BranchLocationDTO>> getAllBranches() {
		List<BranchLocationDTO> branches = branchService.getAllBranchesWithGoodsCount();
		return ResponseEntity.ok(branches);
	}

	/**
	 * 특정 지점의 상세 정보와 상품 리스트를 반환하는 API
	 */
	@GetMapping("/branches/{branchName}")
	public ResponseEntity<BranchLocationDTO> getBranchDetail(@PathVariable String branchName) {
		try {
			BranchLocationDTO branch = branchService.getBranchDetailWithGoods(branchName);
			return ResponseEntity.ok(branch);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}