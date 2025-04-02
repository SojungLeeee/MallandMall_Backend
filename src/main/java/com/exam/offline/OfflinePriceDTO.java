package com.exam.offline;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class OfflinePriceDTO {

	private String productCode;
	private int price;
	private LocalDate priceDate;
	private String category;

}

