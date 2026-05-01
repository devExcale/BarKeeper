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
class CoverSerializationTest {
private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();
@AllArgsConstructor
@NoArgsConstructor
@Getter
static class DummyTarget {
@Cover
private String cover;
}
@Test
void givenCoverString_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
// Given
DummyTarget target = new DummyTarget("https://example.com/cover.jpg");
// When
String json = mapper.writeValueAsString(target);
// Then
String expectedJson = "{\"cover\":{\"type\":\"external\",\"external\":{\"url\":\"https://example.com/cover.jpg\"}}}";
assertEquals(expectedJson, json);
}
@Test
void givenNotionCoverExternalJson_whenDeserialized_thenExtractsString() throws Exception {
// Given
String jsonIn = """
            {
                "cover": {
                    "type": "external",
                    "external": {
                        "url": "https://example.com/cover.jpg"
                    }
                }
            }
            """;
// When
DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);
// Then
assertEquals("https://example.com/cover.jpg", result.getCover());
}
@Test
void givenEmptyCoverJson_whenDeserialized_thenReturnsNull() throws Exception {
// Given
String jsonIn = "{\"cover\":null}";
// When
DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);
// Then
assertNull(result.getCover());
}
}
