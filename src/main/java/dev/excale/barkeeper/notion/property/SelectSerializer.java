package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class SelectSerializer extends StdSerializer<Object> {

	private final Select annotation;

	/**
	 * Default constructor needed by Jackson
	 */
	public SelectSerializer() {
		super(Object.class);
		this.annotation = null;
	}

	/**
	 * Constructor used for contextualization with the @Select annotation
	 *
	 * @param annotation @Select annotation
	 */
	public SelectSerializer(Select annotation) {
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
				"Cannot use NotionSelectSerializer outside of a field context. " +
					"It must be attached to a specific field via @NotionSelect."
			);
			return null; // Unreachable
		}

		// Get the @Select annotation from the property
		Select selectMeta = property.getAnnotation(Select.class);

		if(selectMeta == null) {
			ctx.reportBadDefinition(
				handledType(),
				"Cannot use NotionSelectSerializer outside of a field context. " +
					"It must be attached to a specific field via @NotionSelect."
			);
			return null; // Unreachable
		}

		// Create new contextualized serializer
		return new SelectSerializer(selectMeta);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		// Check annotation is present
		if(annotation == null) {
			ctx.reportBadDefinition(
				handledType(),
				"SelectSerializer was not properly contextualized with @Select annotation."
			);
			return; // Unreachable
		}

		// Write the select property structure
		gen.writeStartObject()
			.writeName("select")
				.writeStartObject()
				.writeStringProperty("name", value.toString())
				.writeEndObject()
		.writeEndObject();

	}

}
