package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.notion.property.*;
import dev.excale.barkeeper.notion.property.Number;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class GamePage {

	@PageId
	private UUID id;

	@Cover
	private String cover;

	@Select("byYs")
	private String store;

	@Number("rkK%3D")
	private Double fullPrice;

	@Number("vJxu")
	private Double discountPrice;

	@Date("h%3AsK")
	private Instant releaseDate;

	@Url("YELs")
	private String storePage;

	@MultiSelect("LyGM")
	private Set<String> genres;

	@MultiSelect("o%60%7Dc")
	private Set<String> categories;

	@MultiSelect("w%60xx")
	private Set<String> developers;

	@MultiSelect("DytZ")
	private Set<String> publishers;

	@CreatedTime("BPfw")
	private Instant createdAt;

//	@Property(id = "hUwy", type = "rich_text")
	private String notes;

}

