package dev.excale.barkeeper.salesnotif;

import dev.excale.barkeeper.games.GameEmbeds;
import dev.excale.barkeeper.notion.NotionClient;
import dev.excale.barkeeper.notion.NotionProperties;
import dev.excale.barkeeper.notion.model.GamePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumPost;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Log4j2
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
			TextChannel channel = Optional.of(settings.getGuildId())
				.map(jda::getGuildById)
				.map(guild -> guild.getTextChannelById(settings.getChannelId()))
				.orElse(null);

			ForumChannel forum = Optional.of(settings.getGuildId())
				.map(jda::getGuildById)
				.map(guild -> guild.getForumChannelById(settings.getChannelId()))
				.orElse(null);

			if(channel != null)
				sendSalesNotificationGuild(channel, games);
			else if(forum != null)
				sendSalesNotificationUsingThread(forum, games);

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

	public void sendSalesNotificationUsingThread(ForumChannel forum) {
		sendSalesNotificationUsingThread(forum, null);
	}

	public void sendSalesNotificationUsingThread(ForumChannel forum, Collection<GamePage> games) {

		// Find games if not provided
		if(games == null)
			games = getGamesOnSale();

		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

		String formattedDay = formatter.withZone(ZoneId.systemDefault()).format(now);
		String message = "There are " + games.size() + " games on sale!";

		// Create thread
		ThreadChannel thread = forum.createForumPost(formattedDay, MessageCreateData.fromContent(message))
			.map(ForumPost::getThreadChannel)
			.complete();

		// Execute on all games
		games.stream()
			.sorted(

				// First sort by most saved amount, then by lowest discount price
				Comparator.comparing((GamePage g) -> g.getFullPrice() - g.getDiscountPrice())
					.reversed()
					.thenComparing(GamePage::getDiscountPrice)

			)
			.map(

				// Build embed for each game
				game -> GameEmbeds.discounted(game, now).build()

			)
			.reduce(

				// Identity (Start)
				CompletableFuture.completedFuture((Void) null),

				// Accumulator (Reduce into future chains)
				(chain, embed) -> chain.thenCompose(
					v -> thread.sendMessageEmbeds(embed)
						.submit()
						.thenAccept(msg -> {})
				),

				// Combiner (mandatory for parallel streams, not used)
				(chain1, chain2) -> chain1.thenCompose(v -> chain2)

			).whenComplete((res, err) -> {

				if(err == null)
					log.info(
						"Sales notification sent to thread {} in forum {}",
						thread.getId(), forum.getId()
					);
				else
					log.error(
						"Error sending sales notification to thread {} in forum {}",
						thread.getId(), forum.getId(), err
					);

			});


	}

}
