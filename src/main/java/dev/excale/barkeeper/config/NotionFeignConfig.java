package dev.excale.barkeeper.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotionFeignConfig {

	@Value("${notion.api.token}")
	private String notionToken;

	@Value("${notion.api.version:2026-03-11}")
	private String notionVersion;

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("Authorization", "Bearer " + notionToken);
			requestTemplate.header("Notion-Version", notionVersion);
			requestTemplate.header("Content-Type", "application/json");
		};
	}

}