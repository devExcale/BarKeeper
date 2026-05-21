package dev.excale.barkeeper.notion;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import dev.excale.barkeeper.notion.codec.NotionFlatteningDecoder;
import dev.excale.barkeeper.notion.codec.NotionPropertyIntrospector;
import dev.excale.barkeeper.notion.codec.NotionUnflatteningEncoder;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
public class NotionFeignConfig {

	private final NotionProperties props;

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("Authorization", "Bearer " + props.apiKey());
			requestTemplate.header("Notion-Version", props.apiVersion());
			requestTemplate.header("Content-Type", "application/json");
		};
	}

	@Bean
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.annotationIntrospector(new NotionPropertyIntrospector())
			.changeDefaultVisibility(vc -> vc
				.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE)
				.withVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
				.withVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
				.withVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
			)
			.findAndAddModules()
			.build();
	}

	@Bean
	public Decoder notionDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters, ObjectMapper mapper) {

		// Build default spring feign decoder
		Decoder defaultSpringDecoder = new ResponseEntityDecoder(new SpringDecoder(messageConverters));

		// Wrap default decoder with notion flattening decoder
		return new NotionFlatteningDecoder(defaultSpringDecoder, mapper);
	}

	@Bean
	public Encoder notionEncoder(ObjectMapper mapper) {
		return new NotionUnflatteningEncoder(mapper);
	}

}
