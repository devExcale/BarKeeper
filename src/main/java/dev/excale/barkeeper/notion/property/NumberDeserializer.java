package dev.excale.barkeeper.notion.property;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
public class NumberDeserializer extends StdDeserializer<Object> {
private final Number annotation;
private final Class<?> rawClass;
public NumberDeserializer() {
super(Object.class);
this.annotation = null;
this.rawClass = null;
}
public NumberDeserializer(Number annotation, Class<?> rawClass) {
super(Object.class);
this.annotation = annotation;
this.rawClass = rawClass;
}
@Override
public ValueDeserializer<?> createContextual(
DeserializationContext ctx,
BeanProperty property
) throws DatabindException {
if (property == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use NumberDeserializer outside of a field context. " +
"It must be attached to a specific field via @Number."
);
return null;
}
Number meta = property.getAnnotation(Number.class);
if (meta == null) {
ctx.reportBadDefinition(
handledType(),
"Cannot use NumberDeserializer without @Number annotation on field " +
property.getName() + "."
);
return null;
}
return new NumberDeserializer(meta, property.getType().getRawClass());
}
@Override
public Object deserialize(JsonParser p, DeserializationContext ctx) {
if (annotation == null) {
ctx.reportBadDefinition(handledType(), "NumberDeserializer was not contextualized.");
return null;
}
JsonNode root = ctx.readTree(p);
if (root == null || root.isNull())
return null;
JsonNode numberNode = root.get("number");
if (numberNode == null || numberNode.isNull())
return null;
if (rawClass == Double.class || rawClass == double.class) return numberNode.asDouble();
if (rawClass == Integer.class || rawClass == int.class) return numberNode.asInt();
if (rawClass == Long.class || rawClass == long.class) return numberNode.asLong();
if (rawClass == Float.class || rawClass == float.class) return (float) numberNode.asDouble();
return numberNode.asDouble();
}
}
