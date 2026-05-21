package dev.excale.barkeeper.discord;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(

	@Min(1)
	@Max(1000)
	@DefaultValue("100")
	Integer cacheSize,

	@NotBlank(message = "Discord bot key is missing")
	String botKey

) {}