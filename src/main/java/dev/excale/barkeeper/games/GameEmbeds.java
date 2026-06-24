package dev.excale.barkeeper.games;

import dev.excale.barkeeper.notion.model.GamePage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GameEmbeds {

	public static EmbedBuilder discounted(GamePage game, Instant now) {

		return new EmbedBuilder()
			.setTitle(String.join("", game.getTitle()))
			.setUrl(game.getStorePage())
			.setImage(game.getCover())
			.setFooter("Steam")
			.setTimestamp(now)
			.addField("Discount Price", "€" + game.getDiscountPrice(), true)
			.addField("Full Price", "€" + game.getFullPrice(), true)
			.addField("Percent Off", game.getDiscountPercent() + "%", true)
			.addField("Genres", String.join(", ", game.getGenres()), false);

	}

}
