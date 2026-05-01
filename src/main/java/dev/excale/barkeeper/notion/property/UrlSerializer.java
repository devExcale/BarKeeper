package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class UrlSerializer extends StdSerializer<Object> {

	private final Url annotation;

	public UrlSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	public UrlSerializer(Url annotation) {
		super(Object.class);
		this.annotation = annotation;
	}

	@Override
	public ValueSerializer<?> createContextual(
		SerializationContext ctx,
		BeanProperty property
	) throws DatabindException {

		if (property == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use UrlSerializer outside of a field context. " +
					"It must be attached to a specific field via @Url."
			);
			return null;
		}

		Url urlMeta = property.getAnnotation(Url.class);

		if (urlMeta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use UrlSerializer without @Url annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new UrlSerializer(urlMeta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"UrlSerializer was not properly contextualized with @Url annotation."
			);
			return;
		}

		gen.writeStartObject()
			.writeStringProperty("url", value.toString())
		.writeEndObject();

	}

}

