package com.exam.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.product.Product;
import com.exam.product.ProductDTO;

import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

	AdminRepositoryGoods adminRepositoryGoods;
	AdminRepositoryProducts adminRepositoryProducts;

	public AdminServiceImpl(AdminRepositoryGoods adminRepositoryGoods,
		AdminRepositoryProducts adminRepositoryProducts) {
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.adminRepositoryProducts = adminRepositoryProducts;
	}

	@Override
	public void addGoods(GoodsDTO goodsDTO) {
		Goods goods = Goods.builder()
			.productCode(goodsDTO.getProductCode())
			.branchName(goodsDTO.getBranchName())
			.expirationDate(goodsDTO.getExpirationDate())
			.build();
		adminRepositoryGoods.save(goods);
	}

	@Override
	@Transactional
	public void deleteGoods(int goodsId) {
		Goods goods = adminRepositoryGoods.findByGoodsId(goodsId).orElse(null);
		System.out.println("goods : " + goods);

		if (goods != null) {
			adminRepositoryGoods.delete(goods);
			// adminRepositoryProducts.deleteById(product.getId());
		}
	}

	@Override
	@Transactional
	public void updateGoods(int goodsId, GoodsDTO goodsDTO) {

		Goods goods = adminRepositoryGoods.findByGoodsId(goodsId).orElse(null);
		//ProductCode 찾으면 product로 리턴, 없으면 null 리턴

		if (goods != null) {
			goods.setProductCode(goodsDTO.getProductCode());
			goods.setBranchName(goodsDTO.getBranchName());
			goods.setExpirationDate(goodsDTO.getExpirationDate());
		}

	}

	@Override
	public List<ProductDTO> findAllProducts() {
		List<Product> productList = adminRepositoryProducts.findAll();

		//Stream API 의 Map 이용
		List<ProductDTO> productDTOList =
			productList.stream().map(t -> { //t는 Todo
				ProductDTO dto = ProductDTO.builder()
					.productCode(t.getProductCode())
					.category(t.getCategory())
					.productName(t.getProductName())
					.description(t.getDescription())
					.price(t.getPrice())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return productDTOList;
	}

	@Override
	public List<GoodsDTO> findAllGoods() {
		List<Goods> goodsList = adminRepositoryGoods.findAll();

		//Stream API 의 Map 이용
		List<GoodsDTO> goodsDTOList =
			goodsList.stream().map(t -> { //t는 Todo
				GoodsDTO dto = GoodsDTO.builder()
					.goodsId(t.getGoodsId())
					.productCode(t.getProductCode())
					.branchName(t.getBranchName())
					.expirationDate(t.getExpirationDate())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return goodsDTOList;
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
		System.out.println("product : " + product);
		//productCode 찾으면 product 리턴, 없으면 null 리턴

		if (product != null) {
			adminRepositoryProducts.delete(product);
			// adminRepositoryProducts.deleteById(product.getId());
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
