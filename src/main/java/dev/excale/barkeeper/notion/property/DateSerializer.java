package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class DateSerializer extends StdSerializer<Object> {

	public DateSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		// "type": "date"
		gen.writeStartObject()
			.writeStringProperty("type", "date");

		if(value != null)
			// "date": { "start": <instant> }
			gen.writeName("date")
				.writeStartObject()
				.writeStringProperty("start", value.toString())
				.writeEndObject();
		else
			// "date": null
			gen.writeName("date")
				.writeNull();

		gen.writeEndObject();
	}

}
