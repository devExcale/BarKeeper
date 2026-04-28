package dev.excale.barkeeper.client;

import dev.excale.barkeeper.config.FeignConfig;
import dev.excale.barkeeper.steam.AppDetails;
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

}