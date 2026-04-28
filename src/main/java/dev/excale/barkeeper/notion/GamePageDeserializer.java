package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.steam.AppDetails;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GamePageDeserializer extends StdDeserializer<GamePage> {

	public GamePageDeserializer() {
		super(AppDetails.class);
	}

	@Override
	public GamePage deserialize(JsonParser jp, DeserializationContext ctx) {

		GamePage dto = new GamePage();

		// Read the JSON tree
		JsonNode root = ctx.readTree(jp);

		// Extract page id
		String stringUuid = root.get("id").asString();
		dto.setId(UUID.fromString(stringUuid));

		// Extract cover
		String coverUrl = root.optional("cover")
			.map(node -> node.get("external"))
			.map(node -> node.get("url").asString())
			.orElse(null);
		dto.setCover(coverUrl);

		// Extract properties
		JsonNode propertiesNode = root.get("properties");

		// Build id->property mapping
		Map<String, JsonNode> propertiesIdMap = propertiesNode.valueStream()
			.collect(Collectors.toMap(
				node -> node.get("id").asString(),
				Function.identity()
			));

		// Loop all properties with @Property
		for(Field field : GamePage.class.getDeclaredFields())
			try {

				setField(dto, field, propertiesIdMap);

			} catch(IllegalAccessException e) {
				throw new RuntimeException("Failed to set field " + field.getName(), e);
			}



		return dto;
	}

	private void setField(GamePage dto, Field field, Map<String, JsonNode> properties) throws IllegalAccessException {

		// Get the @Property annotation on the field
		Property annotation = field.getAnnotation(Property.class);
		if (annotation == null)
			return;

		String propertyId = annotation.id();
		String propertyType = annotation.type();

		// Find the corresponding property by id
		JsonNode propertyNode = properties.get(propertyId);
		if (propertyNode == null)
			return;

		if(!propertyType.equals(propertyNode.get("type").asString()))
			throw new IllegalArgumentException("Property type mismatch for field " + field.getName() + ": expected " + propertyType + " but got " + propertyNode.get("type").asString());

		Object value = switch(propertyType) {

			case "select" -> {

				JsonNode selectNode = propertyNode.get("select");
				if(selectNode == null || selectNode.isNull())
					yield null;

				JsonNode optionsNode = selectNode.get("options");
				if(optionsNode != null)
					yield optionsNode.valueStream()
						.map(node -> node.get("name").asString())
						.findFirst()
						.orElse(null);

				yield selectNode.get("name").asString();
			}

			case "multi_select" -> propertyNode.get("multi_select")
				.optional("options")
				.map(JsonNode::valueStream)
				.orElseGet(Stream::empty)
				.map(node -> node.get("name").asString())
				.collect(Collectors.toSet());

			case "number" -> propertyNode.optional("number")
				.map(JsonNode::asDouble)
				.orElse(null);

			case "date" -> {

				JsonNode dateNode = propertyNode.get("date");
				if (dateNode == null || dateNode.isNull())
					yield null;

				String start = dateNode.get("start").asString();
				if (start == null || start.isEmpty())
					yield null;

				// Notion may return either a full timestamp (ISO instant) or a date-only string (YYYY-MM-DD).
				try {
					yield Instant.parse(start);
				} catch (DateTimeParseException ex) {
					// Try parse as date-only and convert to start of day UTC
					try {
						LocalDate ld = LocalDate.parse(start);
						yield ld.atStartOfDay(ZoneOffset.UTC).toInstant();
					} catch (DateTimeParseException ex2) {
						throw new IllegalArgumentException("Unable to parse date: " + start, ex2);
					}
				}
			}

			case "created_time" -> Instant.parse(propertyNode.get("created_time").asString());

			case "url" -> {

				JsonNode urlNode = propertyNode.get("url");
				if(urlNode == null || urlNode.isNull())
					yield null;

				yield urlNode.asString();
			}

			default -> throw new IllegalArgumentException("Unsupported property type: " + propertyType);

		};

		field.setAccessible(true);
		field.set(dto, value);

	}

}
