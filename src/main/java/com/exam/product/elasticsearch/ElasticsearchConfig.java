package com.exam.product.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//엘라스틱서치 환경설정
//엘라스틱서치 controller는 productController에 위치
@Configuration
public class ElasticsearchConfig {

	@Value("${elasticsearch.host:localhost}")
	private String host;

	@Value("${elasticsearch.port:9200}")
	private int port;

	@Value("${elasticsearch.username:}")
	private String username;

	@Value("${elasticsearch.password:}")
	private String password;

	@Bean
	public RestClient restClient() {
		RestClientBuilder builder = RestClient.builder(new HttpHost(host, port));

		// 인증 정보가 있는 경우 설정
		if (username != null && !username.isEmpty()) {
			final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(
				new AuthScope(host, port),
				new UsernamePasswordCredentials(username, password)
			);

			builder.setHttpClientConfigCallback(
				(HttpAsyncClientBuilder clientBuilder) -> clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
			);
		}

		return builder.build();
	}

	@Bean
	public ElasticsearchTransport elasticsearchTransport() {
		return new RestClientTransport(
			restClient(),
			new JacksonJsonpMapper()
		);
	}

	@Bean
	public ElasticsearchClient elasticsearchClient() {
		return new ElasticsearchClient(elasticsearchTransport());
	}
}