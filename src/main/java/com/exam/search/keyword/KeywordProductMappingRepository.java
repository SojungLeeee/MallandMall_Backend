package com.exam.search.keyword;


import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordProductMappingRepository extends JpaRepository<KeywordProductMapping, Long> {
	@Query("SELECT k FROM KeywordProductMapping k WHERE k.searchKeyword.keyword = :keyword ORDER BY k.clickCount DESC")
	List<KeywordProductMapping> findByKeywordOrderByClickCountDesc(@Param("keyword") String keyword, Pageable pageable);

	Optional<KeywordProductMapping> findBySearchKeyword_KeywordIdAndProductCode(Long keywordId, String productCode);
}
