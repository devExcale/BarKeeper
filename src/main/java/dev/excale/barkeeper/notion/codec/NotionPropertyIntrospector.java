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

	private static final String PROPERTY_PACKAGE = PageId.class.getPackageName();

	private static final Map<Class<? extends Annotation>, Class<?>> MAP_DESERIALIZERS = Map.ofEntries(
		Map.entry(Cover.class,          CoverDeserializer.class),
		Map.entry(CreatedTime.class,    InstantDeserializer.class),
		Map.entry(Date.class,           DateDeserializer.class),
		Map.entry(LastEditedBy.class,   UserIdDeserializer.class),
		Map.entry(LastEditedTime.class, InstantDeserializer.class),
		Map.entry(MultiSelect.class,    MultiSelectDeserializer.class),
		Map.entry(Number.class,         NumberDeserializer.class),
		Map.entry(Parent.class,         ParentDeserializer.class),
		Map.entry(Select.class,         SelectDeserializer.class),
		Map.entry(Title.class,          TitleDeserializer.class),
		Map.entry(Url.class,            UrlDeserializer.class)
	);

	private static final Map<Class<? extends Annotation>, Class<?>> MAP_SERIALIZERS = Map.ofEntries(
		Map.entry(Cover.class,       CoverSerializer.class),
		Map.entry(Date.class,        DateSerializer.class),
		Map.entry(MultiSelect.class, MultiSelectSerializer.class),
		Map.entry(Number.class,      NumberSerializer.class),
		Map.entry(Parent.class,      ParentSerializer.class),
		Map.entry(Select.class,      SelectSerializer.class),
		Map.entry(Title.class,       TitleSerializer.class),
		Map.entry(Url.class,         UrlSerializer.class)
	);

	private static final Map<Class<? extends Annotation>, PropertyName> MAP_CONST_NAMES = Map.ofEntries(
		Map.entry(Cover.class,          PropertyName.construct("cover")),
		Map.entry(CreatedTime.class,    PropertyName.construct("created_time")),
		Map.entry(LastEditedBy.class,   PropertyName.construct("last_edited_by")),
		Map.entry(LastEditedTime.class, PropertyName.construct("last_edited_time")),
		Map.entry(PageId.class,         PropertyName.construct("id")),
		Map.entry(Parent.class,         PropertyName.construct("parent")),
		Map.entry(Title.class,          PropertyName.construct("title"))
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

		// Iterate over all annotations on the field
		return a.annotations()
			// Filter only Notion annotations
			.filter(
				ann -> ann.annotationType()
				.getPackageName()
				.startsWith(PROPERTY_PACKAGE)
			)
			// Extract the "value" property via reflection (if it exists)
			.map(ann -> {
				try {
					Method valueMethod = ann.annotationType().getMethod("value");
					String id = (String) valueMethod.invoke(ann);
					return PropertyName.construct(id);
				} catch(Exception e) {
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