package com.exam.adminbranch;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchWithStockDTO {
	private String branchName;
	private String branchAddress;
	private Double latitude;
	private Double longitude;
	private Double distance; // 사용자와의 거리 (km)
	private boolean hasStock; // 모든 요청된 상품에 대한 재고 존재 여부
	private Map<String, Integer> stockDetails; // 상품별 재고 수량 (productCode -> quantity)
}