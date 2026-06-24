package dev.excale.barkeeper.salesnotif;

import dev.excale.barkeeper.games.GameEmbeds;
import dev.excale.barkeeper.notion.NotionClient;
import dev.excale.barkeeper.notion.NotionProperties;
import dev.excale.barkeeper.notion.model.GamePage;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SalesNotifierService {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final JDA jda;

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	private final NotionClient notionClient;

	private final NotionProperties notionProps;

	private final SalesNotifSettingsRepository settingsRepository;

	// 2:00 PM every day
	@Scheduled(cron = "0 0 14 * * *", zone = "${sync.cron.timezone:Europe/Rome}")
	public void sendSalesNotificationBatch() {

		// Retrieve all games on sale
		List<GamePage> games = getGamesOnSale();

		// Retrieve all guilds with sales notifications enabled
		for(SalesNotifSettings settings : settingsRepository.findAll()) {

			// Get guild channel
			Optional<TextChannel> optChannel = Optional.of(settings.getGuildId())
				.map(jda::getGuildById)
				.map(guild -> guild.getTextChannelById(settings.getChannelId()));

			// Ignore invalid guilds/channels
			if(optChannel.isEmpty())
				continue;

			sendSalesNotificationGuild(optChannel.get(), games);

		}


	}

	private List<GamePage> getGamesOnSale() {
		return notionClient.queryDataSource(notionProps.datasourceId())
			.stream()
			.filter(g ->  g.getFullPrice() != null)
			.filter(g -> g.getDiscountPrice() != null)
			.filter(g -> g.getDiscountPrice() < g.getFullPrice())
			.toList();
	}

	public void sendSalesNotificationGuild(TextChannel channel) {
		sendSalesNotificationGuild(channel, null);
	}

	public void sendSalesNotificationGuild(TextChannel channel, Collection<GamePage> games) {

		// Find games if not provided
		if(games == null)
			games = getGamesOnSale();

		String message = "## Today's Sales! <t:" + System.currentTimeMillis() / 1000 + ":D>";
		Instant now = Instant.now();

		// Create embeds for each game
		List<MessageEmbed> embeds = games.stream()
			.sorted(
				Comparator.comparing((GamePage g) -> g.getDiscountPrice() / g.getFullPrice())
					.thenComparing(GamePage::getDiscountPrice)
			)
			.map(game -> GameEmbeds.discounted(game, now).build())
			.limit(10)  // TODO: send multiple messages if > 10
			.toList();

		// Send notification
		channel.sendMessage(message)
			.setEmbeds(embeds)
			.queue();

	}

}
