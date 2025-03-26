package com.exam.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AnswerDTO {

	private Long answerId;  // 답변 ID
	private Long questionId;  // 질문 ID
	private String content;  // 답변 내용
	private String userId;  // 답변을 작성한 관리자 userId
	private LocalDateTime createDate;  // 답변 작성일
}
