package dev.excale.barkeeper.service;

import dev.excale.barkeeper.entity.GuildSettings;

import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class SignService {

	private final GuildSettingsService guildSettingsService;

	public SignService(GuildSettingsService guildSettingsService) {
		this.guildSettingsService = guildSettingsService;
	}

	public void reconcileGuildSign(Guild guild) {

		Optional<GuildSettings> maybeConfig = guildSettingsService.getForSignChange(guild.getIdLong());
		if(maybeConfig.isEmpty())
			return;

		GuildSettings config = maybeConfig.get();
		if(config.getChannelId() == null) {
			return;
		}

		GuildChannel managedChannel = guild.getGuildChannelById(config.getChannelId());
		if(managedChannel == null) {
			log.warn("Managed channel {} not found for guild {}", config.getChannelId(), guild.getIdLong());
			return;
		}

		String targetName = hasAnyHumanInAudioChannel(guild) ? config.getSignOpen() : config.getSignClosed();
		if(managedChannel.getName()
			.equals(targetName)) {
			return;
		}

		managedChannel.getManager()
			.setName(targetName)
			.queue(
				success -> log.info("Updated channel {} in guild {} to '{}'", managedChannel.getIdLong(),
					guild.getIdLong(), targetName),
				failure -> log.warn(
					"Failed to update channel {} in guild {}: {}",
					managedChannel.getIdLong(),
					guild.getIdLong(),
					failure.getMessage()
				)
			);
	}

	private boolean hasAnyHumanInAudioChannel(Guild guild) {
		return guild.getMembers()
			.stream()
			.anyMatch(member -> isHuman(member)
				&& member.getVoiceState() != null
				&& member.getVoiceState()
				.inAudioChannel());
	}

	private boolean isHuman(Member member) {
		return !member.getUser().isBot();
	}

}


