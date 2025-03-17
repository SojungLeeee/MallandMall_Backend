package com.exam.admin;

import org.springframework.stereotype.Service;

import com.exam.product.Product;
import com.exam.product.ProductDTO;

import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

	AdminRepositoryProducts adminRepositoryProducts;

	public AdminServiceImpl(AdminRepositoryProducts adminRepositoryProducts) {
		this.adminRepositoryProducts = adminRepositoryProducts;
	}

	@Override
	@Transactional
	public void addProduct(ProductDTO productDTO) {
		Product product = Product.builder()
			.productCode(productDTO.getProductCode())
			.category(productDTO.getCategory())
			.productName(productDTO.getProductName())
			.description(productDTO.getDescription())
			.price(productDTO.getPrice())
			.image(productDTO.getImage())
			.build();

		adminRepositoryProducts.save(product);
	}

	@Override
	@Transactional
	public void deleteProduct(String productCode) {

		Product product = adminRepositoryProducts.findByProductCode(productCode).orElse(null);
		//productCode 찾으면 product 리턴, 없으면 null 리턴

		if (product != null) {
			adminRepositoryProducts.delete(product);
			//			todoRepository.deleteById(id);
		}
	}

	@Override
	@Transactional
	public void updateProduct(String productCode, ProductDTO productDTO) {
		//작업 순서
		/*
		  1. id에 해당되는 엔티티 찾기
		  2. 찾은 엔티티를 dto 값으로 수정
		  3. 더티체킹으로 인해서 자동으로 수정
		  	 또는 명시적으로 save(엔티티) 호출도 가능
		 */

		Product product = adminRepositoryProducts.findByProductCode(productCode).orElse(null);
		//ProductCode 찾으면 product로 리턴, 없으면 null 리턴

		if (product != null) {
			product.setCategory(productDTO.getCategory());
			product.setProductName(productDTO.getProductName());
			product.setDescription(productDTO.getDescription());
			product.setPrice(productDTO.getPrice());
			product.setImage(productDTO.getImage());
		}

	}
}
