package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Collection;

public class TitleSerializer extends StdSerializer<Object> {

	public TitleSerializer() {
		super(Object.class);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {

		// "type": "title"
		gen.writeStartObject()
			.writeStringProperty("type", "title");

		if(value == null) {

			// "title": []
			gen.writeName("title")
				.writeStartArray()
				.writeEndArray();

			return;
		}

		// "title": [...]
		gen.writeName("title")
			.writeStartArray();

		Collection<String> titles = (Collection<String>) value;
		for(String title : titles) {

			if(title == null || title.isBlank())
				continue;

			gen.writeStartObject()
				// "type": "text"
				.writeStringProperty("type", "text")
				// "text": { "content": <title> }
				.writeName("text")
					.writeStartObject()
					.writeStringProperty("content", title)
					.writeEndObject()
				.writeEndObject();

		}

		// Close title array and object
		gen.writeEndArray()
			.writeEndObject();

	}

}
