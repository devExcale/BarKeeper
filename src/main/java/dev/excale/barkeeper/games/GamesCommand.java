package dev.excale.barkeeper.games;

import dev.excale.barkeeper.discord.command.CommandDispatcher;
import dev.excale.barkeeper.discord.command.annotation.*;
import dev.excale.barkeeper.notion.NotionClient;
import dev.excale.barkeeper.notion.NotionProperties;
import dev.excale.barkeeper.notion.model.GamePage;
import dev.excale.barkeeper.steam.SteamClient;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.steam.model.StoreSearch;
import dev.excale.barkeeper.steam.model.StoreSearchItem;
import dev.excale.barkeeper.sync.GamePageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.excale.barkeeper.discord.DiscordUtil.replyEphemeralWith;

@Log4j2
@RequiredArgsConstructor
@CommandController(
	name = "games",
	description = "Manage the games watchlist"
)
public class GamesCommand {

	private static final Pattern STORE_APP_PATTERN = Pattern.compile("^.*steam[\\w-.]+com/app/(\\d+).*$");

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final SteamClient steamClient;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final NotionClient notionClient;

	private final NotionProperties notionProperties;

	private final GamePageMapper gamePageMapper;

	private final CommandDispatcher commandDispatcher;

	@SlashMapping(
		name = "add",
		description = "Add a game to the watchlist"
	)
	public ReplyCallbackAction addGame(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "url", description = "Game's URL on the Steam store") String storeUrl
	) {

		// Extract the app ID from the provided URL using the regex pattern
		Matcher appMatcher = STORE_APP_PATTERN.matcher(storeUrl);
		if(!appMatcher.matches())
			return replyEphemeralWith("Invalid Steam URL", event);

		String appId = appMatcher.group(1);
		AppDetails appDetails;

		// Fetch game details from Steam API
		try {

			appDetails = steamClient.getAppDetails(appId);

		} catch (Exception e) {
			return replyEphemeralWith("Failed to fetch game details", event);
		}

		// Map the retrieved game details to a Notion page format
		GamePage gamePage = gamePageMapper.update(new GamePage(), appDetails);
		gamePage.setParent(notionProperties.datasourceUuid());
		gamePage.setStorePage(storeUrl);

		// Save the new game page to Notion
		notionClient.createPage(gamePage);

		return replyEphemeralWith(
			String.format("[%s](%s) added successfully!", appDetails.getName(), storeUrl),
			event
		);
	}

	@SlashMapping(
		name = "find",
		description = "Find a game on Steam and add it to the watchlist"
	)
	public ReplyCallbackAction findGame(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "name", description = "Name of the game", minLength = 1) String name
	) {

		// Search for the game on the Steam store using the provided name
		StoreSearch search = steamClient.getStoreSearch(name);
		if(search.getTotal() == 0)
			return replyEphemeralWith("No results found", event);

		// Filter games only (no DLCs, soundtracks, etc.)
		List<StoreSearchItem> items = search.getItems()
			.stream()
			.filter(i -> "app".equalsIgnoreCase(i.getType()))
			.toList();

		if(items.isEmpty())
			return replyEphemeralWith("No results found", event);

		int total = items.size();
		StringBuilder msg = new StringBuilder("## Found: ")
			.append(total)
			.append(" games");

		// Initialize button matrix
		int nRows = (total + 1) / 2;
		List<List<Button>> buttonRows = new ArrayList<>(nRows);
		for(int i = 0; i < nRows; i++)
			buttonRows.add(new ArrayList<>(2));

		List<MessageEmbed> embeds = new ArrayList<>(total);

		try {

			// Loop found games
			int i = 0;
			for(StoreSearchItem item : items) {

				// Title: 1. <game name>
				String title = String.format("%d. %s", i + 1, item.getName());
				title = title.substring(0, Math.min(Button.LABEL_MAX_LENGTH, title.length()));

				// Description: [Steam](https://s.team/a/<appId>)
				String description = String.format("[Steam](%s)", "https://s.team/a/" + item.getId());

				// Footer: [i/total] Searched for: "<name>"
				String footer = String.format("[%d/%d] Searched for: \"%s\"", i + 1, total, name);

				// Color: Hash of the game name (truncated to 24 bits for RGB)
				int color = item.getName().hashCode() & 0xFFFFFF;

				EmbedBuilder builder = new EmbedBuilder()
					.setTitle(title)
					.setDescription(description)
					.setImage(item.getTinyImage())
					.setFooter(footer)
					.setColor(color);

				embeds.add(builder.build());

				// Get row
				List<Button> row = buttonRows.get(i / 2);

				// Add callback button to row
				row.add(
					Button.primary(
						commandDispatcher.serializeBtnOptions("add", item.getId()),
						title
					)
				);

				i++;
			}

		} catch(IOException e) {
			// TODO: Proper logging and error handling
			log.error("Failed to serialize button options {}", items, e);
			return replyEphemeralWith("An internal error occurred", event);
		}

		// TODO: Replace with Text Select
		return event.reply(msg.toString())
			.setEmbeds(embeds)
			.setComponents(
				buttonRows.stream()
					.map(ActionRow::of)
					.toList()
			)
			.setEphemeral(true);

	}

	@ButtonMapping(name = "add")
	public MessageEditCallbackAction findGameThenAdd(
		ButtonInteractionEvent event,
		@BtnOption Integer appId
	) {

		AppDetails appDetails;

		// Fetch game details from Steam API
		try {

			appDetails = steamClient.getAppDetails(appId.toString());

		} catch (Exception e) {
			return event.editMessage("Failed to fetch game details");
		}

		// Map the retrieved game details to a Notion page format
		GamePage gamePage = gamePageMapper.update(new GamePage(), appDetails);
		gamePage.setParent(notionProperties.datasourceUuid());
		gamePage.setStorePage("https://store.steampowered.com/app/" + appId);

		// Save the new game page to Notion
		notionClient.createPage(gamePage);

		return event.editMessage(
				String.format("[%s](%s) added successfully!", appDetails.getName(), "https://s.team/a/" + appId)
			)
			.setComponents(List.of())
			.setEmbeds(List.of());

	}

}
