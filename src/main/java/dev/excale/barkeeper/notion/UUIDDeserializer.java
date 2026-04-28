package dev.excale.barkeeper.notion;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.UUID;

public class UUIDDeserializer extends StdDeserializer<UUID> {

	public UUIDDeserializer() {
		super(UUID.class);
	}

	@Override
	public UUID deserialize(JsonParser p, DeserializationContext ctxt) {
		try {
			String value = p.getValueAsString();
			if (value == null || value.isEmpty()) {
				return null;
			}
			return UUID.fromString(value);
		} catch (Exception e) {
			ctxt.reportInputMismatch(UUID.class, "Invalid UUID format");
			return null;
		}
	}

}

