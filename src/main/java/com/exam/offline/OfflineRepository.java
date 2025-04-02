package com.exam.offline;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exam.product.Product;

public interface OfflineRepository extends JpaRepository<OfflinePrice, OfflinePriceId> {

	//List<Product> findByCategoryIn(List<String> categories);
	@Query("SELECT p FROM Product p JOIN OfflinePrice op ON p.productCode = op.id.productCode WHERE p.category IN :categories")
	List<Product> findDiscountedProductsByCategories(@Param("categories") List<String> categories);

	boolean existsByIdProductCode(String productCode);
	// 특정 상품 코드에 대한 가격 변동 데이터를 날짜순으로 조회
	List<OfflinePrice> findByIdProductCodeOrderByIdPriceDateAsc(String productCode);
}
