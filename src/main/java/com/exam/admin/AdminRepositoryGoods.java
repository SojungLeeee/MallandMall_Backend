package com.exam.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.product.Product;

public interface AdminRepositoryGoods extends JpaRepository<Product, String> {
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

		/*
			관리자가
			상품 코드 등록(추가)/수정/삭제, - product 테이블 사용
			개별 상품 등록(추가)/수정/삭제, - goods 테이블 사용
			행사     등록(추가)/수정/삭제, - 새로 테이블 생성해야함... 행사 테이블 - event 테이블
			지점     등록(추가)/수정/삭제 기능 구현해야 함 - branch 테이블 사용
		*/
}
