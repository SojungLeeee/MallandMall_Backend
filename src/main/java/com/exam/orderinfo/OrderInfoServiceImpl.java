package com.exam.orderinfo;

import java.io.IOException;
import java.time.LocalDate;
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

			if (dto.getOrders() != null && !dto.getOrders().isEmpty()) {
				// 개별 상품에 대한 주문 저장
				for (ProductOrderDTO item : dto.getOrders()) {
					Product product = productRepository.findByProductCode(item.getProductCode());
					if (product == null) {
						log.warn(" 상품을 찾을 수 없습니다22: {}", item.getProductCode());
						return false;
					}

					// 실제 결제된 금액을 기준으로 처리
					//int itemPrice = paidAmount / dto.getOrders().size(); // 예시: 결제된 금액을 주문 개수로 나누어 분배
					//int itemPrice = product.getPrice() * item.getQuantity();
					// 할인된 가격을 사용
					int originalPrice = product.getPrice() * item.getQuantity(); // 원래 가격 계산
					int discountPercent = dto.getDiscountedPrice(); // 할인 비율

					// 할인된 가격 계산 후 바로 int 형으로 변환 (소수점 버림)
					int itemPrice = (int)(originalPrice - (originalPrice * (discountPercent / 100.0)));
					System.out.println("할인비율 : " + dto.getDiscountedPrice());
					System.out.println("할인된 가격 : " + itemPrice);
					// 개별 주문 저장
					OrderInfo order = OrderInfo.builder()
						.userId(dto.getUserId())
						.productCode(item.getProductCode())
						.quantity(item.getQuantity())
						.receiverName(dto.getReceiverName())
						.post(dto.getPost())
						.addr1(dto.getAddr1())
						.addr2(dto.getAddr2())
						.phoneNumber(dto.getPhoneNumber())
						.orderPrice(itemPrice)  // 실제 결제 금액으로 저장
						.impUid(dto.getImpUid())
						.orderDate(dto.getOrderDate())
						.build();

					orderInfoRepository.save(order);
				}

				return true;
			}

			Product product = productRepository.findByProductCode(dto.getProductCode());
			System.out.println("선택된 product : " + product);
			if (product == null) {
				log.warn(" 상품을 찾을 수 없습니다111.");
				return false;
			}

			// 실제 결제된 금액을 사용
			int orderPrice = paidAmount;

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
				.orderPrice(orderPrice)  // 실제 결제 금액으로 저장
				.impUid(dto.getImpUid())
				.orderDate(dto.getOrderDate())
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

	public List<OrderInfoDTO> getOrderInfoByImpUid(String impUid) {
		// imp_uid로 여러 개의 주문을 찾기
		List<OrderInfo> orders = orderInfoRepository.findByImpUid(impUid);

		// 만약 결과가 없다면 null 반환
		if (orders == null || orders.isEmpty()) {
			return null;
		}

		// 여러 개의 주문을 DTO로 변환하여 반환
		return orders.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	@Override
	public List<UserOrderInfo> getTotalPriceByUserForPeriod(LocalDate startDate, LocalDate endDate) {
		// 사용자별로 총 금액을 계산하여 반환
		return orderInfoRepository.sumOrderPriceByUserAndDateRange(startDate, endDate);
	}

	/*@Override
	public OrderInfoDTO getOrderInfoByImpUid(String impUid) {
		OrderInfo order = orderInfoRepository.findByImpUid(impUid);
		if (order == null)
			return null;

		return convertToDTO(order); // 이미 너가 가지고 있던 변환 메서드 사용
	}*/

	private OrderInfoDTO convertToDTO(OrderInfo order) {

		Product product = productRepository.findByProductCode(order.getProductCode());

		if (product == null) {
			log.warn("상품 정보를 찾을 수 없습니다: {}", order.getProductCode());
			return null;
		}
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
			.productName(product.getProductName()) // 상품명
			.image(product.getImage()) // 상품 이미지 URL
			.orderDate(order.getOrderDate())
			.build();
	}
}