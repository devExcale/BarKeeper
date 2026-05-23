package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.UUID;

public class ParentDeserializer extends StdDeserializer<Object> {

	public ParentDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);
		if (root == null || root.isNull())
			return null;

		JsonNode dataSourceNode = root.get("data_source_id");
		if (dataSourceNode == null || dataSourceNode.isNull())
			return null;

		return UUID.fromString(dataSourceNode.asString());
	}

}
