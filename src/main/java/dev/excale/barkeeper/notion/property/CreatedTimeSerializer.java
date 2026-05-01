package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;

public class CreatedTimeSerializer extends StdSerializer<Object> {

	private final CreatedTime annotation;

	public CreatedTimeSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	public CreatedTimeSerializer(CreatedTime annotation) {
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
				"Cannot use CreatedTimeSerializer outside of a field context. " +
					"It must be attached to a specific field via @CreatedTime."
			);
			return null;
		}

		CreatedTime meta = property.getAnnotation(CreatedTime.class);

		if (meta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use CreatedTimeSerializer without @CreatedTime annotation on field " +
					property.getName() + "."
			);
			return null;
		}

		return new CreatedTimeSerializer(meta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if (annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"CreatedTimeSerializer was not properly contextualized with @CreatedTime annotation."
			);
			return;
		}

		gen.writeStartObject()
			.writeStringProperty("created_time", value.toString())
		.writeEndObject();
	}

}
