package com.exam.question;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "question")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long questionid;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionStatus status; // ✅ 상태 필드 추가

	@PrePersist
	protected void onCreate() {
		this.createDate = LocalDateTime.now();
		this.status = QuestionStatus.WAITING; // ✅ 기본 상태 설정
	}

	public enum QuestionStatus {
		WAITING,  // 대기
		ANSWERED  // 답변 완료
		, CHECKING
	}
}
