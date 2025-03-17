package com.exam.admin;

import com.exam.product.ProductDTO;

public interface AdminService {

	void addProduct(ProductDTO productDTO); //product 의 모든 정보를 넣어주어서 product 생성

	void deleteProduct(String productCode); //productCode 로 product 삭제

	void updateProduct(String productCode, ProductDTO productDTO); //productCode 로 product 수정

}
