package dev.excale.barkeeper.salesnotif;

import dev.excale.barkeeper.discord.command.annotation.CmdOption;
import dev.excale.barkeeper.discord.command.annotation.CommandController;
import dev.excale.barkeeper.discord.command.annotation.SlashMapping;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.Optional;

import static dev.excale.barkeeper.discord.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@CommandController(
	name = "sales",
	description = "Manage sales notifications"
)
public class SalesCommand {

	private final SalesNotifSettingsRepository settingsRepo;

	private final SalesNotifierService notifierService;

	@SlashMapping(
		name = "enable",
		description = "Enable sales notifications"
	)
	public ReplyCallbackAction enableNotifications(
		@CmdOption(
			name = "channel",
			description = "The channel to send notifications to",
			required = false
		) GuildChannelUnion channel,
		SlashCommandInteractionEvent event
	) {

		// Get guild
		Guild guild = event.getGuild();
		if(guild == null)
			return replyEphemeralWith("This command can only be used in a guild", event);

		// Retrieve settings for guild
		Optional<SalesNotifSettings> optSettings = settingsRepo.findById(guild.getIdLong());
		SalesNotifSettings settings;

		if(channel == null && optSettings.map(SalesNotifSettings::getChannelId).isEmpty())
			return replyEphemeralWith("You must set a channel where to receive notifications with `/sales enable <channel>`", event);
		else if(channel != null && !(channel.getType().isMessage() || channel.getType() == ChannelType.FORUM))
			return replyEphemeralWith("The specified channel must be a text channel", event);
		else
			settings = optSettings.orElseGet(
				() -> SalesNotifSettings.builder()
					.guildId(guild.getIdLong())
					.channelId(channel.getIdLong())
					.build()
			);

		// Enable notifications
		settings.setEnabled(true);
		settings.setChannelId(channel != null ? channel.getIdLong() : settings.getChannelId());

		// Save settings
		settingsRepo.save(settings);

		// Reply to user
		return event.reply(
			"Sales notifications enabled in <#" + settings.getChannelId() + ">!\n\n" +
			"Notifications will be sent every day at 2 PM CEST with the discounts of the day."
		);
	}

	@SlashMapping(
		name = "disable",
		description = "Disable sales notifications"
	)
	public ReplyCallbackAction disableNotifications(SlashCommandInteractionEvent event) {

		// Get guild
		Guild guild = event.getGuild();
		if(guild == null)
			return replyEphemeralWith("This command can only be used in a guild", event);

		// Retrieve settings for guild
		Optional<SalesNotifSettings> optSettings = settingsRepo.findById(guild.getIdLong());
		if(optSettings.isEmpty())
			return replyEphemeralWith("Sales notifications are not enabled in this guild", event);

		SalesNotifSettings settings = optSettings.get();

		// Disable notifications
		settings.setEnabled(false);

		// Save settings
		settingsRepo.save(settings);

		// Reply to user
		return event.reply("Sales notifications disabled.");
	}

	@SlashMapping(
		name = "now",
		description = "Send sales notification immediately"
	)
	public ReplyCallbackAction sendSalesNow(SlashCommandInteractionEvent event) {

		// Get guild
		Guild guild = event.getGuild();
		if(guild == null)
			return replyEphemeralWith("This command can only be used in a guild", event);

		// Retrieve settings for guild
		Optional<SalesNotifSettings> optSettings = settingsRepo.findById(guild.getIdLong());
		if(optSettings.isEmpty())
			return replyEphemeralWith("Sales notifications are not set up in this guild", event);

		SalesNotifSettings settings = optSettings.get();

		// Get channel
		TextChannel channel = guild.getTextChannelById(settings.getChannelId());
		ForumChannel forum = guild.getForumChannelById(settings.getChannelId());
		if(channel == null && forum == null)
			return replyEphemeralWith("Sales notifications are not set up in this guild", event);

		String channelMention;

		// Send discounted games
		if(channel != null) {
			notifierService.sendSalesNotificationGuild(channel);
			channelMention = channel.getAsMention();
		} else {
			notifierService.sendSalesNotificationUsingThread(forum);
			channelMention = forum.getAsMention();
		}

		// Reply to user
		return replyEphemeralWith("Sent sales notification in " + channelMention, event);
	}

}
