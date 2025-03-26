package com.exam.adminbranch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/event")
public class UserEventController {

	private final EventService eventService;

	@Autowired
	public UserEventController(EventService eventService) {
		this.eventService = eventService;
	}

	// "/favorites" 엔드포인트 추가
	@GetMapping("/favorites/{userId}")
	public ResponseEntity<List<EventDTO>> getFavoriteEvents(@PathVariable String userId) {
		// userId를 이용해 선호 카테고리로 이벤트를 가져오는 서비스 메서드 호출
		String authenticatedUserId = getAuthenticatedUserId();
		if (!authenticatedUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		List<EventDTO> events = eventService.getFavoriteEvents(userId);
		return ResponseEntity.ok(events);

	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getName();
	}

}