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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiSelectDeserializer extends StdDeserializer<Object> {

	private final MultiSelect annotation;
	private final Class<?> rawClass;

	public MultiSelectDeserializer() {
		super(Object.class);
		this.annotation = null;
		this.rawClass = null;
	}

	public MultiSelectDeserializer(MultiSelect annotation, Class<?> rawClass) {
		super(Object.class);
		this.annotation = annotation;
		this.rawClass = rawClass;
	}

	@Override
	public ValueDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws DatabindException {
		if(property == null) {
			ctx.reportBadDefinition(handledType(), "Cannot use MultiSelectDeserializer outside of a field context. It must be attached to a specific field via @MultiSelect.");
			return null;
		}

		MultiSelect meta = property.getAnnotation(MultiSelect.class);

		if(meta == null) {
			ctx.reportBadDefinition(handledType(), "Cannot use MultiSelectDeserializer without @MultiSelect annotation on field " + property.getName() + ".");
			return null;
		}

		return new MultiSelectDeserializer(meta, property.getType().getRawClass());
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {
		if (annotation == null) {
			ctx.reportBadDefinition(handledType(), "MultiSelectDeserializer was not contextualized.");
			return null;
		}

		JsonNode propertyNode = ctx.readTree(p);

		if (propertyNode == null || propertyNode.isNull()) return returnEmpty();

		JsonNode multiSelectNode = propertyNode.get("multi_select");
		if (multiSelectNode == null || multiSelectNode.isNull()) return returnEmpty();

		Stream<JsonNode> stream;
		if (multiSelectNode.isArray()) stream = multiSelectNode.valueStream();
		else stream = multiSelectNode.optional("options").map(JsonNode::valueStream).orElseGet(Stream::empty);

		List<String> results = stream.map(node -> node.get("name").asString()).collect(Collectors.toList());

		if (rawClass != null && Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>(results);
		return results;
	}

	private Object returnEmpty() {
		if (rawClass != null && Set.class.isAssignableFrom(rawClass)) return new LinkedHashSet<>();
		return new ArrayList<>();
	}
}
