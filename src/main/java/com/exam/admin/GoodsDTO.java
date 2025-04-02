package com.exam.admin;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Alias("GoodsDTO")
public class GoodsDTO {

	private Integer goodsId;  // goodsId는 자동 증가 값이므로, 저장 시 자동으로 설정됩니다.

	@NotBlank(message = "productCode는 필수입니다.")
	private String productCode;  // productCode는 필수 항목

	@NotBlank(message = "branchName은 필수입니다.")
	private String branchName;  // branchName은 필수 항목

	@NotNull(message = "expirationDate는 필수입니다.")
	private LocalDateTime expirationDate;  // expirationDate는 필수 항목

	@NotNull(message = "quantity는 필수입니다.")
	private Integer quantity;  // 입고 개수 추가

}
