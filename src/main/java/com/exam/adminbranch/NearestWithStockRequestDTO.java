package com.exam.adminbranch;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NearestWithStockRequestDTO {
	private double latitude;
	private double longitude;
	private List<String> productCodes; // 상품 코드 리스트
	private Integer limit; // 반환할 지점 개수 제한 (null일 경우 기본값 사용)
}