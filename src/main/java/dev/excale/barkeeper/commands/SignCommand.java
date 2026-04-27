package dev.excale.barkeeper.commands;

import dev.excale.barkeeper.commands.core.annotation.CmdOption;
import dev.excale.barkeeper.commands.core.annotation.CommandController;
import dev.excale.barkeeper.commands.core.annotation.SlashMapping;
import dev.excale.barkeeper.entity.GuildSettings;
import dev.excale.barkeeper.service.GuildSettingsService;
import dev.excale.barkeeper.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static dev.excale.barkeeper.util.DiscordUtil.replyEphemeralWith;

@Log4j2
@RequiredArgsConstructor
@CommandController(
	name = "sign",
	description = "Configure sign text for open/closed voice state"
)
public class SignCommand {

	private static final int DISCORD_CHANNEL_NAME_MAX = 100;
	private static final String OPEN = "open";
	private static final String CLOSED = "closed";

	private final GuildSettingsService guildSettingsService;
	private final SignService signService;

	@SlashMapping(
		name = "open",
		description = "Set sign text for open voice state"
	)
	public ReplyCallbackAction setOpen(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "name", description = "Channel open name", maxLength = DISCORD_CHANNEL_NAME_MAX) String openName
	) {
		return setSignText(event, OPEN, openName);
	}

	@SlashMapping(
		name = "closed",
		description = "Set sign text for closed voice state"
	)
	public ReplyCallbackAction setClosed(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "name", description = "Channel closed name", maxLength = DISCORD_CHANNEL_NAME_MAX) String closedName
	) {
		return setSignText(event, CLOSED, closedName);
	}

	private ReplyCallbackAction setSignText(SlashCommandInteractionEvent event, String mode, String rawName) {
		Guild guild = event.getGuild();
		if(guild == null)
			return replyEphemeralWith("This command can only be used inside a guild.", event);

		if(!hasManageServerPermission(event.getMember()))
			return replyEphemeralWith("You need MANAGE_SERVER permission to use this command.", event);

		String name = rawName == null ? "" : rawName.trim();

		if(name.isEmpty())
			return replyEphemeralWith("Name cannot be empty.", event);

		if(name.length() > DISCORD_CHANNEL_NAME_MAX)
			return replyEphemeralWith("Name is too long. Discord channel names can be at most 100 characters.", event);

		try {

			Optional<GuildSettings> updated = OPEN.equals(mode)
				? guildSettingsService.setOpenSign(guild.getIdLong(), name)
				: guildSettingsService.setClosedSign(guild.getIdLong(), name);

			if(updated.isEmpty())
				return replyEphemeralWith("Could not persist config for this guild.", event);

			signService.reconcileGuildSign(guild);
			return replyEphemeralWith("Sign text updated for state '" + mode + "'.", event);

		} catch(DataAccessException ex) {
			log.warn("DB error during writesign in guild {}", guild.getIdLong(), ex);
			return replyEphemeralWith("Database error while saving sign text.", event);
		}
	}

	private boolean hasManageServerPermission(Member member) {
		return member != null && member.hasPermission(Permission.MANAGE_SERVER);
	}


}


