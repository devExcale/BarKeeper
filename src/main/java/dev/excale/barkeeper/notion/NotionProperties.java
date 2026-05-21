package dev.excale.barkeeper.notion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
@ConfigurationProperties(prefix = "notion")
public record NotionProperties(

	@NotBlank(message = "Notion API key is missing")
	String apiKey,

	@DefaultValue("2026-03-11")
	String apiVersion,

	@NotNull(message = "Notion bot UUID is missing or invalid")
	UUID botUuid,

	@NotBlank(message = "Notion datasource ID is missing")
	String datasourceId

) {}