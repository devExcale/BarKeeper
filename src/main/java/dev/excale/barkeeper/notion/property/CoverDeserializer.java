package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CoverDeserializer extends StdDeserializer<Object> {

	@SuppressWarnings("unused")
	public CoverDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);

		if (root == null || root.isNull())
			return null;

		return root.optional("external")
			.map(node -> node.get("url"))
			.map(JsonNode::asString)
			.orElse(null);
	}

}
