package com.exam.adminbranch;

import java.net.URI;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/proxy/naver-map")
public class NaverMapProxyController {

	@Value("${naver.map.api.client-id}")
	private String naverClientId;

	private final RestTemplate restTemplate;

	public NaverMapProxyController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/maps.js")
	public ResponseEntity<String> getNaverMapScript() {
		System.out.println("네이버 맵 스크립트 요청 받음");
		URI uri = UriComponentsBuilder
			.fromUriString("https://openapi.map.naver.com/openapi/v3/maps.js")
			.queryParam("ncpClientId", naverClientId)
			.build()
			.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.parseMediaType("application/javascript")));

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
			uri,
			HttpMethod.GET,
			entity,
			String.class
		);

		return ResponseEntity.status(response.getStatusCode())
			.contentType(MediaType.parseMediaType("application/javascript"))
			.body(response.getBody());
	}
}