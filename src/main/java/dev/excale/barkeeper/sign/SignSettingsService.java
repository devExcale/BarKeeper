package dev.excale.barkeeper.sign;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SignSettingsService {

	private final SignSettingsRepository repository;
	private final SignSettingsCache cache;

	/**
	 * Get the sign settings for a guild, if it exists.
	 * Uses a read-through cache with DB fallback.
	 *
	 * @param guildId the ID of the guild to get the sign settings for
	 * @return an Optional containing the sign settings if it exists, or an empty Optional if does not
	 */
	public Optional<SignSettings> getForSignChange(long guildId) {

		Optional<SignSettings> cached = cache.get(guildId);
		if(cached.isPresent())
			return cached;

		Optional<SignSettings> fromDb = repository.findByGuildId(guildId);
		fromDb.ifPresent(cache::put);

		return fromDb;
	}

	/**
	 * Set the managed channel for a guild.
	 * <p>
	 * If the guild does not have sign settings, creates default settings and then sets the channel.
	 * Bypasses the cache for write, then refreshes the cache from the database.
	 *
	 * @param guildId the ID of the guild to set the managed channel for
	 * @param channelId the ID of the channel to set as the managed channel
	 * @return an Optional containing the updated sign settings if the guild exists,
	 * 	       or an empty Optional if the guild does not exist
	 */
	@Transactional
	public Optional<SignSettings> setManagedChannel(long guildId, long channelId) {

		SignSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));

		config.setChannelId(channelId);
		repository.save(config);

		return refreshCacheFromDatabase(guildId);
	}

	/**
	 * Set the open sign label for a guild.
	 * <p>
	 * If the guild does not have sign settings, creates default settings and then sets the open sign label.
	 * Bypasses the cache for write, then refreshes the cache from the database.
	 *
	 * @param guildId the ID of the guild to set the open sign label for
	 * @param signOpen the label to set for the open sign
	 * @return an Optional containing the updated sign settings if the guild exists,
	 *         or an empty Optional if the guild does not exist
	 */
	@Transactional
	public Optional<SignSettings> setOpenSign(long guildId, String signOpen) {

		SignSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));

		config.setLabelOpen(signOpen);
		repository.save(config);

		return refreshCacheFromDatabase(guildId);
	}

	/**
	 * Set the closed sign label for a guild.
	 * <p>
	 * If the guild does not have sign settings, creates default settings and then sets the closed sign label.
	 * Bypasses the cache for write, then refreshes the cache from the database.
	 *
	 * @param guildId the ID of the guild to set the closed sign label for
	 * @param signClosed the label to set for the closed sign
	 * @return an Optional containing the updated sign settings if the guild exists,
	 *         or an empty Optional if the guild does not exist
	 */
	@Transactional
	public Optional<SignSettings> setClosedSign(long guildId, String signClosed) {

		SignSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));

		config.setLabelClosed(signClosed);
		repository.save(config);

		return refreshCacheFromDatabase(guildId);
	}

	/**
	 * Refresh the cache for a guild by fetching the latest sign settings from the database.
	 *
	 * @param guildId the ID of the guild to refresh the cache for
	 * @return an Optional containing the latest sign settings if they exist, or an empty Optional if they do not
	 */
	private Optional<SignSettings> refreshCacheFromDatabase(long guildId) {

		Optional<SignSettings> fresh = repository.findByGuildId(guildId);

		if(fresh.isPresent())
			cache.put(fresh.get());
		else
			cache.remove(guildId);

		return fresh;
	}

	/**
	 * Create default sign settings for a guild.
	 *
	 * @param guildId the ID of the guild to create default sign settings for
	 * @return a SignSettings object with default values for the specified guild
	 */
	private SignSettings defaultConfig(long guildId) {
		return SignSettings.builder()
			.guildId(guildId)
			.labelOpen("open")
			.labelClosed("closed")
			.build();
	}

}
