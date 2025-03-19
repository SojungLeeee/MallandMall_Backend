package com.exam.admin;

import java.util.List;

import com.exam.product.ProductDTO;

public interface AdminService {

	List<ProductDTO> findAllProducts();

	List<GoodsDTO> findAllGoods();

	void addProduct(ProductDTO productDTO); //product 의 모든 정보를 넣어주어서 product 생성

	void deleteProduct(String productCode); //productCode 로 product 삭제

	void updateProduct(String productCode, ProductDTO productDTO); //productCode 로 product 수정

	void addGoods(GoodsDTO goodsDTO);

	void deleteGoods(int goodsId);

	void updateGoods(int goodsId, GoodsDTO goodsDTO);
}
