package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

public class SelectDeserializer extends StdDeserializer<Object> {

	private final Select annotation;

	/**
	 * Default constructor needed by Jackson
	 */
	public SelectDeserializer() {
		super(Object.class);
		this.annotation = null;
	}

	/**
	 * Constructor used for contextualization with the @Select annotation
	 *
	 * @param annotation @Select annotation
	 */
	public SelectDeserializer(Select annotation) {
		super(Object.class);
		this.annotation = annotation;
	}

	@Override
	public ValueDeserializer<?> createContextual(
		DeserializationContext ctx,
		BeanProperty property
	) throws DatabindException {

		if(property == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use SelectDeserializer outside of a field context. " +
					"It must be attached to a specific field via @Select."
			);
			return null; // Unreachable
		}

		Select selectMeta = property.getAnnotation(Select.class);

		if(selectMeta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use SelectDeserializer without @Select annotation on field " +
					property.getName() + "."
			);
			return null; // Unreachable
		}

		return new SelectDeserializer(selectMeta);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(handledType(), "SelectDeserializer was not contextualized.");
			return null;
		}

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

