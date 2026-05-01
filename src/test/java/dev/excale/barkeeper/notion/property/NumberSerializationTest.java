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
class NumberSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@Number("rkK%3D")
		private Double fullPrice;

	}

	@Test
	void givenNumberDouble_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		DummyTarget target = new DummyTarget(5.89);

		// When
		String json = mapper.writeValueAsString(target);

		// Then
		String expectedJson = "{\"rkK%3D\":{\"number\":5.89}}";
		assertEquals(expectedJson, json);
	}

	@Test
	void givenNotionNumberJson_whenDeserialized_thenExtractsDouble() throws Exception {
		// Given
		String jsonIn = """
            {
                "rkK%3D": {
                    "id": "rkK%3D",
                    "type": "number",
                    "number": 5.89
                }
            }
            """;

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(5.89, result.getFullPrice());
	}

	@Test
	void givenEmptyNumberJson_whenDeserialized_thenReturnsNull() throws Exception {
		// Given
		String jsonIn = "{\"rkK%3D\":{\"id\":\"rkK%3D\",\"type\":\"number\",\"number\":null}}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertNull(result.getFullPrice());
	}
}
