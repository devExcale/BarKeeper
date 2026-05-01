package dev.excale.barkeeper.notion.property;

import dev.excale.barkeeper.config.Jackson3Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JsonTest
class UrlSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@Url("YELs")
		private String storePage;

	}

	@Test
	void givenUrlString_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		DummyTarget target = new DummyTarget("https://store.steampowered.com/app/1234");

		// When
		String json = mapper.writeValueAsString(target);

		// Then
		String expectedJson = "{\"YELs\":{\"url\":\"https://store.steampowered.com/app/1234\"}}";
		assertEquals(expectedJson, json);
	}

	@Test
	void givenNotionUrlJson_whenDeserialized_thenExtractsString() throws Exception {
		// Given
		String jsonIn = """
            {
                "YELs": {
                    "id": "YELs",
                    "type": "url",
                    "url": "https://store.steampowered.com/app/1234"
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals("https://store.steampowered.com/app/1234", result.getStorePage());
	}

	@Test
	void givenEmptyUrlJson_whenDeserialized_thenReturnsNull() throws Exception {
		// Given
		String jsonIn = "{\"YELs\":{\"id\":\"YELs\",\"type\":\"url\",\"url\":null}}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertNull(result.getStorePage());
	}
}
