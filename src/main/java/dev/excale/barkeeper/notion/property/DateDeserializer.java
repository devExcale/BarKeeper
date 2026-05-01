package dev.excale.barkeeper.notion.property;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
public class DateDeserializer extends StdDeserializer<Object> {
private final Date annotation;
public DateDeserializer() {
super(Object.class);
this.annotation = null;
}
public DateDeserializer(Date annotation) {
super(Object.class);
this.annotation = annotation;
}
@Override
public ValueDeserializer<?> createContextual(
DeserializationContext ctx,
BeanProperty property
) throws DatabindException {
if (property == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use DateDeserializer outside of a field context. " +
"It must be attached to a specific field via @Date."
);
return null;
}
Date meta = property.getAnnotation(Date.class);
if (meta == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use DateDeserializer without @Date annotation on field " +
property.getName() + "."
);
return null;
}
return new DateDeserializer(meta);
}
@Override
public Object deserialize(JsonParser p, DeserializationContext ctx) {
if (annotation == null) {
ctx.reportBadDefinition(handledType(), "DateDeserializer was not contextualized.");
return null;
}
JsonNode root = ctx.readTree(p);
if (root == null || root.isNull())
return null;
JsonNode dateNode = root.get("date");
if (dateNode == null || dateNode.isNull())
return null;
JsonNode startNode = dateNode.get("start");
if (startNode == null || startNode.isNull() || startNode.asText().isEmpty())
return null;
String start = startNode.asText();
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
