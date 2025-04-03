package com.exam.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogDTO {

	private Long logId;

	private String productCode;

	private String branchName;

	private ChangeType changeType; // IN or OUT

	private int quantity;

	private LocalDateTime changeDate;

	private Integer remainingStock;// 문자열로 포맷된 날짜 (프론트에서 보기 좋게)
}
