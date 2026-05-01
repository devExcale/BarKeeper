package dev.excale.barkeeper.config;

import dev.excale.barkeeper.notion.NotionPropertyIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class Jackson3Config {

	@Bean
	public ObjectMapper jacksonObjectMapper() {
		return JsonMapper.builder()
			.annotationIntrospector(new NotionPropertyIntrospector())
			.findAndAddModules()
			.build();
	}

}