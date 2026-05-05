package dev.excale.barkeeper.notion;

import dev.excale.barkeeper.notion.property.*;
import dev.excale.barkeeper.notion.property.Number;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class NotionPropertyIntrospector extends JacksonAnnotationIntrospector {

	private static final Map<Class<? extends Annotation>, Class<?>> MAP_DESERIALIZERS = Map.of(
		Select.class,         SelectDeserializer.class,
		MultiSelect.class,    MultiSelectDeserializer.class,
		Url.class,            UrlDeserializer.class,
		Cover.class,          CoverDeserializer.class,
		Number.class,         NumberDeserializer.class,
		Date.class,           DateDeserializer.class,
		CreatedTime.class,    InstantDeserializer.class,
		Title.class,          TitleDeserializer.class,
		LastEditedTime.class, InstantDeserializer.class
	);

	private static final Map<Class<? extends Annotation>, Class<?>> MAP_SERIALIZERS = Map.of(
		Select.class,      SelectSerializer.class,
		MultiSelect.class, MultiSelectSerializer.class,
		Url.class,         UrlSerializer.class,
		Cover.class,       CoverSerializer.class,
		Number.class,      NumberSerializer.class,
		Date.class,        DateSerializer.class,
		Title.class,       TitleSerializer.class
	);

	private static final Map<Class<? extends Annotation>, PropertyName> MAP_CONST_NAMES = Map.of(
		PageId.class,         PropertyName.construct("id"),
		Cover.class,          PropertyName.construct("cover"),
		Title.class,          PropertyName.construct("title"),
		CreatedTime.class,    PropertyName.construct("created_time"),
		LastEditedTime.class, PropertyName.construct("last_edited_time")
	);

	@Override
	public Object findDeserializer(MapperConfig<?> config, Annotated a) {

		for(Map.Entry<Class<? extends Annotation>, Class<?>> entry : MAP_DESERIALIZERS.entrySet())
			if(a.hasAnnotation(entry.getKey()))
				return entry.getValue();

		return super.findDeserializer(config, a);
	}

	@Override
	public Object findSerializer(MapperConfig<?> config, Annotated a) {

		for(Map.Entry<Class<? extends Annotation>, Class<?>> entry : MAP_SERIALIZERS.entrySet())
			if(a.hasAnnotation(entry.getKey()))
				return entry.getValue();

		return super.findSerializer(config, a);
	}

	@Override
	public Object findNullSerializer(MapperConfig<?> config, Annotated a) {

		for(Map.Entry<Class<? extends Annotation>, Class<?>> entry : MAP_SERIALIZERS.entrySet())
			if(a.hasAnnotation(entry.getKey()))
				return entry.getValue();

		return super.findNullSerializer(config, a);
	}

	@Override
	public PropertyName findNameForDeserialization(MapperConfig<?> config, Annotated a) {

		for(Map.Entry<Class<? extends Annotation>, PropertyName> entry : MAP_CONST_NAMES.entrySet())
			if(a.hasAnnotation(entry.getKey()))
				return entry.getValue();

		PropertyName name = extractNotionPropertyId(a);
		return name != null ? name : super.findNameForDeserialization(config, a);
	}

	@Override
	public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated a) {

		for(Map.Entry<Class<? extends Annotation>, PropertyName> entry : MAP_CONST_NAMES.entrySet())
			if(a.hasAnnotation(entry.getKey()))
				return entry.getValue();

		PropertyName name = extractNotionPropertyId(a);

		return name != null ? name : super.findNameForSerialization(config, a);
	}

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