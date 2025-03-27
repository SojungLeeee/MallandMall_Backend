package com.exam.coupon;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

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
@Alias("CouponDTO")
public class CouponDTO {

	int couponId;  // 쿠폰 고유 번호
	String userId;
	String couponName;  // 쿠폰명
	String minPrice;  // 최소 구매 금액
	LocalDate expirationDate;  // 쿠폰 유효 기간
	String benefits;  // 쿠폰 혜택
	String couponType;

}
