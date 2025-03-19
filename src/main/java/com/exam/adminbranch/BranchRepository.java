package com.exam.adminbranch;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, String> {
	//지점 이름 통해서 조회
	Optional<Branch> findByBranchName(String branchName);

}
