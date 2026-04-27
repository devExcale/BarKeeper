package dev.excale.barkeeper.component;

import dev.excale.barkeeper.commands.core.CommandDispatcher;
import dev.excale.barkeeper.commands.core.event.CommandUpdateEvent;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Log4j2
@Component
public class DiscordService {

	@Getter(onMethod_ = @Bean(destroyMethod = "shutdown"))
	private final JDA jda;

	public DiscordService(
		@Value("${discord.token:}") String token,
		DiscordEventListener discordEventListener,
		CommandDispatcher commandDispatcher
	) {

		this.jda = JDABuilder.createDefault(token)
			.enableIntents(
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.MESSAGE_CONTENT,
				GatewayIntent.GUILD_VOICE_STATES
			)
			.addEventListeners(discordEventListener, commandDispatcher)
			.build();

		log.info("JDA connected");
	}

	@EventListener
	public void onCommandUpdateEvent(CommandUpdateEvent event) {

		CommandData[] commandData = event.getCommandData();

		String commandListString = jda
			.updateCommands()
			.addCommands(commandData)
			.complete()
			.stream()
			.map(Command::getName)
			.collect(Collectors.joining(", "));

		log.info("[Registered SlashCommands: {}] {}", commandData.length, commandListString);
	}

}

