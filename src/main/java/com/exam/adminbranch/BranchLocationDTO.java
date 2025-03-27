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
public class BranchLocationDTO {
	private String branchName;
	private String branchAddress;
	private Double latitude;
	private Double longitude;
	private Integer goodsCount;
	private List<GoodsInfoDTO> goods;
}
