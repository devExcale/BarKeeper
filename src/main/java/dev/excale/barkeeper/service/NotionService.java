package dev.excale.barkeeper.service;

import dev.excale.barkeeper.client.NotionClient;
import dev.excale.barkeeper.client.SteamClient;
import dev.excale.barkeeper.notion.GamePage;
import dev.excale.barkeeper.steam.AppDetails;
import dev.excale.barkeeper.steam.Category;
import dev.excale.barkeeper.steam.Genre;
import dev.excale.barkeeper.steam.PriceOverview;
import dev.excale.barkeeper.util.SteamUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
public class NotionService {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final NotionClient notionClient;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final SteamClient steamClient;

	private final UUID botId = UUID.fromString("34f0c276-a8de-81b7-bd01-0027387fb7d2");

	@EventListener
	public void onApplicationStart(ApplicationReadyEvent ignored) {
		List<GamePage> rows = notionClient.queryDataSource("21f0c276a8de8069a44f000b6de18485");
		for(GamePage row : rows) {

			Instant lastEditedDay = row.getLastEditedAt().truncatedTo(ChronoUnit.DAYS);
			Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
			if(!lastEditedDay.isBefore(today) && botId.equals(row.getLastEditedBy()))
				continue;

			processPage(row);
		}
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
				update(row, appDetails);
				notionClient.updatePage(row.getId().toString(), row);
			}
		} catch (FeignException e) {
			log.warn("Failed to fetch Steam app details for app ID: {}", appId, e);
			log.warn(new String(e.request().body(), StandardCharsets.UTF_8));
			log.warn(e.responseBody().map(byteBuffer -> new String(byteBuffer.array(), StandardCharsets.UTF_8)).orElse("No response body"));
		}
	}

	private void update(GamePage row, AppDetails appDetails) {

		row.setStore("Steam");

		row.setCover(appDetails.getHeaderImage());

		row.setTitle(List.of(appDetails.getName()));

		PriceOverview price = appDetails.getPriceOverview();
		if(price != null) {
			row.setFullPrice(price.getInitialPrice() / 100d);
			row.setDiscountPrice(price.getFinalPrice() / 100d);
		} else {
			row.setFullPrice(0d);
			row.setDiscountPrice(0d);
		}

		row.setReleaseDate(appDetails.getReleaseDate());

		row.setGenres(
			appDetails.getGenres()
				.stream()
				.map(Genre::getDescription)
				.collect(Collectors.toSet())
		);

		row.setCategories(
			appDetails.getCategories()
				.stream()
				.map(Category::getDescription)
				.collect(Collectors.toSet())
		);

		row.setDevelopers(new HashSet<>(appDetails.getDevelopers()));

		row.setPublishers(new HashSet<>(appDetails.getPublishers()));

	}

}
