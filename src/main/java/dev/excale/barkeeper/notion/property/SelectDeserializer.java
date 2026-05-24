package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class SelectDeserializer extends StdDeserializer<Object> {

	/**
	 * Default constructor needed by Jackson
	 */
	@SuppressWarnings("unused")
	public SelectDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode propertyNode = ctx.readTree(p);

		if (propertyNode == null || propertyNode.isNull())
			return null;

		JsonNode selectNode = propertyNode.get("select");
		if (selectNode == null || selectNode.isNull())
			return null;

		JsonNode nameNode = selectNode.get("name");
		if (nameNode == null || nameNode.isNull())
			return null;

		return nameNode.asString();
	}

}
