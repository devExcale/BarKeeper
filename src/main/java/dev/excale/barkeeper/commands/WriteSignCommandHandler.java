package dev.excale.barkeeper.commands;

import dev.excale.barkeeper.entity.GuildSettings;
import dev.excale.barkeeper.service.GuildSettingsService;
import dev.excale.barkeeper.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Component
public class WriteSignCommandHandler implements DiscordSlashCommandHandler {

	private static final int DISCORD_CHANNEL_NAME_MAX = 100;
	private static final String COMMAND = "writesign";
	private static final String OPTION_STATE = "state";
	private static final String OPTION_NAME = "name";
	private static final String OPEN = "open";
	private static final String CLOSED = "closed";

	private final GuildSettingsService guildSettingsService;
	private final SignService signService;

	@Override
	public String commandName() {
		return COMMAND;
	}

	@Override
	public CommandData commandData() {
		OptionData stateOption = new OptionData(OptionType.STRING, OPTION_STATE, "State to configure", true)
			.addChoice(OPEN, OPEN)
			.addChoice(CLOSED, CLOSED);

		OptionData nameOption = new OptionData(OptionType.STRING, OPTION_NAME, "Channel name to apply", true);

		return Commands.slash(COMMAND, "Set sign text for open/closed voice state")
			.addOptions(stateOption, nameOption);
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

		String mode = event.getOption(OPTION_STATE, OPEN, option -> option.getAsString().trim().toLowerCase());
		String name = event.getOption(OPTION_NAME, "", option -> option.getAsString().trim());

		if(name.isEmpty()) {
			reply(event, "Name cannot be empty.");
			return;
		}
		if(name.length() > DISCORD_CHANNEL_NAME_MAX) {
			reply(event, "Name is too long. Discord channel names can be at most 100 characters.");
			return;
		}

		try {
			Optional<GuildSettings> updated;
			if(OPEN.equals(mode)) {
				updated = guildSettingsService.setOpenSign(guild.getIdLong(), name);
			} else if(CLOSED.equals(mode)) {
				updated = guildSettingsService.setClosedSign(guild.getIdLong(), name);
			} else {
				reply(event, "Invalid state. Use open or closed.");
				return;
			}

			if(updated.isEmpty()) {
				reply(event, "Could not persist config for this guild.");
				return;
			}

			signService.reconcileGuildSign(guild);
			reply(event, "Sign text updated for state '" + mode + "'.");
		} catch(DataAccessException ex) {
			log.warn("DB error during writesign in guild {}", guild.getIdLong(), ex);
			reply(event, "Database error while saving sign text.");
		}
	}

	private boolean hasManageServerPermission(Member member) {
		return member != null && member.hasPermission(Permission.MANAGE_SERVER);
	}

	private void reply(SlashCommandInteractionEvent event, String message) {
		event.reply(message).setEphemeral(true).queue();
	}

}


