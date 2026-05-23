package dev.excale.barkeeper.sync;

import dev.excale.barkeeper.notion.model.GamePage;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.steam.model.Category;
import dev.excale.barkeeper.steam.model.Genre;
import dev.excale.barkeeper.steam.model.PriceOverview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
	componentModel = "spring",
	unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE,
	unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
	nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_NULL,
	imports = {
		GamePageMapper.class
	}
)
public interface GamePageMapper {

	@SuppressWarnings("UnmappedTargetProperties")
	@Mapping(target = "store",         constant   = "Steam"                                            )
	@Mapping(target = "cover",         source     = "headerImage"                                      )
	@Mapping(target = "title",         expression = "java(GamePageMapper.toTitle(appDetails.getName()))")
	@Mapping(target = "fullPrice",     expression = "java(GamePageMapper.getFullPrice(appDetails))"    )
	@Mapping(target = "discountPrice", expression = "java(GamePageMapper.getDiscountPrice(appDetails))")
	@Mapping(target = "releaseDate",   source     = "releaseDate"                                      )
	@Mapping(target = "genres",        expression = "java(GamePageMapper.getGenres(appDetails))"       )
	@Mapping(target = "categories",    expression = "java(GamePageMapper.getCategories(appDetails))"   )
	@Mapping(target = "developers",    expression = "java(GamePageMapper.getDevelopers(appDetails))"   )
	@Mapping(target = "publishers",    expression = "java(GamePageMapper.getPublishers(appDetails))"   )
	GamePage update(@MappingTarget GamePage gamePage, AppDetails appDetails);

	static List<String> toTitle(String name) {
		return name == null ? null : List.of(name);
	}

	static Double getFullPrice(AppDetails appDetails) {

		PriceOverview price = appDetails.getPriceOverview();
		if(price == null)
			return null;

		Integer initialPrice = price.getInitialPrice();
		if(initialPrice == null)
			return null;

		return initialPrice / 100d;
	}

	static Double getDiscountPrice(AppDetails appDetails) {

		PriceOverview price = appDetails.getPriceOverview();
		if(price == null)
			return null;

		Integer finalPrice = price.getFinalPrice();
		if(finalPrice == null)
			return null;

		return finalPrice / 100d;
	}

	static Set<String> getGenres(AppDetails appDetails) {

		List<Genre> genres = appDetails.getGenres();
		if(genres == null)
			return new HashSet<>();

		return genres.stream()
			.map(Genre::getDescription)
			.collect(Collectors.toSet());
	}

	static Set<String> getCategories(AppDetails appDetails) {

		List<Category> categories = appDetails.getCategories();
		if(categories == null)
			return new HashSet<>();

		return categories.stream()
			.map(Category::getDescription)
			.collect(Collectors.toSet());
	}

	static Set<String> getDevelopers(AppDetails appDetails) {

		List<String> developers = appDetails.getDevelopers();
		if(developers == null)
			return new HashSet<>();

		return new HashSet<>(developers);
	}

	static Set<String> getPublishers(AppDetails appDetails) {

		List<String> publishers = appDetails.getPublishers();
		if(publishers == null)
			return new HashSet<>();

		return new HashSet<>(publishers);
	}

}
