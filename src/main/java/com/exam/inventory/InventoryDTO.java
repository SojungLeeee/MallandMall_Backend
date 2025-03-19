package com.exam.inventory;

import jakarta.validation.constraints.NotBlank;
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
public class InventoryDTO {

	private int inventoryId;

	@NotBlank(message = "productCode 필수")
	private String productCode;

	@NotBlank(message = "quantity는 0이상 필수")
	private int quantity;

	@NotBlank(message = "branchName 필수")
	private String branchName;

}
