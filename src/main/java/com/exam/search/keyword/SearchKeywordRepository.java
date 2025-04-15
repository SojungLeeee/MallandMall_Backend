package com.exam.search.keyword;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
	Optional<SearchKeyword> findByKeyword(String keyword);

	@Query("SELECT s FROM SearchKeyword s ORDER BY s.searchCount DESC")
	List<SearchKeyword> findTopKeywords(Pageable pageable);
}

