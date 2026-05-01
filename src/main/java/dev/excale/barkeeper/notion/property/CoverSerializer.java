package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class CoverSerializer extends StdSerializer<Object> {

	private final Cover annotation;

	public CoverSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	public CoverSerializer(Cover annotation) {
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
				"Cannot use CoverSerializer outside of a field context. " +
					"It must be attached to a specific field via @Cover."
			);
			return null;
		}

		Cover meta = property.getAnnotation(Cover.class);

		if (meta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use CoverSerializer without @Cover annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new CoverSerializer(meta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"CoverSerializer was not properly contextualized with @Cover annotation."
			);
			return;
		}

		gen.writeStartObject()
			.writeStringProperty("type", "external")
			.writeName("external")
			.writeStartObject()
				.writeStringProperty("url", value.toString())
			.writeEndObject()
		.writeEndObject();
	}

}
