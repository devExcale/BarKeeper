package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MultiSelectDeserializer extends StdDeserializer<Object> {

	private final Class<?> rawClass;

	@SuppressWarnings("unused")
	public MultiSelectDeserializer() {
		super(Object.class);
		this.rawClass = null;
	}

	public MultiSelectDeserializer(Class<?> rawClass) {
		super(Object.class);
		this.rawClass = rawClass;
	}

	@Override
	public ValueDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws DatabindException {
		Class<?> propertyClass = property == null ? null : property.getType().getRawClass();
		return new MultiSelectDeserializer(propertyClass);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode propertyNode = ctx.readTree(p);

		if (propertyNode == null || propertyNode.isNull()) return returnEmpty();

		JsonNode multiSelectNode = propertyNode.get("multi_select");
		if (multiSelectNode == null || multiSelectNode.isNull()) return returnEmpty();

		Stream<JsonNode> stream;
		if (multiSelectNode.isArray()) stream = multiSelectNode.valueStream();
		else stream = multiSelectNode.optional("options").map(JsonNode::valueStream).orElseGet(Stream::empty);

		List<String> results = stream.map(node -> node.get("name").asString()).toList();

		if (rawClass != null && Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>(results);
		return results;
	}

	private Object returnEmpty() {
		if (rawClass != null && Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>();
		return new ArrayList<>();
	}
}
