package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class CoverSerializer extends StdSerializer<Object> {

	@SuppressWarnings("unused")
	public CoverSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		if (value == null) {
			gen.writeNull();
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
