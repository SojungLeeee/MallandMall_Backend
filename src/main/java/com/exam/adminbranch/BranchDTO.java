package com.exam.adminbranch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class BranchDTO {
	@Id
	String branchName;
	String branchAddress;

	//위도 경도 필드 추가
	Double latitude;
	Double longitude;

	//재고 있는 상품 수
	Integer goodsCount;

	//거리 정보 추가
	Double distance;

}

