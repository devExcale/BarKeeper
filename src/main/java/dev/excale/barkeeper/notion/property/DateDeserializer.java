package dev.excale.barkeeper.notion.property;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class DateDeserializer extends StdDeserializer<Object> {

	@SuppressWarnings("unused")
	public DateDeserializer() {
		super(Object.class);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctx) {

		JsonNode root = ctx.readTree(p);
		if (root == null || root.isNull())
			return null;

		JsonNode dateNode = root.get("date");
		if (dateNode == null || dateNode.isNull())
			return null;

		JsonNode startNode = dateNode.get("start");
		if (startNode == null || startNode.isNull())
			return null;

		String start = startNode.asString();
		if(start.isEmpty())
			return null;

		try {

			return Instant.parse(start);

		} catch (DateTimeParseException ex) {
			// Try parse as date-only and convert to start of day UTC
			try {
				LocalDate ld = LocalDate.parse(start);
				return ld.atStartOfDay(ZoneOffset.UTC).toInstant();
			} catch (DateTimeParseException ex2) {
				ctx.reportBadDefinition(handledType(), "Unable to parse date: " + start);
				return null;
			}
		}
	}

}
