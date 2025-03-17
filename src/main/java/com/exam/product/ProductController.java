package com.exam.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	//카테고리별 상품 조회
	@GetMapping("/{category}")
	public ResponseEntity<List<ProductDTO>> getCategoryProducts(@PathVariable String category) {
		List<ProductDTO> products = productService.getProductsByCategory(category);
		return ResponseEntity.ok(products);
	}

	@GetMapping("/search/{productName}")
	public ResponseEntity<List<ProductDTO>> searchProductsByName(@PathVariable String productName) {
		List<ProductDTO> products = productService.getProductsByName(productName);
		return (products.isEmpty()) ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

}
