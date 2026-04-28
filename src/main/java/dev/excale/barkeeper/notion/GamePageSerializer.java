package dev.excale.barkeeper.notion;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collection;

public class GamePageSerializer extends StdSerializer<GamePage> {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String OPTIONS = "options";
	private static final String NUMBER = "number";
	private static final String CREATED_TIME = "created_time";

	public GamePageSerializer() {
		super(GamePage.class);
	}

	@Override
	public void serialize(GamePage value, JsonGenerator gen, SerializationContext provider) {
		try {
			serializeGamePage(gen, value);
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize GamePage", e);
		}
	}

	private void serializeGamePage(JsonGenerator gen, GamePage value) throws IOException {
		ObjectNode root = objectMapper.createObjectNode();

		// Write page id
		if (value.getId() != null) {
			root.put("id", value.getId().toString());
		}

		// Write cover
		if(value.getCover() != null) {
			ObjectNode coverNode = objectMapper.createObjectNode();
			coverNode.put("type", "external");
			coverNode.set("external", objectMapper.createObjectNode().put("url", value.getCover()));
			root.set("cover", coverNode);
		} else {
			root.putNull("cover");
		}

		// Build properties object
		ObjectNode propertiesNode = objectMapper.createObjectNode();

		// Loop through all fields with @Property annotation
		for (Field field : GamePage.class.getDeclaredFields()) {
			Property annotation = field.getAnnotation(Property.class);
			if (annotation == null)
				continue;

			String propertyId = annotation.id();
			String propertyType = annotation.type();

			if(propertyType.equals(CREATED_TIME))
				continue;

			field.setAccessible(true);
			Object fieldValue;
			try {
				fieldValue = field.get(value);
			} catch (IllegalAccessException e) {
				throw new IOException("Failed to access field " + field.getName(), e);
			}

			// Create property object
			ObjectNode propertyNode = objectMapper.createObjectNode();

			// Add the value based on type
			addPropertyValue(propertyNode, propertyType, fieldValue);

			propertiesNode.set(propertyId, propertyNode);
		}

		root.set("properties", propertiesNode);

		// Write the final JSON
		objectMapper.writeValue(gen, root);
	}

	private void addPropertyValue(ObjectNode propertyNode, String propertyType, Object value) {
		switch (propertyType) {
			case "select" -> addSelectValue(propertyNode, value);
			case "multi_select" -> addMultiSelectValue(propertyNode, value);
			case NUMBER -> addNumberValue(propertyNode, value);
			case "date" -> addDateValue(propertyNode, value);
			case CREATED_TIME -> addCreatedTimeValue(propertyNode, value);
			case "url" -> addUrlValue(propertyNode, value);
			default -> {
				// Unsupported type - skip
			}
		}
	}

	private void addSelectValue(ObjectNode propertyNode, Object value) {

		ObjectNode selectNode = objectMapper.createObjectNode();
		if (value != null)
			selectNode.put("name", value.toString());
		else
			selectNode.putNull(OPTIONS);

		propertyNode.set("select", selectNode);
	}

	private void addMultiSelectValue(ObjectNode propertyNode, Object value) {
		ArrayNode multiSelectNode = objectMapper.createArrayNode();
		if (value instanceof Collection<?> collection)
			multiSelectNode.addAll(
				collection.stream()
					.map(Object::toString)
					.map(name -> objectMapper.createObjectNode().put("name", name))
					.toList()
			);

		propertyNode.set("multi_select", multiSelectNode);
	}

	private void addNumberValue(ObjectNode propertyNode, Object value) {
		if (value != null) {
			propertyNode.put(NUMBER, ((Number) value).doubleValue());
		} else {
			propertyNode.putNull(NUMBER);
		}
	}

	private void addDateValue(ObjectNode propertyNode, Object value) {
		ObjectNode dateNode = objectMapper.createObjectNode();
		if (value instanceof Instant instant) {
			dateNode.put("start", instant.toString());
		} else {
			dateNode = null;
		}
		propertyNode.set("date", dateNode);
	}

	private void addCreatedTimeValue(ObjectNode propertyNode, Object value) {
		if (value instanceof Instant instant) {
			propertyNode.put(CREATED_TIME, instant.toString());
		} else {
			propertyNode.putNull(CREATED_TIME);
		}
	}

	private void addUrlValue(ObjectNode propertyNode, Object value) {
		if (value != null) {
			propertyNode.put("url", value.toString());
		} else {
			propertyNode.putNull("url");
		}
	}

}









