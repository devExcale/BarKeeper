package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class NumberSerializer extends StdSerializer<Object> {

	private final Number annotation;

	public NumberSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	public NumberSerializer(Number annotation) {
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
				"Cannot use NumberSerializer outside of a field context. " +
					"It must be attached to a specific field via @Number."
			);
			return null;
		}

		Number meta = property.getAnnotation(Number.class);

		if (meta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use NumberSerializer without @Number annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new NumberSerializer(meta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"NumberSerializer was not properly contextualized with @Number annotation."
			);
			return;
		}

		gen.writeStartObject()
			.writeName("number")
			.writeNumber(((java.lang.Number) value).doubleValue())
		.writeEndObject();
	}

}

