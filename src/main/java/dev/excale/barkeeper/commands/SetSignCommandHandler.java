package dev.excale.barkeeper.commands;

import dev.excale.barkeeper.entity.GuildSettings;
import dev.excale.barkeeper.service.GuildSettingsService;
import dev.excale.barkeeper.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Component
public class SetSignCommandHandler implements DiscordSlashCommandHandler {

	private static final String COMMAND = "setsign";
	private static final String OPTION_CHANNEL = "channel";

	private final GuildSettingsService guildSettingsService;
	private final SignService signService;

	@Override
	public String commandName() {
		return COMMAND;
	}

	@Override
	public CommandData commandData() {
		return Commands.slash(COMMAND, "Set the guild channel managed as open/closed sign")
			.addOption(OptionType.CHANNEL, OPTION_CHANNEL, "Renameable channel to manage", true);
	}

	@Override
	public void handle(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		if(guild == null) {
			reply(event, "This command can only be used inside a guild.");
			return;
		}

		if(!hasManageServerPermission(event.getMember())) {
			reply(event, "You need MANAGE_SERVER permission to use this command.");
			return;
		}

		GuildChannel channel = event.getOption(OPTION_CHANNEL, OptionMapping::getAsChannel);
		if(channel == null) {
			reply(event, "You must provide a guild channel.");
			return;
		}

		if(!isRenameable(channel)) {
			reply(event, "The selected channel type cannot be renamed.");
			return;
		}

		Member selfMember = guild.getSelfMember();
		if(selfMember == null || !selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
			reply(event, "I need MANAGE_CHANNEL permission on that channel.");
			return;
		}

		try {
			Optional<GuildSettings> updated = guildSettingsService.setManagedChannel(guild.getIdLong(), channel.getIdLong());
			if(updated.isEmpty()) {
				reply(event, "Could not persist config for this guild.");
				return;
			}

			signService.reconcileGuildSign(guild);
			reply(event, "Managed sign channel set to #" + channel.getName() + ".");
		} catch(DataAccessException ex) {
			log.warn("DB error during setsign in guild {}", guild.getIdLong(), ex);
			reply(event, "Database error while saving the sign channel.");
		}
	}

	private boolean hasManageServerPermission(Member member) {
		return member != null && member.hasPermission(Permission.MANAGE_SERVER);
	}

	private boolean isRenameable(GuildChannel channel) {
		try {
			channel.getManager().setName(channel.getName());
			return true;
		} catch(IllegalStateException | UnsupportedOperationException ex) {
			return false;
		}
	}

	private void reply(SlashCommandInteractionEvent event, String message) {
		event.reply(message).setEphemeral(true).queue();
	}

}


