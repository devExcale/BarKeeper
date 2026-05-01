package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.config.Jackson3Config;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
@JsonTest
class GamePageSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@Test
	void givenFlattenedJson_whenDeserialized_thenGamePagePopulated() throws Exception {
		// Given (A trimmed version of notion-properties-out.json representing the flattened state)
		String flattenedJsonIn = """
			{
			    "id": "21f0c276-a8de-8042-b777-d2fe829a054f",
			    "byYs": {
			        "id": "byYs",
			        "type": "select",
			        "select": { "name": "Steam" }
			    },
			    "rkK%3D": {
			        "id": "rkK%3D",
			        "type": "number",
			        "number": 5.89
			    },
			    "vJxu": {
			        "id": "vJxu",
			        "type": "number",
			        "number": 4.12
			    }
			}
			""";

		// When
		GamePage page = mapper.readValue(flattenedJsonIn, GamePage.class);

		// Then
		assertNotNull(page);

		// Assert default Jackson mapped fields
		assertEquals("21f0c276-a8de-8042-b777-d2fe829a054f", page.getId()
			.toString());

		// Assert custom @Select fields
		assertEquals("Steam", page.getStore());

		// Assuming @Number deserializer works similarly, we assert those fields too
		assertEquals(5.89, page.getFullPrice());
		assertEquals(4.12, page.getDiscountPrice());
	}

}