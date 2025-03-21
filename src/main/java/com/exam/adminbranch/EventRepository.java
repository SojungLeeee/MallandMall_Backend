package com.exam.adminbranch;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EventRepository  extends JpaRepository<Event, String> {
	// 지점별 이벤트 조회 메소드 추가
	List<Event> findByBranchName(String branch);

	// 기본 CRUD 메서드는 JpaRepository에서 자동으로 제공된다... 혁신
	// save(Event entity) - 생성 및 수정
	// findById(Integer id) - ID로 조회
	// deleteById(Integer id) - ID로 삭제
	// existsById(Integer id) - ID로 존재 여부 확인

	}
