package com.exam.category;

import java.util.List;

public interface LikecategoriesService {

	//
	void addCategories(LikecategoriesDTO dto);

	//userId로 선호 카테고리 목록 보기
	List<LikecategoriesDTO> getCategoriesByUserId(String userId);

	//선호카테고리 삭제
	void deleteCategoriesByUserId(String userId, String category);
}
