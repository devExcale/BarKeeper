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
class DateSerializationTest {
private final ObjectMapper mapper = new Jackson3Config().jacksonObjectMapper();
@AllArgsConstructor
@NoArgsConstructor
@Getter
static class DummyTarget {
@Date("h%3AsK")
private Instant releaseDate;
}
@Test
void givenInstant_whenSerialized_thenOutputsCorrectNotionFormat() throws Exception {
// Given
Instant instant = Instant.parse("2023-10-12T00:00:00Z");
DummyTarget target = new DummyTarget(instant);
// When
String json = mapper.writeValueAsString(target);
// Then
String expectedJson = "{\"h%3AsK\":{\"date\":{\"start\":\"2023-10-12T00:00:00Z\"}}}";
assertEquals(expectedJson, json);
}
@Test
void givenNotionDateInstantJson_whenDeserialized_thenExtractsInstant() throws Exception {
// Given
String jsonIn = """
            {
                "h%3AsK": {
                    "id": "h%3AsK",
                    "type": "date",
                    "date": {
                        "start": "2023-10-12T00:00:00Z",
                        "end": null,
                        "time_zone": null
                    }
                }
            }
            """;
// When
DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);
// Then
assertEquals(Instant.parse("2023-10-12T00:00:00Z"), result.getReleaseDate());
}
@Test
void givenNotionDateOnlyJson_whenDeserialized_thenExtractsInstantAtStartOfDayUtc() throws Exception {
// Given
String jsonIn = """
            {
                "h%3AsK": {
                    "id": "h%3AsK",
                    "type": "date",
                    "date": {
                        "start": "2023-10-12",
                        "end": null,
                        "time_zone": null
                    }
                }
            }
            """;
// When
DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);
// Then
assertEquals(Instant.parse("2023-10-12T00:00:00Z"), result.getReleaseDate());
}
@Test
void givenEmptyDateJson_whenDeserialized_thenReturnsNull() throws Exception {
// Given
String jsonIn = "{\"h%3AsK\":{\"id\":\"h%3AsK\",\"type\":\"date\",\"date\":null}}";
// When
DummyTarget result = mapper.readValue(jsonIn, DummyTarget.class);
// Then
assertNull(result.getReleaseDate());
}
}
