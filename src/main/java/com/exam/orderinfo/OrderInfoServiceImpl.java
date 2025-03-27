package com.exam.orderinfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.order.OrderInfo;
import com.exam.product.Product;
import com.exam.product.ProductRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

	private final IamportClient iamportClient;
	private final OrderInfoRepository orderInfoRepository;
	private final ProductRepository productRepository;

	@Override
	public boolean verifyAndSaveOrder(OrderInfoDTO dto) {
		try {
			// imp_uid를 사용해 아임포트에서 결제 정보 가져오기
			IamportResponse<Payment> response = iamportClient.paymentByImpUid(dto.getImpUid());

			if (response == null || response.getResponse() == null) {
				log.warn(" 결제 정보를 불러올 수 없습니다.");
				return false;
			}

			int paidAmount = response.getResponse().getAmount().intValue();

			Product product = productRepository.findByProductCode(dto.getProductCode());
			if (product == null) {
				log.warn(" 상품을 찾을 수 없습니다.");
				return false;
			}

			int expectedAmount = product.getPrice() * dto.getQuantity();
			int orderPrice = product.getPrice() * dto.getQuantity();
			if (paidAmount != expectedAmount) {
				log.warn(" 금액 불일치: 기대값 = {}, 실제 결제 = {}", expectedAmount, paidAmount);
				return false;
			}

			// 주문 저장
			OrderInfo order = OrderInfo.builder()
				.userId(dto.getUserId())
				.productCode(dto.getProductCode())
				.quantity(dto.getQuantity())
				.receiverName(dto.getReceiverName())
				.post(dto.getPost())
				.addr1(dto.getAddr1())
				.addr2(dto.getAddr2())
				.phoneNumber(dto.getPhoneNumber())
				.orderPrice(orderPrice)
				.impUid(dto.getImpUid())
				.build();

			orderInfoRepository.save(order);
			return true;

		} catch (IamportResponseException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<OrderInfoDTO> getOrdersByUserId(String userId) {
		List<OrderInfo> orders = orderInfoRepository.findByUserId(userId);
		return orders.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	@Override
	public OrderInfoDTO getOrderInfoByImpUid(String impUid) {
		OrderInfo order = orderInfoRepository.findByImpUid(impUid);
		if (order == null)
			return null;

		return convertToDTO(order); // 이미 너가 가지고 있던 변환 메서드 사용
	}

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
			.orderPrice(order.getOrderPrice())
			.phoneNumber(order.getPhoneNumber())
			.impUid(order.getImpUid())
			.build();
	}
}
