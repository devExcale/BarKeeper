package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class ParentSerializer extends StdSerializer<Object> {

	public ParentSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		// "parent": null
		if (value == null) {
			gen.writeNull();
			return;
		}

		// "parent": ...
		gen.writeStartObject()
			// "type": "data_source_id"
			.writeStringProperty("type", "data_source_id")
			// "data_source_id": <value>
			.writeStringProperty("data_source_id", value.toString())
		.writeEndObject();

	}

}
