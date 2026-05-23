package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class UrlDeserializer extends StdDeserializer<Object> {

	@SuppressWarnings("unused")
	public UrlDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode propertyNode = ctx.readTree(p);

		if (propertyNode == null || propertyNode.isNull())
			return null;

		JsonNode urlNode = propertyNode.get("url");
		if (urlNode == null || urlNode.isNull())
			return null;

		return urlNode.asString();
	}

}
