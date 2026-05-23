package dev.excale.barkeeper.sync;

import dev.excale.barkeeper.notion.NotionClient;
import dev.excale.barkeeper.notion.NotionProperties;
import dev.excale.barkeeper.steam.SteamClient;
import dev.excale.barkeeper.notion.model.GamePage;
import dev.excale.barkeeper.steam.SteamUtil;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.steam.model.Category;
import dev.excale.barkeeper.steam.model.Genre;
import dev.excale.barkeeper.steam.model.PriceOverview;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class SteamNotionSyncService {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final NotionClient notionClient;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final SteamClient steamClient;

	private final NotionProperties props;

	private final GamePageMapper gamePageMapper;

	@EventListener
	public void onApplicationStart(ApplicationReadyEvent ignored) {
		sync();
	}

	// 1:30 PM every day
	@Scheduled(cron = "0 30 13 * * *")
	public void sync() {

		log.info("Starting Steam-Notion sync...");

		UUID botId = props.botUuid();

		List<GamePage> rows = notionClient.queryDataSource(props.datasourceId());
		for(GamePage row : rows) {

			Instant lastEditedDay = row.getLastEditedAt().truncatedTo(ChronoUnit.DAYS);
			Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
			if(!lastEditedDay.isBefore(today) && botId.equals(row.getLastEditedBy()))
				continue;

			processPage(row);
		}

		log.info("Steam-Notion sync completed.");

	}

	private void processPage(GamePage row) {

		if(row.getStorePage() == null)
			return;

		Matcher matcher = SteamUtil.REGEX_STEAM_STORE_URL.matcher(row.getStorePage());
		if(!matcher.matches())
			return;

		// Extract app ID from storePage using regex
		String appId = matcher.group(1);
		log.info("Found app ID: {}", appId);
		fetchAndUpdateRow(row, appId);

	}

	private void fetchAndUpdateRow(GamePage row, String appId) {
		try {
			// Fetch app details from Steam
			AppDetails appDetails = steamClient.getAppDetails(appId);
			if (appDetails != null) {
				gamePageMapper.update(row, appDetails);
				notionClient.updatePage(row.getId().toString(), row);
			}
		} catch (FeignException e) {
			log.warn("Failed to fetch Steam app details for app ID: {}", appId, e);
			log.warn(new String(e.request().body(), StandardCharsets.UTF_8));
			log.warn(e.responseBody().map(byteBuffer -> new String(byteBuffer.array(), StandardCharsets.UTF_8)).orElse("No response body"));
		}
	}

}
