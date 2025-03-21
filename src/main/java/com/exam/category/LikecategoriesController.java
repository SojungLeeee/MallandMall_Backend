package com.exam.category;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/likecategories")
@Slf4j
public class LikecategoriesController {


		/*
			관리자가
			상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
			개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
			행사 등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블
			지점 등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
		*/

	LikecategoriesService likecategoriesService;

	public LikecategoriesController(LikecategoriesService likecategoriesService) {
		this.likecategoriesService = likecategoriesService;
	}

	@PostMapping("/saveLikeCategories")
	public ResponseEntity<LikecategoriesDTO> saveLikeCategories(@Valid @RequestBody LikecategoriesDTO dto) {
		likecategoriesService.addCategories(dto);
		return ResponseEntity.created(null).body(dto);  // 201 상태코드 반환됨.
	}

}
