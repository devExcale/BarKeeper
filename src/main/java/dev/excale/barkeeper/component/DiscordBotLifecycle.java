package dev.excale.barkeeper.component;

import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Log4j2
@Component
public class DiscordBotLifecycle {

	private final String token;
	private final DiscordEventListener discordEventListener;
	private JDA jda;

	public DiscordBotLifecycle(
		@Value("${discord.token:}") String token,
		DiscordEventListener discordEventListener
	) {
		this.token = token;
		this.discordEventListener = discordEventListener;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startBot() {

		if(!StringUtils.hasText(token)) {
			log.error("discord.token not configured, skipping Discord bot startup.");
			return;
		}

		this.jda = JDABuilder.createDefault(token)
			.enableIntents(
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.MESSAGE_CONTENT,
				GatewayIntent.GUILD_VOICE_STATES
			)
			.addEventListeners(discordEventListener)
			.build();

		log.info("Discord bot startup initiated.");

	}

	@PreDestroy
	public void shutdown() {
		if(jda != null)
			jda.shutdown();
	}

}

