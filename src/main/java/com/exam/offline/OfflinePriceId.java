package com.exam.offline;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class OfflinePriceId implements Serializable {

	private String productCode;  // 상품 코드
	private LocalDate priceDate;     // 가격 적용 날짜

	public OfflinePriceId() {}  // 기본 생성자

	public OfflinePriceId(String productCode, LocalDate priceDate) {
		this.productCode = productCode;
		this.priceDate = priceDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OfflinePriceId that = (OfflinePriceId) o;
		return productCode.equals(that.productCode) && priceDate.equals(that.priceDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(productCode, priceDate);
	}
}
