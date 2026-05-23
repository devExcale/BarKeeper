package dev.excale.barkeeper.notion.property;

import dev.excale.barkeeper.notion.NotionFeignConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JsonTest
class ParentSerializationTest {

	private final ObjectMapper mapper = new NotionFeignConfig(null).objectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@Parent
		private String parent;

	}

	@Test
	void givenParentDataSourceId_whenSerialized_thenOutputsCorrectNotionFormat() {

		// Given
		String randomId = UUID.randomUUID().toString();
		DummyTarget target = new DummyTarget(randomId);

		// When
		String json = mapper.writeValueAsString(target);

		// Then
		String expectedJson = """
			{
				"parent": {
			        "type": "data_source_id",
			        "data_source_id": "randomId"
			    }
			}
			"""
			.replace("randomId", randomId)
			.replaceAll("\\s+", "");
		assertEquals(expectedJson, json);

	}

	@Test
	void givenParentDataSourceJson_whenDeserialized_thenExtractsDataSourceId() throws Exception {

		// Given
		String randomId = UUID.randomUUID().toString();
		String jsonIn = """
			{
			    "parent": {
			        "type": "data_source_id",
			        "data_source_id": "randomId"
			    }
			}
			"""
			.replace("randomId", randomId);

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(randomId, result.getParent());

	}

	@Test
	void givenNullParentJson_whenDeserialized_thenReturnsNull() throws Exception {

		// Given
		String jsonIn = "{\"parent\":null}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertNull(result.getParent());

	}

}
