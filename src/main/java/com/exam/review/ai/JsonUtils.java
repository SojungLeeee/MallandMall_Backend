package com.exam.review.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	// 객체를 JSON 문자열로 변환
	public static String toJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			log.error("JSON 변환 중 오류: {}", e.getMessage(), e);
			return "{}";
		}
	}

	// JSON 문자열을 Map으로 변환
	public static Map<String, Double> parseJsonToMap(String json) {
		try {
			if (json == null || json.isEmpty()) {
				return Collections.emptyMap();
			}
			return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 중 오류: {}", e.getMessage(), e);
			return Collections.emptyMap();
		}
	}

	// JSON 문자열을 String 리스트로 변환
	public static List<String> parseJsonToStringList(String json) {
		try {
			if (json == null || json.isEmpty()) {
				return Collections.emptyList();
			}
			return objectMapper.readValue(json, new TypeReference<List<String>>() {});
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 중 오류: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	// JSON 문자열을 ReviewCategoryDTO 리스트로 변환
	public static List<ReviewCategoryDTO> parseJsonToCategories(String json) {
		try {
			if (json == null || json.isEmpty()) {
				return Collections.emptyList();
			}
			return objectMapper.readValue(json, new TypeReference<List<ReviewCategoryDTO>>() {});
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 중 오류: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}
}