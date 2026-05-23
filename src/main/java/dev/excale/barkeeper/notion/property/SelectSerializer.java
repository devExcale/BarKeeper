package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class SelectSerializer extends StdSerializer<Object> {

	/**
	 * Default constructor needed by Jackson
	 */
	@SuppressWarnings("unused")
	public SelectSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		// Write the select property structure
		gen.writeStartObject()
			.writeName("select");

		if(value != null)
			gen.writeStartObject()
				.writeStringProperty("name", value.toString())
				.writeEndObject();
		else
			gen.writeNull();

		gen.writeEndObject();

	}

}
