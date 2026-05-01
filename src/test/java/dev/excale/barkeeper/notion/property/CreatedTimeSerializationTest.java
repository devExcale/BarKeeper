package dev.excale.barkeeper.notion.property;

import dev.excale.barkeeper.config.Jackson3Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JsonTest
class CreatedTimeSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@CreatedTime("BPfw")
		private Instant createdAt;

	}

	@Test
	void givenInstant_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		Instant instant = Instant.parse("2023-10-12T00:00:00Z");
		DummyTarget target = new DummyTarget(instant);

		// When
		String json = mapper.writeValueAsString(target);

		// Then
		String expectedJson = "{\"BPfw\":{\"created_time\":\"2023-10-12T00:00:00Z\"}}";
		assertEquals(expectedJson, json);
	}

	@Test
	void givenNotionCreatedTimeJson_whenDeserialized_thenExtractsInstant() throws Exception {
		// Given
		String jsonIn = """
            {
                "BPfw": {
                    "id": "BPfw",
                    "type": "created_time",
                    "created_time": "2023-10-12T00:00:00Z"
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(Instant.parse("2023-10-12T00:00:00Z"), result.getCreatedAt());
	}

	@Test
	void givenEmptyCreatedTimeJson_whenDeserialized_thenReturnsNull() throws Exception {
		// Given
		String jsonIn = "{\"BPfw\":{\"id\":\"BPfw\",\"type\":\"created_time\",\"created_time\":null}}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertNull(result.getCreatedAt());
	}
}
