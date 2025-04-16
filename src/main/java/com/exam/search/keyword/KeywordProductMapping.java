package com.exam.search.keyword;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "keywordproductmapping")
@Getter
@Setter
public class KeywordProductMapping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mappingId;

	@ManyToOne
	@JoinColumn(name = "keywordId", nullable = false)
	private SearchKeyword searchKeyword;

	@Column(name = "productCode", nullable = false)
	private String productCode;

	@Column(name = "clickCount")
	private int clickCount = 1;

	@Column(name = "createdAt")
	private LocalDateTime createdAt;

	@Column(name = "updatedAt")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
