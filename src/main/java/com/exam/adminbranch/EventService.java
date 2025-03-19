package com.exam.adminbranch;

import java.util.List;

public interface EventService {
	//지점 조회
	List<Event> getEventsByBranch(String branch);

	// 이벤트 생성
	void createEvent(EventDTO dto);

	// 이벤트 수정
	void updateEvent(String eventId, EventDTO dto);

	// 이벤트 삭제
	void deleteEvent(String eventId);
}