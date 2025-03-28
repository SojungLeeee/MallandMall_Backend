package com.exam.orderinfo;

import java.time.LocalDate;
import java.util.List;

public interface OrderInfoService {
	List<OrderInfoDTO> getOrdersByUserId(String userId);

	boolean verifyAndSaveOrder(OrderInfoDTO orderDto);

	List<OrderInfoDTO> getOrderInfoByImpUid(String impUid);

	List<UserOrderInfo> getTotalPriceByUserForPeriod(LocalDate startDate, LocalDate endDate);  // 사용자별 총 금액 조회
}