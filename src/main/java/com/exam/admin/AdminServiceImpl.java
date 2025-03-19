package com.exam.admin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.exam.adminbranch.Branch;
import com.exam.adminbranch.BranchRepository;
import com.exam.inventory.Inventory;
import com.exam.inventory.InventoryRepository;
import com.exam.product.Product;
import com.exam.product.ProductDTO;

import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

	AdminRepositoryGoods adminRepositoryGoods;
	AdminRepositoryProducts adminRepositoryProducts;
	InventoryRepository inventoryRepository;
	BranchRepository branchRepository;

	public AdminServiceImpl(AdminRepositoryGoods adminRepositoryGoods,
		AdminRepositoryProducts adminRepositoryProducts,
		InventoryRepository inventoryRepository,
		BranchRepository branchRepository) {
		this.adminRepositoryGoods = adminRepositoryGoods;
		this.adminRepositoryProducts = adminRepositoryProducts;
		this.inventoryRepository = inventoryRepository;
		this.branchRepository = branchRepository;
	}

	@Override
	public void addGoods(GoodsDTO goodsDTO) {
		// 1. 지점이 존재하는지 확인
		Optional<Branch> branch = branchRepository.findByBranchName(goodsDTO.getBranchName());

		// 지점이 존재하지 않으면, 외래키 제약 조건 오류 발생
		if (!branch.isPresent()) {
			throw new RuntimeException("존재하지 않는 지점명입니다. 지점명을 확인해주세요." + goodsDTO.getBranchName());
		}

		// 2. 지점이 존재하면 Goods 추가
		Goods goods = Goods.builder()
			.productCode(goodsDTO.getProductCode())
			.branchName(goodsDTO.getBranchName())
			.expirationDate(goodsDTO.getExpirationDate())
			.build();
		adminRepositoryGoods.save(goods);

		// 3. Inventory 테이블에서 해당 productCode와 branchName을 가진 레코드 확인
		Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(
			goodsDTO.getProductCode(), goodsDTO.getBranchName());

		if (inventory == null) {
			// 4. Inventory에 해당 레코드가 없으면 새로 추가 (quantity는 1로 설정)
			Inventory newInventory = Inventory.builder()
				.productCode(goodsDTO.getProductCode())
				.branchName(goodsDTO.getBranchName())
				.quantity(1) // 처음 들어온 상품이므로 quantity는 1
				.build();
			inventoryRepository.save(newInventory);
		} else {
			// 5. Inventory에 해당 레코드가 있으면 수량을 1 증가시킴
			inventory.setQuantity(inventory.getQuantity() + 1); // quantity 1 증가
			inventoryRepository.save(inventory);  // 갱신된 정보 저장
		}
	}

	@Override
	@Transactional
	public void deleteGoods(int goodsId) {
		Goods goods = adminRepositoryGoods.findByGoodsId(goodsId).orElse(null);

		if (goods != null) {
			adminRepositoryGoods.delete(goods);
			// adminRepositoryProducts.deleteById(product.getId());
		}

		// 2. Inventory 테이블에서 해당 productCode와 branchName을 가진 레코드 확인
		Inventory inventory = inventoryRepository.findByProductCodeAndBranchName(
			goods.getProductCode(), goods.getBranchName());

		// 2-1. Inventory의 quantity가 1 이상이면 1 감소
		if (inventory.getQuantity() > 1) {
			inventory.setQuantity(inventory.getQuantity() - 1); // quantity 1 감소
			inventoryRepository.save(inventory);  // 갱신된 정보 저장
		} else {
			// 2-2. Inventory의 quantity가 1이면 해당 레코드 삭제
			inventoryRepository.delete(inventory);  // 해당 레코드 삭제
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
