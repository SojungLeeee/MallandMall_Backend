package com.exam.adminbranch;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/branch")
public class BranchController {

	private final BranchService branchService;

	public BranchController(BranchService branchService) {
		this.branchService = branchService;
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllBranches() {
		try {
			List<Branch> branches = branchService.getAllBranches();
			List<BranchDTO> branchDTOs = branches.stream()
				.map(branch -> BranchDTO.builder()
					.branchName(branch.getBranchName())
					.branchAddress(branch.getBranchAddress())
					.build())
				.collect(Collectors.toList());

			return ResponseEntity.ok(branchDTOs);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	@GetMapping("/{branchName}")
	public ResponseEntity<?> getBranchByName(@PathVariable String branchName) {
		try {
			Branch branch = branchService.getBranchByName(branchName);
			BranchDTO branchDTO = BranchDTO.builder()
				.branchName(branch.getBranchName())
				.branchAddress(branch.getBranchAddress())
				.build();

			return ResponseEntity.ok(branchDTO);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("지점 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}