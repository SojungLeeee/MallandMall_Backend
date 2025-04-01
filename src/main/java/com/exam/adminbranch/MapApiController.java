package com.exam.adminbranch;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapApiController {

	private final BranchService branchService;

	public MapApiController(BranchService branchService) {
		this.branchService = branchService;
	}

	/**
	 * 모든 지점의 위치 정보를 반환하는 API
	 */
	@GetMapping("/branches")
	public ResponseEntity<List<BranchLocationDTO>> getAllBranches() {
		List<Branch> branches = branchService.getAllBranches();

		// 지점 정보를 DTO로 변환
		List<BranchLocationDTO> locationDTOs = branches.stream()
			.map(branch -> {
				BranchLocationDTO dto = new BranchLocationDTO();
				dto.setBranchName(branch.getBranchName());
				dto.setBranchAddress(branch.getBranchAddress());
				dto.setLatitude(branch.getLatitude());
				dto.setLongitude(branch.getLongitude());
				return dto;
			})
			.toList();

		return ResponseEntity.ok(locationDTOs);
	}

	/**
	 * 특정 지점의 상세 정보를 반환하는 API
	 */
	@GetMapping("/branches/{branchName}")
	public ResponseEntity<BranchDTO> getBranchDetail(@PathVariable String branchName) {
		try {
			Branch branch = branchService.getBranchByName(branchName);
			BranchDTO dto = BranchDTO.builder()
				.branchName(branch.getBranchName())
				.branchAddress(branch.getBranchAddress())
				.latitude(branch.getLatitude())
				.longitude(branch.getLongitude())
				.build();

			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * 특정 상품을 보유한 지점 목록을 반환하는 API
	 */
	@GetMapping("/branches/product/{productCode}")
	public ResponseEntity<List<BranchLocationDTO>> getBranchesByProduct(@PathVariable String productCode) {
		List<BranchLocationDTO> branches = branchService.getBranchesWithProduct(productCode);
		return ResponseEntity.ok(branches);
	}

	/**
	 * 지점명 또는 주소로 검색하는 API
	 */
	@GetMapping("/search")
	public ResponseEntity<List<BranchLocationDTO>> searchBranches(@RequestParam String keyword) {
		List<BranchLocationDTO> branches = branchService.searchBranches(keyword);
		return ResponseEntity.ok(branches);
	}
}