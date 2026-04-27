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
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static dev.excale.barkeeper.util.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@Log4j2
@CommandController
public class SignChannelCommand {

	private final GuildSettingsService guildSettingsService;

	private final SignService signService;

	@SlashMapping(
		name = "signchannel",
		description = "Set the guild channel managed as open/closed sign"
	)
	public ReplyCallbackAction handle(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "channel", description = "Sign channel") GuildChannelUnion channel
	) {

		Guild guild = event.getGuild();
		if(guild == null)
			return replyEphemeralWith("This command can only be used inside a guild.", event);

		if(!hasManageServerPermission(event.getMember()))
			return replyEphemeralWith("You need MANAGE_SERVER permission to use this command.", event);

		if(!isRenameable(channel))
			return replyEphemeralWith("The selected channel type cannot be renamed.", event);

		Member selfMember = guild.getSelfMember();
		if(!selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL))
			return replyEphemeralWith("I need MANAGE_CHANNEL permission on that channel.", event);

		try {

			Optional<GuildSettings> updated = guildSettingsService.setManagedChannel(guild.getIdLong(),
				channel.getIdLong());
			if(updated.isEmpty())
				return replyEphemeralWith("Could not persist config for this guild.", event);

			signService.reconcileGuildSign(guild);
			return replyEphemeralWith("Managed sign channel set to #" + channel.getName() + ".", event);

		} catch(DataAccessException ex) {
			log.warn("DB error during setsign in guild {}", guild.getIdLong(), ex);
			return replyEphemeralWith("Database error while saving the sign channel.", event);
		}
	}

	private boolean hasManageServerPermission(Member member) {
		return member != null && member.hasPermission(Permission.MANAGE_SERVER);
	}

	private boolean isRenameable(GuildChannel channel) {
		try {
			channel.getManager()
				.setName(channel.getName());
			return true;
		} catch(IllegalStateException | UnsupportedOperationException ex) {
			return false;
		}
	}

}


