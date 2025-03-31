package com.exam.Cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

	    	/*
		 JpaRepository에서 기본으로 제공하는 CRUD 메서드 사용 가능

		- 전체 엔티티 조회 : findAll() - 리턴타입: List
		- 특정 엔티티 조회 : findById(ID id) - 리턴타입: Optional
		- 엔티티 저장 : save(entity)
		- 전체 엔티티 삭제 : deleteAll()
		- 특정 엔티티 id로 삭제 : deleteById(ID id)
		- 특정 엔티티 엔티티로 삭제 : delete(T entity)
		- 엔티티 수정 : 메서드 지원 없이 더티체킹 이용
		- 엔티티 갯수 : count()
	    */

	// 특정 사용자의 장바구니 목록 조회
	List<Cart> findByUserId(String userId);

	void deleteByUserIdAndProductCode(String userId, String productCode);

	// 특정 사용자의 장바구니에서 특정 상품 찾기
	Cart findByUserIdAndProductCode(String userId, String productCode);

	// cartId로 여러 개의 장바구니 항목 삭제
	void deleteByCartIdIn(List<Integer> cartIds);  // cartIds 리스트를 받아서 삭제
}