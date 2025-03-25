package com.exam.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	//선호 카테고리 저장 (회원가입 페이지 이후에서)
	@PostMapping("/saveLikeCategories")
	public ResponseEntity<LikecategoriesDTO> saveLikeCategories(@Valid @RequestBody LikecategoriesDTO dto) {
		likecategoriesService.addCategories(dto);
		return ResponseEntity.created(null).body(dto);  // 201 상태코드 반환됨.
	}

	// 마이페이지에서 선호 카테고리 수정 시 필요한 userId당 선호카테고리 전체 보기
	@GetMapping("/showLikeCategories/{userId}")
	public ResponseEntity<List<LikecategoriesDTO>> showLikeCategories(@PathVariable String userId) {
		List<LikecategoriesDTO> likecategoriesDTOList = likecategoriesService.getCategoriesByUserId(userId);
		return ResponseEntity.ok(likecategoriesDTOList);
	}

	//userId, category 를 확인하고 삭제
	@DeleteMapping("/deleteLikeCategories/{userId}/{category}")
	public ResponseEntity<LikecategoriesDTO> deleteCategory(@PathVariable String userId,
		@PathVariable String category) {
		likecategoriesService.deleteCategoriesByUserId(userId, category); // 카테고리 삭제 서비스 호출
		return ResponseEntity.ok().build();  // 상태 코드 200 OK
	}

}
