package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Collection;

public class MultiSelectSerializer extends StdSerializer<Object> {

	@SuppressWarnings("unused")
	public MultiSelectSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		gen.writeStartObject()
			.writeName("multi_select")
				.writeStartArray();

		if (value instanceof Collection<?> collection)
			for(Object item : collection) {

				String name = item.toString()
					.replace(",", "");

				gen.writeStartObject()
					.writeStringProperty("name", name)
					.writeEndObject();

			}

		gen.writeEndArray()
			.writeEndObject();
	}
}
