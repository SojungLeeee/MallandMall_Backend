package com.exam.orderinfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.exam.inventory.ChangeType;
import com.exam.inventory.Inventory;
import com.exam.inventory.InventoryLog;
import com.exam.inventory.InventoryLogRepository;
import com.exam.inventory.InventoryService;
import com.exam.orderinfo.OrderInfo;
import com.exam.product.Product;
import com.exam.product.ProductRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

	private final IamportClient iamportClient;
	private final OrderInfoRepository orderInfoRepository;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;
	private OrderInfoDTO dto;
	private final InventoryLogRepository inventoryLogRepository;

	// verifyAndSaveOrder 메소드에 재고 차감 로직 추가
	@Override
	@Transactional
	public boolean verifyAndSaveOrder(OrderInfoDTO dto) {
		try {
			// 기존 결제 검증 코드 유지
			IamportResponse<Payment> response = iamportClient.paymentByImpUid(dto.getImpUid());

			if (response == null || response.getResponse() == null) {
				log.warn("결제 정보를 불러올 수 없습니다.");
				return false;
			}

			int paidAmount = response.getResponse().getAmount().intValue();
			String branchName = dto.getBranchName(); // 새로 추가된 필드

			// 지점명이 없으면 처리 불가
			if (branchName == null || branchName.isEmpty()) {
				log.warn("주문을 처리할 지점 정보가 없습니다.");
				return false;
			}

			if (dto.getOrders() != null && !dto.getOrders().isEmpty()) {
				// 여러 상품 주문 처리
				for (ProductOrderDTO item : dto.getOrders()) {
					// 재고 차감 시도
					boolean inventoryUpdated = inventoryService.updateInventory(
						branchName,
						item.getProductCode(),
						-item.getQuantity() // 음수로 전달하여 재고 차감
					);

					// 재고 부족 시 롤백
					if (!inventoryUpdated) {
						TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
						log.warn("재고 부족으로 주문을 처리할 수 없습니다: {}", item.getProductCode());
						return false;
					}
					List<InventoryLog> logs = inventoryLogRepository
						.findByProductCodeAndBranchNameOrderByChangeDateAsc(item.getProductCode(), branchName);

					int currentStock = 0;
					for (InventoryLog log : logs) {
						currentStock += (log.getChangeType() == ChangeType.IN)
							? log.getQuantity()
							: -log.getQuantity();
					}

					int updatedStock = currentStock - item.getQuantity();

					inventoryLogRepository.save(
						InventoryLog.builder()
							.productCode(item.getProductCode())
							.branchName(branchName)
							.changeType(ChangeType.OUT)
							.quantity(item.getQuantity())
							.remainingStock(updatedStock)
							.changeDate(LocalDateTime.now())
							.build()
					);

					// 이하 기존 주문 처리 코드
					Product product = productRepository.findByProductCode(item.getProductCode());
					if (product == null) {
						TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
						log.warn("상품을 찾을 수 없습니다: {}", item.getProductCode());
						return false;
					}

					int originalPrice = product.getPrice() * item.getQuantity();
					int discountPercent = dto.getDiscountedPrice();
					int itemPrice = (int)(originalPrice - (originalPrice * (discountPercent / 100.0)));

					OrderInfo order = OrderInfo.builder()
						.userId(dto.getUserId())
						.productCode(item.getProductCode())
						.quantity(item.getQuantity())
						.receiverName(dto.getReceiverName())
						.post(dto.getPost())
						.addr1(dto.getAddr1())
						.addr2(dto.getAddr2())
						.phoneNumber(dto.getPhoneNumber())
						.orderPrice(itemPrice)
						.impUid(dto.getImpUid())
						.orderDate(dto.getOrderDate())
						.branchName(branchName) // 주문 처리 지점 정보 저장
						.build();

					orderInfoRepository.save(order);
				}

				return true;
			} else {
				// 단일 상품 주문 처리

				// 재고 차감 시도
				boolean inventoryUpdated = inventoryService.updateInventory(
					branchName,
					dto.getProductCode(),
					-dto.getQuantity() // 음수로 전달하여 재고 차감
				);

				// 재고 부족 시 롤백
				if (!inventoryUpdated) {
					log.warn("재고 부족으로 주문을 처리할 수 없습니다: {}", dto.getProductCode());
					return false;
				}
				

				// 이하 기존 주문 처리 코드
				Product product = productRepository.findByProductCode(dto.getProductCode());
				if (product == null) {
					log.warn("상품을 찾을 수 없습니다.");
					return false;
				}

				List<InventoryLog> logs = inventoryLogRepository
					.findByProductCodeAndBranchNameOrderByChangeDateAsc(dto.getProductCode(), branchName);

				int currentStock = 0;
				for (InventoryLog log : logs) {
					currentStock += (log.getChangeType() == ChangeType.IN)
						? log.getQuantity()
						: -log.getQuantity();
				}
				// 로그 테이블에 로그 남기기
				int updatedStock = currentStock - dto.getQuantity();

				inventoryLogRepository.save(
					InventoryLog.builder()
						.productCode(dto.getProductCode())
						.branchName(branchName)
						.changeType(ChangeType.OUT)
						.quantity(dto.getQuantity())
						.remainingStock(updatedStock)
						.changeDate(LocalDateTime.now())
						.build()
				);

				int orderPrice = paidAmount;

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
					.orderDate(dto.getOrderDate())
					.branchName(branchName) // 주문 처리 지점 정보 저장
					.build();

				orderInfoRepository.save(order);
				return true;
			}

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