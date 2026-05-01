package dev.excale.barkeeper.notion.property;

import dev.excale.barkeeper.config.Jackson3Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
class MultiSelectSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@MultiSelect("genres")
		private Set<String> genres;

	}

	@Test
	void givenMultiSelectSet_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		DummyTarget target = new DummyTarget(Set.of("Action", "RPG"));

		// When
		String json = mapper.writeValueAsString(target);

		// Then (Verify it builds the nested Notion object structure)
		// Set order is not guaranteed in Java 8+, but Set.of has stable or random order? We can use List in java.util.List if we want stable order for assertion, or just use regex / contains.
		// For simplicity, let's just use string contains.
		assertTrue(json.contains("\"genres\":{\"multi_select\":["));
		assertTrue(json.contains("{\"name\":\"Action\"}"));
		assertTrue(json.contains("{\"name\":\"RPG\"}"));
	}

	@Test
	void givenNotionMultiSelectJson_whenDeserialized_thenExtractsSet_fromArray() throws Exception {
		// Given (from GamePageDeserializer array style mapping if applicable)
		String jsonIn = """
            {
                "genres": {
                    "id": "genres",
                    "type": "multi_select",
                    "multi_select": [
                        {
                            "id": "1",
                            "name": "Action",
                            "color": "red"
                        },
                        {
                            "id": "2",
                            "name": "RPG",
                            "color": "blue"
                        }
                    ]
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(2, result.getGenres().size());
		assertTrue(result.getGenres().contains("Action"));
		assertTrue(result.getGenres().contains("RPG"));
	}

	@Test
	void givenNotionMultiSelectJson_whenDeserialized_thenExtractsSet_fromOptionsObject() throws Exception {
		// Given (from GamePageDeserializer mapping with options object)
		String jsonIn = """
            {
                "genres": {
                    "id": "genres",
                    "type": "multi_select",
                    "multi_select": {
                        "options": [
                            {
                                "id": "1",
                                "name": "Action",
                                "color": "red"
                            },
                            {
                                "id": "2",
                                "name": "RPG",
                                "color": "blue"
                            }
                        ]
                    }
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(2, result.getGenres().size());
		assertTrue(result.getGenres().contains("Action"));
		assertTrue(result.getGenres().contains("RPG"));
	}

	@Test
	void givenEmptyMultiSelectJson_whenDeserialized_thenReturnsEmptySet() throws Exception {
		// Given
		String jsonIn = "{\"genres\":{\"id\":\"genres\",\"type\":\"multi_select\",\"multi_select\":[]}}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertTrue(result.getGenres().isEmpty());
	}
}


