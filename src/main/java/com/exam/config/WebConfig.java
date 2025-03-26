package com.exam.config;

import java.util.Collections;

import org.checkerframework.checker.units.qual.C;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class WebConfig {

	@Bean(name = "restTemplate")
	public RestTemplate restTemplate() {
		ObjectMapper objectMapper = new ObjectMapper();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
		RestTemplate restTemplate = new RestTemplate(Collections.singletonList(converter));
		return restTemplate;
	}
}
