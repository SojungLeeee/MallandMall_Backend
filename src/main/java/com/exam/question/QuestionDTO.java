package com.exam.question;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class QuestionDTO {
	private Long questionId;
	private String userId;
	private String title;
	private String content;
	private LocalDateTime createDate;
	private String status;
}
