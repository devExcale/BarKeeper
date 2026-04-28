package dev.excale.barkeeper.steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@Data
@JsonDeserialize(using = AppDetailsDeserializer.class)
public class AppDetails {

	private String type;

	private String name;

	@JsonProperty("steam_appid")
	private Integer steamAppid;

	@JsonProperty("required_age")
	private Integer requiredAge;

	@JsonProperty("is_free")
	private Boolean isFree;

	@JsonProperty("detailed_description")
	private String detailedDescription;

	@JsonProperty("about_the_game")
	private String aboutTheGame;

	@JsonProperty("short_description")
	private String shortDescription;

	@JsonProperty("supported_languages")
	private String supportedLanguages;

	@JsonProperty("header_image")
	private String headerImage;

	@JsonProperty("capsule_image")
	private String capsuleImage;

	@JsonProperty("capsule_imagev5")
	private String capsuleImageV5;

	private String website;

	private List<String> developers;

	private List<String> publishers;

	@JsonProperty("price_overview")
	private PriceOverview priceOverview;

	private List<Category> categories;

	private List<Genre> genres;

	private Boolean comingSoon;

	private Instant releaseDate;

	private String background;

	@JsonProperty("background_raw")
	private String backgroundRaw;

}

