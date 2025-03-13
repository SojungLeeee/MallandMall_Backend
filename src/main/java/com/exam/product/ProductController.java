package com.exam.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	//  전체 상품 목록 조회
	@GetMapping("/home")
	public ResponseEntity<List<ProductDTO>> getAllProducts() {
		List<ProductDTO> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	//  특정 상품 상세 조회
	@GetMapping("/detail/{productCode}")
	public ResponseEntity<ProductDTO> getProductDetail(@PathVariable String productCode) {
		ProductDTO product = productService.getProductByCode(productCode);
		return (product != null) ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
	}
}
