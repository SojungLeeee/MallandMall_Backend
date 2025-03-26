package com.exam.question;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
		this.status = QuestionStatus.PENDING; // ✅ 기본 상태 설정
	}

	public enum QuestionStatus {
		PENDING,  // 대기
		ANSWERED  // 답변 완료
	}
}
