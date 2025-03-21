package com.exam.adminbranch;

import java.util.List;

public interface EventService {
	//지점별 전체 이벤트 조회
	List<Event> getAllEventsByBranch(String branchName);

	// 이벤트 생성
	void createEvent(EventDTO dto);

	// 이벤트 수정
	void updateEvent(String eventId, EventDTO dto);

	// 이벤트 삭제
	void deleteEvent(String eventId);
}