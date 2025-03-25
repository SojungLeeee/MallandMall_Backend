package com.exam.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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

	@Override
	public List<LikecategoriesDTO> getCategoriesByUserId(String userId) {
		List<Likecategories> likecategoriesList = likecategoriesRepository.findByUserId(userId);
		List<LikecategoriesDTO> likecategoriesDTOList =
			likecategoriesList.stream().map(cg -> { //cg는 Likecategories
				LikecategoriesDTO dto = LikecategoriesDTO.builder()
					.categoryId(cg.getCategoryId())
					.userId(cg.getUserId())
					.category(cg.getCategory())
					.build();
				return dto;
			}).collect(Collectors.toList());

		return likecategoriesDTOList;
	}

	@Override
	@Transactional
	public void deleteCategoriesByUserId(String userId, String category) {
		// userId와 category로 해당 카테고리 데이터를 찾기
		Likecategories likecategories = likecategoriesRepository.findByUserIdAndCategory(userId, category)
			.orElseThrow(() -> new RuntimeException("선호 카테고리를 찾을 수 없습니다."));
		// 카테고리 삭제
		likecategoriesRepository.delete(likecategories);

	}

}