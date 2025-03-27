package com.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

	@Bean
	//(name = "restTemplate")
	public RestTemplate restTemplate() {
		// ObjectMapper objectMapper = new ObjectMapper();
		// MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
		// RestTemplate restTemplate = new RestTemplate(Collections.singletonList(converter));
		return new RestTemplate();
	}
}
