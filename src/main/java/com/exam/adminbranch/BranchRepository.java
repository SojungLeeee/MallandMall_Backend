package com.exam.adminbranch;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, String> {
	//JpaRepository에서 제공하는 CRUD 상속받아 사용
}
