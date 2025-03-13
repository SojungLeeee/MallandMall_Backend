package com.exam.product;

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
public class ProductDTO {

	private String productCode;
	private String category;
	private String productName;
	private String description;
	private int price;
	private String image;
	private double averageRating;
}
