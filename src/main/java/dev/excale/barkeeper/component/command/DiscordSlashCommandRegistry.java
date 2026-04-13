package dev.excale.barkeeper.component.command;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DiscordSlashCommandRegistry {

	@Getter
	private final List<DiscordSlashCommandHandler> handlers;
	private final Map<String, DiscordSlashCommandHandler> handlerByName;

	public DiscordSlashCommandRegistry(List<DiscordSlashCommandHandler> handlers) {
		this.handlers = List.copyOf(handlers);
		this.handlerByName = this.handlers.stream()
			.collect(Collectors.toUnmodifiableMap(
				DiscordSlashCommandHandler::commandName,
				Function.identity(),
				(first, second) -> {
					throw new IllegalStateException("Duplicate slash command handler name: " + first.commandName());
				}
			));
	}

	public DiscordSlashCommandHandler find(String commandName) {
		return handlerByName.get(commandName);
	}

}
