package dev.excale.barkeeper.client;

import dev.excale.barkeeper.config.FeignConfig;
import dev.excale.barkeeper.config.NotionFeignConfig;
import dev.excale.barkeeper.notion.GamePage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
	name = "notion-api-client",
	url = "https://api.notion.com",
	configuration = { NotionFeignConfig.class, FeignConfig.class }
)
public interface NotionClient {

	@GetMapping("/v1/data_sources/{id}")
	String retrieveDataSource(@PathVariable("id") String datasourceId);

	@PatchMapping("/v1/data_sources/{id}")
	String updateDataSource(@PathVariable("id") String datasourceId, String body);

	@PostMapping("/v1/data_sources/{id}/query")
	String queryDataSource(@PathVariable("id") String datasourceId);

	@PatchMapping("/v1/pages/{id}")
	GamePage updatePage(@PathVariable("id") String pageId, GamePage row);

}