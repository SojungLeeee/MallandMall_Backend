// 📌 주문 정보에서 특정 유저가 특정 상품을 구매한 적이 있는지 확인하는 메서드 추가
package com.exam.orderinfo;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.exam.order.OrderInfo;

public interface OrderInfoRepository extends JpaRepository<com.exam.order.OrderInfo, Long> {
	// 댓글쓸떄 구매내역 조회
	boolean existsByUserIdAndProductCode(String userId, String productCode);

	List<OrderInfo> findByImpUid(String impUid);

	List<OrderInfo> findByUserId(String userId);

	@Query("SELECT new com.exam.orderinfo.UserOrderInfo(o.userId, CAST(SUM(o.orderPrice) AS int)) " +
		"FROM OrderInfo o " +
		"WHERE o.orderDate BETWEEN :startDate AND :endDate " +
		"GROUP BY o.userId")
	List<UserOrderInfo> sumOrderPriceByUserAndDateRange(@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);
}