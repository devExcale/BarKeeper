package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class NumberSerializer extends StdSerializer<Object> {

	@SuppressWarnings("unused")
	public NumberSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		gen.writeStartObject()
			.writeName("number");

		if(value != null)
			gen.writeNumber(((java.lang.Number) value).doubleValue());
		else
			gen.writeNull();

		gen.writeEndObject();
	}

}
