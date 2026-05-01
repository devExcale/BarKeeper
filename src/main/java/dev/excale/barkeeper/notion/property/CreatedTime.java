package dev.excale.barkeeper.notion.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JsonSerialize(using = CreatedTimeSerializer.class)
@JsonDeserialize(using = CreatedTimeDeserializer.class)
public @interface CreatedTime {

	String value();

}
