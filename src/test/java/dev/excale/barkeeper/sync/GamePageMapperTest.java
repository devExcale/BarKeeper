package dev.excale.barkeeper.sync;

import dev.excale.barkeeper.notion.model.GamePage;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.steam.model.Category;
import dev.excale.barkeeper.steam.model.Genre;
import dev.excale.barkeeper.steam.model.PriceOverview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GamePageMapperTest.GamePageMapperTestConfig.class)
class GamePageMapperTest {

	@Configuration
	@ComponentScan(
		basePackageClasses = GamePageMapper.class,
		useDefaultFilters = false,
		includeFilters = @ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = GamePageMapper.class
		)
	)
	static class GamePageMapperTestConfig {

	}

	@SuppressWarnings({ "SpringJavaInjectionPointsAutowiringInspection", "RedundantSuppression" })
	@Autowired
	private GamePageMapper mapper;

	@Test
	void givenAppDetails_whenUpdated_thenMapsFields() {

		// Given
		AppDetails appDetails = new AppDetails();
		appDetails.setName("Hades");
		appDetails.setHeaderImage("https://cdn.example/hades.jpg");
		appDetails.setReleaseDate(Instant.parse("2020-09-17T00:00:00Z"));

		PriceOverview price = new PriceOverview();
		price.setInitialPrice(2999);
		price.setFinalPrice(1499);
		appDetails.setPriceOverview(price);

		appDetails.setGenres(List.of(genre("Action"), genre("RPG"), genre("Action")));
		appDetails.setCategories(List.of(category("Single-player"), category("Single-player")));
		appDetails.setDevelopers(List.of("Supergiant", "Supergiant"));
		appDetails.setPublishers(List.of("Supergiant", "Private Division"));

		GamePage target = new GamePage();

		// When
		GamePage result = mapper.update(target, appDetails);

		// Then
		assertEquals("Steam", result.getStore());
		assertEquals("https://cdn.example/hades.jpg", result.getCover());
		assertEquals(List.of("Hades"), result.getTitle());
		assertEquals(29.99d, result.getFullPrice(), 0.0001d);
		assertEquals(14.99d, result.getDiscountPrice(), 0.0001d);
		assertEquals(Instant.parse("2020-09-17T00:00:00Z"), result.getReleaseDate());
		assertEquals(Set.of("Action", "RPG"), result.getGenres());
		assertEquals(Set.of("Single-player"), result.getCategories());
		assertEquals(Set.of("Supergiant"), result.getDevelopers());
		assertEquals(Set.of("Supergiant", "Private Division"), result.getPublishers());

	}

	@Test
	void givenNullAppDetails_whenUpdated_thenReturnsUnchangedTarget() {

		// Given
		GamePage target = new GamePage();
		target.setStore("Existing");
		target.setCover("https://cdn.example/old.jpg");
		target.setTitle(List.of("Old Title"));
		target.setFullPrice(19.99d);
		target.setDiscountPrice(9.99d);
		target.setReleaseDate(Instant.parse("2019-01-01T00:00:00Z"));
		target.setGenres(Set.of("Old Genre"));
		target.setCategories(Set.of("Old Category"));
		target.setDevelopers(Set.of("Old Dev"));
		target.setPublishers(Set.of("Old Pub"));

		// When
		GamePage result = mapper.update(target, null);

		// Then
		assertSame(target, result);
		assertEquals("Existing", result.getStore());
		assertEquals("https://cdn.example/old.jpg", result.getCover());
		assertEquals(List.of("Old Title"), result.getTitle());
		assertEquals(19.99d, result.getFullPrice());
		assertEquals(9.99d, result.getDiscountPrice());
		assertEquals(Instant.parse("2019-01-01T00:00:00Z"), result.getReleaseDate());
		assertEquals(Set.of("Old Genre"), result.getGenres());
		assertEquals(Set.of("Old Category"), result.getCategories());
		assertEquals(Set.of("Old Dev"), result.getDevelopers());
		assertEquals(Set.of("Old Pub"), result.getPublishers());

	}

	@Test
	void givenNullFieldsInAppDetails_whenUpdated_thenClearsMappedFields() {

		// Given
		AppDetails appDetails = new AppDetails();
		GamePage target = new GamePage();
		target.setStore("Existing");
		target.setCover("https://cdn.example/old.jpg");
		target.setTitle(List.of("Old Title"));
		target.setFullPrice(19.99);
		target.setDiscountPrice(9.99);
		target.setReleaseDate(Instant.parse("2019-01-01T00:00:00Z"));
		target.setGenres(Set.of("Old Genre"));
		target.setCategories(Set.of("Old Category"));
		target.setDevelopers(Set.of("Old Dev"));
		target.setPublishers(Set.of("Old Pub"));

		// When
		GamePage result = mapper.update(target, appDetails);

		// Then
		assertEquals("Steam", result.getStore());
		assertNull(result.getCover());
		assertNull(result.getTitle());
		assertNull(result.getFullPrice());
		assertNull(result.getDiscountPrice());
		assertNull(result.getReleaseDate());
		assertEquals(Set.of(), result.getGenres());
		assertEquals(Set.of(), result.getCategories());
		assertEquals(Set.of(), result.getDevelopers());
		assertEquals(Set.of(), result.getPublishers());

	}

	private static Genre genre(String description) {
		Genre genre = new Genre();
		genre.setDescription(description);
		return genre;
	}

	private static Category category(String description) {
		Category category = new Category();
		category.setDescription(description);
		return category;
	}

}
