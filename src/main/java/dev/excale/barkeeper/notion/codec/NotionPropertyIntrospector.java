package dev.excale.barkeeper.notion.codec;

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
		Cover.class,          CoverDeserializer.class,
		CreatedTime.class,    InstantDeserializer.class,
		Date.class,           DateDeserializer.class,
		LastEditedBy.class,   UserIdDeserializer.class,
		LastEditedTime.class, InstantDeserializer.class,
		MultiSelect.class,    MultiSelectDeserializer.class,
		Number.class,         NumberDeserializer.class,
		Select.class,         SelectDeserializer.class,
		Title.class,          TitleDeserializer.class,
		Url.class,            UrlDeserializer.class
	);

	private static final Map<Class<? extends Annotation>, Class<?>> MAP_SERIALIZERS = Map.of(
		Cover.class,       CoverSerializer.class,
		Date.class,        DateSerializer.class,
		MultiSelect.class, MultiSelectSerializer.class,
		Number.class,      NumberSerializer.class,
		Select.class,      SelectSerializer.class,
		Title.class,       TitleSerializer.class,
		Url.class,         UrlSerializer.class
	);

	private static final Map<Class<? extends Annotation>, PropertyName> MAP_CONST_NAMES = Map.of(
		Cover.class,          PropertyName.construct("cover"),
		CreatedTime.class,    PropertyName.construct("created_time"),
		LastEditedBy.class,   PropertyName.construct("last_edited_by"),
		LastEditedTime.class, PropertyName.construct("last_edited_time"),
		PageId.class,         PropertyName.construct("id"),
		Title.class,          PropertyName.construct("title")
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