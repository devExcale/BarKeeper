package dev.excale.barkeeper.steam;

import dev.excale.barkeeper.config.FeignConfig;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.steam.model.StoreSearch;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
	name = "steam-api-client",
	url = "https://store.steampowered.com/api",
	configuration = FeignConfig.class
)
public interface SteamClient {

	@GetMapping("/appdetails/?appids={appid}&l=english&cc=IT")
	AppDetails getAppDetails(@PathVariable("appid") String appId);

	@GetMapping("/storesearch/?term={term}&l=english&cc=IT")
	StoreSearch getStoreSearch(@PathVariable("term") String term);

}
