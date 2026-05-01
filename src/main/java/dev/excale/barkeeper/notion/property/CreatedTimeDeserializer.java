package dev.excale.barkeeper.notion.property;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import java.time.Instant;
public class CreatedTimeDeserializer extends StdDeserializer<Object> {
private final CreatedTime annotation;
public CreatedTimeDeserializer() {
super(Object.class);
this.annotation = null;
}
public CreatedTimeDeserializer(CreatedTime annotation) {
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
"Cannot use CreatedTimeDeserializer outside of a field context."
);
return null;
}
CreatedTime meta = property.getAnnotation(CreatedTime.class);
if (meta == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use CreatedTimeDeserializer without @CreatedTime annotation."
);
return null;
}
return new CreatedTimeDeserializer(meta);
}
@Override
public Object deserialize(JsonParser p, DeserializationContext ctx) {
try {
if (annotation == null) {
return null;
}
JsonNode root = ctx.readTree(p);
if (root == null || root.isNull())
return null;
JsonNode createdTimeNode = root.get("created_time");
if (createdTimeNode == null || createdTimeNode.isNull())
return null;
return Instant.parse(createdTimeNode.asText());
} catch (Exception e) {
return null;
}
}
}