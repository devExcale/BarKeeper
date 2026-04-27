package dev.excale.barkeeper.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface DiscordSlashCommandHandler {

	String commandName();

	CommandData commandData();

	void handle(SlashCommandInteractionEvent event);

}

