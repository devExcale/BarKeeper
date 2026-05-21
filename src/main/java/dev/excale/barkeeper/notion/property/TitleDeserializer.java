package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.ArrayNode;

import java.util.Collection;
import java.util.List;

public class TitleDeserializer extends StdDeserializer<Object> {

	public TitleDeserializer() {
		super(Object.class);
	}

	@Override
	public List<String> deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);

		if (root == null || root.isNull())
			return List.of();

		return root.optional("title")
			.map(JsonNode::asArray)
			.map(ArrayNode::elements)
			.stream()
			.flatMap(Collection::stream)
			.map(n -> n.get("plain_text").asString())
			.toList();
	}

}
