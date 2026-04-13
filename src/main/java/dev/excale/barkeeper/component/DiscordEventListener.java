package dev.excale.barkeeper.component;

import dev.excale.barkeeper.component.command.DiscordSlashCommandHandler;
import dev.excale.barkeeper.component.command.DiscordSlashCommandRegistry;
import dev.excale.barkeeper.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Log4j2
@RequiredArgsConstructor
@Component
public class DiscordEventListener extends ListenerAdapter {

	private final DiscordSlashCommandRegistry commandRegistry;
	private final SignService signService;

	@Override
	public void onReady(ReadyEvent event) {
		event.getJDA()
			.updateCommands()
			.addCommands(commandRegistry.getHandlers().stream().map(DiscordSlashCommandHandler::commandData).toList())
			.queue(
				success -> log.info("Registered {} slash commands.", success.size()),
				error -> log.error("Failed to register slash commands.", error)
			);

		event.getJDA()
			.getGuilds()
			.forEach(signService::reconcileGuildSign);
	}

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		signService.reconcileGuildSign(event.getGuild());
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		DiscordSlashCommandHandler handler = commandRegistry.find(event.getName());
		if(handler == null) {
			return;
		}

		handler.handle(event);
	}

}


