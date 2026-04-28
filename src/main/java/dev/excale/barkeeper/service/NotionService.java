package dev.excale.barkeeper.service;

import dev.excale.barkeeper.client.NotionClient;
import dev.excale.barkeeper.client.SteamClient;
import dev.excale.barkeeper.notion.GamePage;
import dev.excale.barkeeper.steam.AppDetails;
import dev.excale.barkeeper.steam.Category;
import dev.excale.barkeeper.steam.Genre;
import dev.excale.barkeeper.steam.PriceOverview;
import dev.excale.barkeeper.util.SteamUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
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

	private final ObjectMapper objectMapper = new ObjectMapper();

	@EventListener
	public void onApplicationStart(ApplicationReadyEvent ignored) {
		String dbContent = notionClient.queryDataSource("21f0c276a8de8069a44f000b6de18485");
		parseAndUpdateRows(dbContent);
	}

	private void parseAndUpdateRows(String dbContent) {
		try {
			JsonNode root = objectMapper.readTree(dbContent);
			JsonNode resultsNode = root.get("results");

			if (resultsNode != null && resultsNode.isArray()) {
				for (JsonNode recordNode : resultsNode) {
					processRecord(recordNode);
				}
			}
		} catch (Exception e) {
			log.error("Failed to parse Notion datasource content", e);
		}
	}

	private void processRecord(JsonNode recordNode) {
		try {
			GamePage row = objectMapper.treeToValue(recordNode, GamePage.class);

			// Extract app ID from storePage using regex
			if (row.getStorePage() != null) {
				Matcher matcher = SteamUtil.REGEX_STEAM_STORE_URL.matcher(row.getStorePage());
				if (matcher.matches()) {
					String appId = matcher.group(1);
					log.info("Found app ID: {}", appId);
					fetchAndUpdateRow(row, appId);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to process record", e);
		}
	}

	private void fetchAndUpdateRow(GamePage row, String appId) {
		try {
			// Fetch app details from Steam
			AppDetails appDetails = steamClient.getAppDetails(appId);
			if (appDetails != null) {
				update(row, appDetails);
				notionClient.updatePage(row.getId().toString(), row);
			}
		} catch (Exception e) {
			log.warn("Failed to fetch Steam app details for app ID: {}", appId, e);
		}
	}

	private void update(GamePage row, AppDetails appDetails) {

		row.setStore("Steam");

		row.setCover(appDetails.getHeaderImage());

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
