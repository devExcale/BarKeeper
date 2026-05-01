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
class SelectSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	// Dummy class to test the @Select contextualization and Introspector routing
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@Select("byYs")
		private String store;

	}

	@Test
	void givenSelectString_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		DummyTarget target = new DummyTarget("Steam");

		// When
		String json = mapper.writeValueAsString(target);

		// Then (Verify it builds the nested Notion object structure)
		String expectedJson = "{\"byYs\":{\"select\":{\"name\":\"Steam\"}}}";
		assertEquals(expectedJson, json);
	}

	@Test
	void givenNotionSelectJson_whenDeserialized_thenExtractsString() throws Exception {
		// Given (Extracted exactly from the JSON in notion-properties-out.json)
		String jsonIn = """
            {
                "byYs": {
                    "id": "byYs",
                    "type": "select",
                    "select": {
                        "id": "3d1a937d-7bb9-466c-8719-788eab57b40d",
                        "name": "Steam",
                        "color": "blue"
                    }
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals("Steam", result.getStore());
	}

	@Test
	void givenEmptySelectJson_whenDeserialized_thenReturnsNull() throws Exception {
		// Given
		String jsonIn = "{\"byYs\":{\"id\":\"byYs\",\"type\":\"select\",\"select\":null}}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertNull(result.getStore());
	}
}
