package dev.excale.barkeeper.notion;

import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.reflect.Method;
import java.util.Objects;

import dev.excale.barkeeper.notion.property.Cover;
import dev.excale.barkeeper.notion.property.CoverDeserializer;
import dev.excale.barkeeper.notion.property.CoverSerializer;
import dev.excale.barkeeper.notion.property.Url;
import dev.excale.barkeeper.notion.property.UrlDeserializer;
import dev.excale.barkeeper.notion.property.UrlSerializer;
import dev.excale.barkeeper.notion.property.PageId;
import dev.excale.barkeeper.notion.property.Number;
import dev.excale.barkeeper.notion.property.NumberDeserializer;
import dev.excale.barkeeper.notion.property.NumberSerializer;
import dev.excale.barkeeper.notion.property.Date;
import dev.excale.barkeeper.notion.property.DateDeserializer;
import dev.excale.barkeeper.notion.property.DateSerializer;
import dev.excale.barkeeper.notion.property.CreatedTime;
import dev.excale.barkeeper.notion.property.CreatedTimeDeserializer;
import dev.excale.barkeeper.notion.property.CreatedTimeSerializer;
import dev.excale.barkeeper.notion.property.Select;
import dev.excale.barkeeper.notion.property.SelectDeserializer;
import dev.excale.barkeeper.notion.property.SelectSerializer;
import dev.excale.barkeeper.notion.property.MultiSelect;
import dev.excale.barkeeper.notion.property.MultiSelectDeserializer;
import dev.excale.barkeeper.notion.property.MultiSelectSerializer;

public class NotionPropertyIntrospector extends JacksonAnnotationIntrospector {

	@Override
	public Object findDeserializer(MapperConfig<?> config, Annotated a) {
		if (a.hasAnnotation(Select.class)) return SelectDeserializer.class;
		if (a.hasAnnotation(MultiSelect.class)) return MultiSelectDeserializer.class;
		if (a.hasAnnotation(Url.class)) return UrlDeserializer.class;
		if (a.hasAnnotation(Cover.class)) return CoverDeserializer.class;
		if (a.hasAnnotation(Number.class)) return NumberDeserializer.class;
		if (a.hasAnnotation(Date.class)) return DateDeserializer.class;
		if (a.hasAnnotation(CreatedTime.class)) return CreatedTimeDeserializer.class;
		return super.findDeserializer(config, a);
	}

	@Override
	public Object findSerializer(MapperConfig<?> config, Annotated a) {
		if (a.hasAnnotation(Select.class)) return SelectSerializer.class;
		if (a.hasAnnotation(MultiSelect.class)) return MultiSelectSerializer.class;
		if (a.hasAnnotation(Url.class)) return UrlSerializer.class;
		if (a.hasAnnotation(Cover.class)) return CoverSerializer.class;
		if (a.hasAnnotation(Number.class)) return NumberSerializer.class;
		if (a.hasAnnotation(Date.class)) return DateSerializer.class;
		if (a.hasAnnotation(CreatedTime.class)) return CreatedTimeSerializer.class;
		return super.findSerializer(config, a);
	}

	@Override
	public PropertyName findNameForDeserialization(MapperConfig<?> config, Annotated a) {
		PropertyName name = extractNotionPropertyId(a);
		if (name == null && a.hasAnnotation(Cover.class)) return PropertyName.construct("cover");
		if (name == null && a.hasAnnotation(PageId.class)) return PropertyName.construct("id");
		return name != null ? name : super.findNameForDeserialization(config, a);
	}

	@Override
	public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated a) {
		PropertyName name = extractNotionPropertyId(a);
		if (name == null && a.hasAnnotation(Cover.class)) return PropertyName.construct("cover");
		if (name == null && a.hasAnnotation(PageId.class)) return PropertyName.construct("id");
		return name != null ? name : super.findNameForSerialization(config, a);
	}

	// ... (rest of the Introspector class)

	private PropertyName extractNotionPropertyId(Annotated a) {
		return a.annotations()
			// Filter only your custom Notion annotations
			.filter(ann -> ann.annotationType()
				.getPackageName()
				.startsWith("dev.excale.barkeeper.notion.property"))
			.map(ann -> {
				try {
					// Extract the "value" property via reflection
					Method valueMethod = ann.annotationType()
						.getMethod("value");
					String id = (String) valueMethod.invoke(ann);
					return PropertyName.construct(id);
				} catch(Exception e) {
					// Ignore annotations that don't have a value() method
					return null;
				}
			})
			// Drop any nulls from failed reflection attempts
			.filter(Objects::nonNull)
			// Return the first match, or null if none found
			.findFirst()
			.orElse(null);
	}

}