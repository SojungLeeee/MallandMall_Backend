package com.exam.adminbranch;

import java.util.List;

public interface BranchService {
	//모든 지점 조회
	List<Branch> getAllBranches();

	//특정 지점 조회
	Branch getBranchByName(String branchName);

	//지점 추가
	Branch createBranches(Branch branch);

	//지점 수정
	Branch updateBranches(Branch branch);

	//지점 삭제
	void deleteBranches(Branch branch, String branchName);

	// 추가: 지도 API용 메소드
	//특정 상품을 보유한 지점 목록 조회
	List<BranchLocationDTO> getBranchesWithProduct(String productCode);

	//지점명 또는 주소로 검색
	List<BranchLocationDTO> searchBranches(String keyword);

	// navermap - 가까운 지점 찾기
	BranchDTO findNearestBranch(Double latitude, Double longitude);

	// 가까운 지점을 재고로 찾기
	List<BranchWithStockDTO> findNearestBranchesWithStock(double latitude, double longitude, List<String> productCodes, int limit);
	}

