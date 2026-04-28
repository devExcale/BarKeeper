package dev.excale.barkeeper.steam;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class AppDetailsDeserializer extends StdDeserializer<AppDetails> {

	public AppDetailsDeserializer() {
		super(AppDetails.class);
	}

	@Override
	public AppDetails deserialize(JsonParser jp, DeserializationContext ctx) {

		// Read the JSON tree
		JsonNode node = ctx.readTree(jp);

		// Unwrap app details - the top level has the app ID as key
		String appId = node.propertyNames()
			.stream()
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Unexpected JSON structure: no app ID found"));

		JsonNode detailsNode = node.get(appId).get("data");
		AppDetails dto = new AppDetails();

		// Map all fields from JSON to AppDetails
		if (detailsNode.has("type"))
			dto.setType(detailsNode.get("type").asString());

		if (detailsNode.has("name"))
			dto.setName(detailsNode.get("name").asString());

		if (detailsNode.has("steam_appid"))
			dto.setSteamAppid(detailsNode.get("steam_appid").asInt());

		if (detailsNode.has("required_age"))
			dto.setRequiredAge(detailsNode.get("required_age").asInt());

		if (detailsNode.has("is_free"))
			dto.setIsFree(detailsNode.get("is_free").asBoolean());

		if (detailsNode.has("detailed_description"))
			dto.setDetailedDescription(detailsNode.get("detailed_description").asString());

		if (detailsNode.has("about_the_game"))
			dto.setAboutTheGame(detailsNode.get("about_the_game").asString());

		if (detailsNode.has("short_description"))
			dto.setShortDescription(detailsNode.get("short_description").asString());

		if (detailsNode.has("supported_languages"))
			dto.setSupportedLanguages(detailsNode.get("supported_languages").asString());

		if (detailsNode.has("header_image"))
			dto.setHeaderImage(detailsNode.get("header_image").asString());

		if (detailsNode.has("capsule_image"))
			dto.setCapsuleImage(detailsNode.get("capsule_image").asString());

		if (detailsNode.has("capsule_imagev5"))
			dto.setCapsuleImageV5(detailsNode.get("capsule_imagev5").asString());

		if (detailsNode.has("website"))
			dto.setWebsite(detailsNode.get("website").asString());

		if (detailsNode.has("developers"))
			dto.setDevelopers(
				detailsNode.get("developers")
					.valueStream()
					.map(JsonNode::stringValue)
					.toList()
			);

		if (detailsNode.has("publishers"))
			dto.setPublishers(
				detailsNode.get("publishers")
					.valueStream()
					.map(JsonNode::stringValue)
					.toList()
			);

		if (detailsNode.has("price_overview")) {
			JsonNode priceOverviewNode = detailsNode.get("price_overview");
			PriceOverview priceOverview = ctx.readTreeAsValue(priceOverviewNode, PriceOverview.class);
			dto.setPriceOverview(priceOverview);
		}

		if(detailsNode.has("categories"))
			dto.setCategories(
				detailsNode.get("categories")
					.valueStream()
					.map(category -> ctx.readTreeAsValue(category, Category.class))
					.toList()
			);

		if(detailsNode.has("genres"))
			dto.setGenres(
				detailsNode.get("genres")
					.valueStream()
					.map(genre -> ctx.readTreeAsValue(genre, Genre.class))
					.toList()
			);

		// Flatten release_date into coming_soon and releaseDate
		if (detailsNode.has("release_date")) {

			JsonNode releaseDate = detailsNode.get("release_date");

			if (releaseDate.has("coming_soon"))
				dto.setComingSoon(releaseDate.get("coming_soon").asBoolean());

			if (releaseDate.has("date")) {
				String dateStr = releaseDate.get("date").asString();
				if (dateStr != null && !dateStr.isEmpty()) {
					// Steam may return a human-readable date like "14 Jan, 2009".
					// Try to parse as ISO instant first, then fall back to the pattern "d MMM, yyyy".
					try {
						Instant parsed = Instant.parse(dateStr);
						dto.setReleaseDate(parsed);
					} catch (DateTimeParseException ex) {
						try {
							DateTimeFormatter f = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH);
							LocalDate ld = LocalDate.parse(dateStr, f);
							Instant inst = ld.atStartOfDay(ZoneOffset.UTC).toInstant();
							dto.setReleaseDate(inst);
						} catch (DateTimeParseException ex2) {
							// If parsing fails, leave releaseDate null
							dto.setReleaseDate(null);
						}
					}
				}
			}

		}

		if (detailsNode.has("background"))
			dto.setBackground(detailsNode.get("background").asString());

		if (detailsNode.has("background_raw"))
			dto.setBackgroundRaw(detailsNode.get("background_raw").asString());

		return dto;
	}
}