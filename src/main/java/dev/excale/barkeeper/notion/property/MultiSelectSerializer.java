package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Collection;

public class MultiSelectSerializer extends StdSerializer<Object> {

	private final MultiSelect annotation;

	public MultiSelectSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	public MultiSelectSerializer(MultiSelect annotation) {
		super(Object.class);
		this.annotation = annotation;
	}

	@Override
	public ValueSerializer<?> createContextual(
		SerializationContext ctx,
		BeanProperty property
	) throws DatabindException {

		if(property == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use MultiSelectSerializer outside of a field context. " +
					"It must be attached to a specific field via @MultiSelect."
			);
			return null; // Unreachable
		}

		MultiSelect multiSelectMeta = property.getAnnotation(MultiSelect.class);

		if(multiSelectMeta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use MultiSelectSerializer outside of a field context. " +
					"It must be attached to a specific field via @MultiSelect."
			);
			return null; // Unreachable
		}

		return new MultiSelectSerializer(multiSelectMeta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if(annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"MultiSelectSerializer was not properly contextualized with @MultiSelect annotation."
			);
			return; // Unreachable
		}

		gen.writeStartObject()
			.writeName("multi_select")
				.writeStartArray();

		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				gen.writeStartObject()
					.writeStringProperty("name", item.toString())
				.writeEndObject();
			}
		}

		gen.writeEndArray()
		.writeEndObject();
	}
}

