package dev.excale.barkeeper.notion.property;

import dev.excale.barkeeper.config.Jackson3Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class PageIdSerializationTest {

	private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	static class DummyTarget {

		@PageId
		private UUID id;

	}

	@Test
	void givenPageId_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
		// Given
		UUID uuid = UUID.fromString("21f0c276-a8de-8042-b777-d2fe829a054f");
		DummyTarget target = new DummyTarget(uuid);

		// When
		String json = mapper.writeValueAsString(target);

		// Then
		String expectedJson = "{\"id\":\"21f0c276-a8de-8042-b777-d2fe829a054f\"}";
		assertEquals(expectedJson, json);
	}

	@Test
	void givenNotionPageIdJson_whenDeserialized_thenExtractsUuid() throws Exception {
		// Given
		String jsonIn = "{\"id\":\"21f0c276-a8de-8042-b777-d2fe829a054f\"}";

		// When
		DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);

		// Then
		assertEquals(UUID.fromString("21f0c276-a8de-8042-b777-d2fe829a054f"), result.getId());
	}
}

