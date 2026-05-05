package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;

public class InstantDeserializer extends StdDeserializer<Object> {

	public InstantDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);
		if(root == null || root.isNull())
			return null;

		return Instant.parse(root.asString());
	}

}