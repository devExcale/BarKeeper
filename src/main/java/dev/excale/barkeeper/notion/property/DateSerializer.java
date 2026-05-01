package dev.excale.barkeeper.notion.property;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;
import java.time.Instant;
public class DateSerializer extends StdSerializer<Object> {
private final Date annotation;
public DateSerializer() {
super(Object.class);
this.annotation = null;
}
public DateSerializer(Date annotation) {
super(Object.class);
this.annotation = annotation;
}
@Override
public ValueSerializer<?> createContextual(
SerializationContext ctx,
BeanProperty property
) throws DatabindException {
if (property == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use DateSerializer outside of a field context. " +
"It must be attached to a specific field via @Date."
);
return null;
}
Date meta = property.getAnnotation(Date.class);
if (meta == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use DateSerializer without @Date annotation on field " +
property.getName() + "."
);
return null;
}
return new DateSerializer(meta);
}
@Override
public void serialize(Object value, JsonGenerator gen, SerializationContext ctx) {
if (annotation == null) {
ctx.reportBadDefinition(
handledType(),
"DateSerializer was not properly contextualized with @Date annotation."
);
return;
}
gen.writeStartObject()
.writeName("date")
.writeStartObject()
.writeStringProperty("start", value.toString()) // value should be an Instant
.writeEndObject()
.writeEndObject();
}
}
