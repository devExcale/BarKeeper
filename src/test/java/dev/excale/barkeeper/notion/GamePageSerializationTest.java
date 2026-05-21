package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.notion.model.GamePage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class GamePageSerializationTest {

	private final ObjectMapper mapper = new NotionFeignConfig().objectMapper();

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

	@Test
	void givenFlattenedJsonArray_whenDeserialized_thenGamePageListPopulated() throws Exception {
		// Given a JSON array of flattened page objects
		String flattenedJsonListIn = """
			[
			    {
			        "id": "21f0c276-a8de-8042-b777-d2fe829a054f",
			        "byYs": { "id": "byYs", "type": "select", "select": { "name": "Steam" } },
			        "rkK%3D": { "id": "rkK%3D", "type": "number", "number": 5.89 },
			        "vJxu": { "id": "vJxu", "type": "number", "number": 4.12 }
			    },
			    {
			        "id": "32a1b489-c9ef-9153-c888-e3ab930b165c",
			        "byYs": { "id": "byYs", "type": "select", "select": { "name": "Epic Games" } },
			        "rkK%3D": { "id": "rkK%3D", "type": "number", "number": 19.99 },
			        "vJxu": { "id": "vJxu", "type": "number", "number": 14.99 }
			    }
			]
			""";

		// When
		// We use Jackson's TypeReference to safely deserialize generic collections
		List<GamePage> pages = mapper.readValue(flattenedJsonListIn, new TypeReference<List<GamePage>>() {});

		// Then
		assertNotNull(pages);
		assertEquals(2, pages.size());

		// Assert first item matches the first JSON object
		GamePage page1 = pages.get(0);
		assertEquals("21f0c276-a8de-8042-b777-d2fe829a054f", page1.getId().toString());
		assertEquals("Steam", page1.getStore());
		assertEquals(5.89, page1.getFullPrice());
		assertEquals(4.12, page1.getDiscountPrice());

		// Assert second item matches the second JSON object
		GamePage page2 = pages.get(1);
		assertEquals("32a1b489-c9ef-9153-c888-e3ab930b165c", page2.getId().toString());
		assertEquals("Epic Games", page2.getStore());
		assertEquals(19.99, page2.getFullPrice());
		assertEquals(14.99, page2.getDiscountPrice());
	}
}
