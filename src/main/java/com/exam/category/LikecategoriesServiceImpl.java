package com.exam.category;

import org.springframework.stereotype.Service;

@Service
public class LikecategoriesServiceImpl implements LikecategoriesService {

	LikecategoriesRepository likecategoriesRepository;

	public LikecategoriesServiceImpl(LikecategoriesRepository likecategoriesRepository) {
		this.likecategoriesRepository = likecategoriesRepository;
	}

	@Override
	public void addCategories(LikecategoriesDTO dto) {
		Likecategories likecategories = Likecategories.builder()
			.userId(dto.getUserId())
			.category(dto.getCategory())
			.build();

		Likecategories saveLikeCategories = likecategoriesRepository.save(likecategories);  // 데이터베이스에 저장
	}
}