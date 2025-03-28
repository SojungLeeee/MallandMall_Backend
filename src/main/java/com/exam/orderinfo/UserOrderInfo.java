package com.exam.orderinfo;

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
public class UserOrderInfo {
	String userId;  // 사용자 ID
	int totalPrice;  // 해당 사용자의 총 구매 금액
}
