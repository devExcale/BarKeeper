package dev.excale.barkeeper.notion;

import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonDeserialize(using = GamePageDeserializer.class)
@JsonSerialize(using = GamePageSerializer.class)
public class GamePage {

	private UUID id;

	private String cover;

	@Property(id = "byYs", type = "select")
	private String store;

	@Property(id = "rkK%3D", type = "number")
	private Double fullPrice;

	@Property(id = "vJxu", type = "number")
	private Double discountPrice;

	@Property(id = "h%3AsK", type = "date")
	private Instant releaseDate;

	@Property(id = "YELs", type = "url")
	private String storePage;

	@Property(id = "LyGM", type = "multi_select")
	private Set<String> genres;

	@Property(id = "o%60%7Dc", type = "multi_select")
	private Set<String> categories;

	@Property(id = "w%60xx", type = "multi_select")
	private Set<String> developers;

	@Property(id = "DytZ", type = "multi_select")
	private Set<String> publishers;

	@Property(id = "BPfw", type = "created_time")
	private Instant createdAt;

//	@Property(id = "hUwy", type = "rich_text")
	private String notes;

}

