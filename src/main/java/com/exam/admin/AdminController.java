package com.exam.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.product.ProductDTO;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {
		/*
			관리자가
			상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
			개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
			행사     등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블
			지점     등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
		*/

	AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@PostMapping("/addProductCode")
	public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO) {
		adminService.addProduct(productDTO);
		return ResponseEntity.created(null).body(productDTO);  // 201 상태코드 반환됨.
	}

	@DeleteMapping("/deleteProductCode/{productCode}")
	public ResponseEntity<ProductDTO> deleteProduct(@RequestBody String productCode) {
		adminService.deleteProduct(productCode);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/updateProductCode")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable String productName,
		@RequestBody ProductDTO productDTO) {
		adminService.updateProduct(productName, productDTO);
		return ResponseEntity.status(201).body(productDTO); //201
	}

}
