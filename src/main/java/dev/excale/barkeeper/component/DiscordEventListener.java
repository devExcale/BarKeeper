package dev.excale.barkeeper.component;

import dev.excale.barkeeper.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Log4j2
@RequiredArgsConstructor
@Component
public class DiscordEventListener extends ListenerAdapter {

	private final SignService signService;

	@Override
	public void onReady(ReadyEvent event) {

		event.getJDA()
			.getGuilds()
			.forEach(signService::reconcileGuildSign);

	}

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		signService.reconcileGuildSign(event.getGuild());
	}

}


