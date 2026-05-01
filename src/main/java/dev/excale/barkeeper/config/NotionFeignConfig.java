package dev.excale.barkeeper.config;

import dev.excale.barkeeper.notion.NotionFlatteningDecoder;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

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

	@Bean
	public Decoder notionDecoder(Decoder defaultDecoder, ObjectMapper objectMapper) {
		return new NotionFlatteningDecoder(defaultDecoder, objectMapper);
	}

}