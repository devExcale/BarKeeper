package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

public class UrlDeserializer extends StdDeserializer<Object> {

	private final Url annotation;

	public UrlDeserializer() {
		super(Object.class);
		this.annotation = null;
	}

	public UrlDeserializer(Url annotation) {
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
				"Cannot use UrlDeserializer outside of a field context. " +
					"It must be attached to a specific field via @Url."
			);
			return null;
		}

		Url urlMeta = property.getAnnotation(Url.class);

		if (urlMeta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use UrlDeserializer without @Url annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new UrlDeserializer(urlMeta);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(handledType(), "UrlDeserializer was not contextualized.");
			return null;
		}

		JsonNode propertyNode = ctx.readTree(p);

		if (propertyNode == null || propertyNode.isNull())
			return null;

		JsonNode urlNode = propertyNode.get("url");
		if (urlNode == null || urlNode.isNull())
			return null;

		return urlNode.asString();
	}

}

