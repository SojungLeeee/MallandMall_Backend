package com.exam.adminbranch;

import java.util.List;

public interface BranchService {

	List<Branch> getAllBranches(); //모든 지점 조회

	Branch getBranchByName(String branchName); // 특정 지점 조회 (지점 이름으로 조회)
}
