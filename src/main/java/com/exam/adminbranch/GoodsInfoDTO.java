package com.exam.adminbranch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsInfoDTO {
	private String productCode;
	private String productName;
	private Integer count;  // 해당 상품의 개수
}