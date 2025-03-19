package com.exam.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler
	extends ResponseEntityExceptionHandler {
	// 사용자 입력 데이터 유효성 예외 처리하는 메서드를 재정의함.

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
		HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.info("logger:유효성 예외처리.: {}", ex.getMessage());
		// 에러메시지 저장할 ErrorDetails 생성
		// ex.getMessage() 하면 출력되는 에러메시지가 너무 많다.
		// ex.getFieldError().getDefaultMessage() 하면 예외 발생된 개별 에러메시지만 출력됨.
		ErrorDetails errorDetails =
			new ErrorDetails(ex.getFieldError().getDefaultMessage(), LocalDate.now(), request.getDescription(false));

		return new ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST);  // 400 에러

	}//end handleMethodArgumentNotValid

	//이부분 다시 보아야함

	@ExceptionHandler(value = {SQLIntegrityConstraintViolationException.class, ConstraintViolationException.class})
	public ResponseEntity<ErrorDetails> errorPage(Exception e) {
		log.info("logger:중복 예외처리.: {}", e.getMessage());

		// 예외 메시지에서 "user" 또는 "products" 키워드를 포함하는지 체크
		String message = e.getMessage();

		ErrorDetails errorDetails;

		// 메시지에 "user.PRIMARY"가 포함되어 있으면 사용자 관련 에러
		if (message.contains("user.PRIMARY")) {
			errorDetails = new ErrorDetails("아이디 중복", LocalDate.now(), "사용자 userId를 다시 확인하세요");
		}
		// 메시지에 "products.PRIMARY"가 포함되어 있으면 상품 관련 에러
		else if (message.contains("products.PRIMARY")) {
			errorDetails = new ErrorDetails("상품 중복", LocalDate.now(), "상품 productCode 가 중복되었습니다. 다시 확인하세요");
		}
		// 기본적으로 일반적인 예외 메시지
		else {
			errorDetails = new ErrorDetails("알 수 없는 오류", LocalDate.now(), "예상치 못한 오류가 발생했습니다");
		}

		// 500 에러와 함께 ErrorDetails를 반환
		return ResponseEntity.status(500).body(errorDetails);
	}

}

// 에러메시지 저장 클래스
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
class ErrorDetails {

	String message;
	LocalDate timestamp;
	String detail;

}




