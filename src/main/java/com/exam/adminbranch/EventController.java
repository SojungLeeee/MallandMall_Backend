package com.exam.adminbranch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/event")
public class EventController {

	private final EventService eventService;

	@Autowired
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	// 지점별 이벤트 조회
	@GetMapping("/branch/{branchName}")
	public ResponseEntity<List<Event>> getEventsByBranch(@PathVariable String branchName) {
		List<Event> events = eventService.getAllEventsByBranch(branchName);
		return ResponseEntity.ok(events);
	}

	// 이벤트 생성
	@PostMapping
	public ResponseEntity<String> createEvent(@RequestBody EventDTO eventDTO) {
		try {
			Event event = Event.builder()
				.eventId(eventDTO.getEventId())
				.branchName(eventDTO.getBranchName())
				.eventTitle(eventDTO.getEventTitle())
				.category(eventDTO.getCategory())
				.build();

			eventService.createEvent(eventDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body("이벤트가 성공적으로 생성되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이벤트 생성 실패: " + e.getMessage());
		}
	}

	// 이벤트 수정
	@PutMapping("/{eventId}")
	public ResponseEntity<String> updateEvent(@PathVariable String eventId, @RequestBody EventDTO eventDTO) {
		try {
			Event event = Event.builder()
				.eventTitle(eventDTO.getEventTitle())
				.branchName(eventDTO.getBranchName())
				.build();

			eventService.updateEvent(eventId, eventDTO);
			return ResponseEntity.ok("이벤트가 성공적으로 수정되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이벤트 수정 실패: " + e.getMessage());
		}
	}

	// 이벤트 삭제
	@DeleteMapping("/{eventId}")
	public ResponseEntity<String> deleteEvent(@PathVariable String eventId) {
		try {
			eventService.deleteEvent(eventId);
			return ResponseEntity.ok("이벤트가 성공적으로 삭제되었습니다.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이벤트 삭제 실패: " + e.getMessage());
		}
	}
}