package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CoverDeserializer extends StdDeserializer<Object> {

	private final Cover annotation;

	public CoverDeserializer() {
		super(Object.class);
		this.annotation = null;
	}

	public CoverDeserializer(Cover annotation) {
		super(Object.class);
		this.annotation = annotation;
	}

	@Override
	public ValueDeserializer<?> createContextual(
		DeserializationContext ctx,
		BeanProperty property
	) throws DatabindException {

		if (property == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use CoverDeserializer outside of a field context. " +
					"It must be attached to a specific field via @Cover."
			);
			return null;
		}

		Cover meta = property.getAnnotation(Cover.class);

		if (meta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use CoverDeserializer without @Cover annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new CoverDeserializer(meta);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(handledType(), "CoverDeserializer was not contextualized.");
			return null;
		}

		JsonNode root = ctx.readTree(p);

		if (root == null || root.isNull())
			return null;

		return root.optional("external")
			.map(node -> node.get("url"))
			.map(JsonNode::asString)
			.orElse(null);
	}

}

