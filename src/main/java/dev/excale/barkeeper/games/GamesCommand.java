package dev.excale.barkeeper.games;

import dev.excale.barkeeper.discord.command.annotation.CmdOption;
import dev.excale.barkeeper.discord.command.annotation.CommandController;
import dev.excale.barkeeper.discord.command.annotation.SlashMapping;
import dev.excale.barkeeper.notion.NotionClient;
import dev.excale.barkeeper.notion.NotionProperties;
import dev.excale.barkeeper.notion.model.GamePage;
import dev.excale.barkeeper.steam.SteamClient;
import dev.excale.barkeeper.steam.model.AppDetails;
import dev.excale.barkeeper.sync.GamePageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.excale.barkeeper.discord.DiscordUtil.replyEphemeralWith;

@Log4j2
@RequiredArgsConstructor
@CommandController(
	name = "games",
	description = "www"
)
public class GamesCommand {

	private static final Pattern STORE_APP_PATTERN = Pattern.compile("^.*steam[\\w-.]+com/app/(\\d+).*$");

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final SteamClient steamClient;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final NotionClient notionClient;

	private final NotionProperties notionProperties;

	private final GamePageMapper gamePageMapper;

	@SlashMapping(
		name = "add",
		description = "www"
	)
	public ReplyCallbackAction addGame(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "name", description = "Channel open name") String storeUrl
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


}
