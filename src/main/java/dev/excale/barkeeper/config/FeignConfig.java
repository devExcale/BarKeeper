package dev.excale.barkeeper.config;

import feign.Client;
import feign.okhttp.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FeignConfig {

	@Bean
	@Primary
	public Client client() {
		okhttp3.OkHttpClient okHttpClient = new Builder().build();
		return new OkHttpClient(okHttpClient);
	}

}