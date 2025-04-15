package com.exam.adminbranch;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

	//이벤트 조회
	List<Event> getAllEvent();

	// 이벤트 생성
	void createEvent(EventDTO dto);

	// 이벤트 수정
	void updateEvent(String eventId, EventDTO dto);

	// 이벤트 삭제
	void deleteEvent(String eventId);

	// 사용자 ID로 선호 카테고리와 관련된 이벤트를 가져오는 메소드
	List<EventDTO> getFavoriteEvents(String userId);

	//쿠폰 자동삭제 로직
	void deleteExpiredEvent(LocalDateTime now);
}