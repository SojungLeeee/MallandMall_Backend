package com.exam.orderinfo;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.exam.order.OrderInfo;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

	private final OrderInfoRepository orderInfoRepository;

	public OrderInfoServiceImpl(OrderInfoRepository orderInfoRepository) {
		this.orderInfoRepository = orderInfoRepository;
	}

	// 사용자의 주문 내역
	@Override
	public List<OrderInfoDTO> getOrdersByUserId(String userId) {
		List<com.exam.order.OrderInfo> orders = orderInfoRepository.findByUserId(userId);
		return orders.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	//  Entity → DTO 변환
	private OrderInfoDTO convertToDTO(OrderInfo order) {
		return OrderInfoDTO.builder()
			.orderId(order.getOrderId())
			.userId(order.getUserId())
			.productCode(order.getProductCode())
			.quantity(order.getQuantity())
			.receiverName(order.getReceiverName())
			.post(order.getPost())
			.addr1(order.getAddr1())
			.addr2(order.getAddr2())
			.phoneNumber(order.getPhoneNumber())
			.build();
	}
}
