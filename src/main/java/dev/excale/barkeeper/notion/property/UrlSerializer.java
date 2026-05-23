package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class UrlSerializer extends StdSerializer<Object> {

	@SuppressWarnings("unused")
	public UrlSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		gen.writeStartObject()
			.writeName("url");

		if (value != null)
			gen.writeString(value.toString());
		else
			gen.writeNull();

		gen.writeEndObject();

	}

}
