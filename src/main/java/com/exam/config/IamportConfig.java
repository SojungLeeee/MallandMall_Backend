package com.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siot.IamportRestClient.IamportClient;

@Configuration
public class IamportConfig {

	@Value("0020266371022425")
	private String apiKey;

	@Value("xfeF1iM90zvEisoRGcRyVPP7M3ef69lbLxzXgngxUDHwGtOoqYNMMHbiVI2bJcaGiT6acOwl6YzM93WL")
	private String apiSecret;

	@Bean
	public IamportClient iamportClient() {
		return new IamportClient(apiKey, apiSecret);
	}
}
