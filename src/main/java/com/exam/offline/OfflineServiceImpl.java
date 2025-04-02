package com.exam.offline;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exam.category.Likecategories;
import com.exam.category.LikecategoriesRepository;
import com.exam.product.Product;
import com.exam.product.ProductDTO;
import com.exam.product.ProductRepository;

@Service
public class OfflineServiceImpl implements OfflineService {

	private final OfflineRepository offlineRepository;
	private final LikecategoriesRepository likecategoriesRepository;
	private final ProductRepository productRepository;// 수정된 repository 이름

	// 생성자 주입 방식으로 Repository 주입
	public OfflineServiceImpl(OfflineRepository offlineRepository, LikecategoriesRepository likecategoriesRepository, ProductRepository productRepository) {
		this.offlineRepository = offlineRepository;
		this.likecategoriesRepository = likecategoriesRepository;
		this.productRepository = productRepository;
	}

	@Override
	public List<OfflinePriceDTO> getPriceHistory(String productCode) {
		List<OfflinePrice> prices = offlineRepository.findByIdProductCodeOrderByIdPriceDateAsc(productCode);
		return prices.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
	}

	@Override
	public List<ProductDTO> getDiscountedProductsByUserCategory(String userId) {
		// userId를 기반으로 선호 카테고리 조회
		List<String> preferredCategories = likecategoriesRepository.findByUserId(userId).stream()
			.map(Likecategories::getCategory)  // Likecategories에서 카테고리 추출
			.collect(Collectors.toList());

		// 선호 카테고리 목록을 기준으로 products 테이블에서 상품 조회
		List<Product> products = productRepository.findByCategoryIn(preferredCategories);

		// 제품이 없으면 로그 추가
		if (products.isEmpty()) {
			System.out.println("No products found for categories: " + preferredCategories);
		}

		// offlineprice 테이블에 존재하는 상품만 필터링
		List<Product> filteredProducts = products.stream()
			.filter(product -> offlineRepository.existsByIdProductCode(product.getProductCode()))
			.collect(Collectors.toList());

		// 필터링된 상품 정보가 없으면 로그 추가
		if (filteredProducts.isEmpty()) {
			System.out.println("No discounted products found in offlineprice for categories: " + preferredCategories);
		}

		// 제품 정보를 ProductDTO로 변환하여 반환
		return filteredProducts.stream()
			.map(this::convertToProductDTO)
			.collect(Collectors.toList());
	}



	private OfflinePriceDTO convertToDTO(OfflinePrice offlinePrice) {
		OfflinePriceDTO dto = new OfflinePriceDTO();
		dto.setProductCode(offlinePrice.getId().getProductCode());
		dto.setPrice(offlinePrice.getPrice());
		dto.setPriceDate(offlinePrice.getId().getPriceDate());
		dto.setCategory(offlinePrice.getCategory());
		return dto;
	}

	private ProductDTO convertToProductDTO(Product product) {
		ProductDTO dto = new ProductDTO();
		dto.setProductCode(product.getProductCode());
		dto.setCategory(product.getCategory());
		dto.setProductName(product.getProductName());
		dto.setDescription(product.getDescription());
		dto.setPrice(product.getPrice());
		dto.setImage(product.getImage());
		return dto;
	}
}