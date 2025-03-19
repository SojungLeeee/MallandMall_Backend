package com.exam.Cart;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CartDTO {
	private String userId;
	private String productCode;
	private int quantity;
}
