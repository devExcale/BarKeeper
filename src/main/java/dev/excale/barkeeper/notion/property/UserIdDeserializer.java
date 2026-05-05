package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.UUID;

public class UserIdDeserializer extends StdDeserializer<Object> {

	public UserIdDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);
		if(root == null || root.isNull())
			return null;

		JsonNode idNode = root.get("id");
		if(idNode == null || idNode.isNull())
			return null;

		return UUID.fromString(idNode.asString());
	}

}