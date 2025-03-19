package com.exam.adminbranch;

import org.springframework.stereotype.Service;

import java.util.List;

@Service // 비즈니스 로직 애노테이션
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository; // 데이터 접근 로직을 위한 리포지토리

	// 생성자를 통한 의존성 주입
	public BranchServiceImpl(BranchRepository branchRepository) {
		this.branchRepository = branchRepository;
	}

	@Override
	public List<Branch> getAllBranches() {
		// 데이터베이스에서 모든 지점을 조회
		return branchRepository.findAll();
	}

	@Override
	public Branch getBranchByName(String branchName) {
		// 데이터베이스에서 지점 이름으로 특정 지점을 조회
		return branchRepository.findByBranchName(branchName)
			.orElseThrow(() -> new RuntimeException("지점을 찾을 수 없습니다: " + branchName));
	}
}