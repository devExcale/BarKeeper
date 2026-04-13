package dev.excale.barkeeper.service;

import java.util.Optional;

import dev.excale.barkeeper.component.GuildSettingsCache;
import dev.excale.barkeeper.entity.GuildSettings;
import dev.excale.barkeeper.repository.GuildSettingsRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GuildSettingsService {

	private final GuildSettingsRepository repository;
	private final GuildSettingsCache cache;

	public GuildSettingsService(GuildSettingsRepository repository, GuildSettingsCache cache) {
		this.repository = repository;
		this.cache = cache;
	}

	// Used by voice/startup sign checks: read-through cache with DB fallback.
	public Optional<GuildSettings> getForSignChange(long guildId) {

		Optional<GuildSettings> cached = cache.get(guildId);
		if(cached.isPresent())
			return cached;

		Optional<GuildSettings> fromDb = repository.findByGuildId(guildId);
		fromDb.ifPresent(cache::put);

		return fromDb;
	}

	// Used by commands: bypass cache for write, then refresh cache from DB.
	@Transactional
	public Optional<GuildSettings> setManagedChannel(long guildId, long channelId) {

		GuildSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));

		config.setChannelId(channelId);
		repository.save(config);

		return refreshCacheFromDatabase(guildId);
	}

	// Used by commands: bypass cache for write, then refresh cache from DB.
	@Transactional
	public Optional<GuildSettings> setOpenSign(long guildId, String signOpen) {
		GuildSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));
		config.setSignOpen(signOpen);
		repository.save(config);
		return refreshCacheFromDatabase(guildId);
	}

	// Used by commands: bypass cache for write, then refresh cache from DB.
	@Transactional
	public Optional<GuildSettings> setClosedSign(long guildId, String signClosed) {
		GuildSettings config = repository.findByGuildId(guildId)
			.orElseGet(() -> defaultConfig(guildId));
		config.setSignClosed(signClosed);
		repository.save(config);
		return refreshCacheFromDatabase(guildId);
	}

	private Optional<GuildSettings> refreshCacheFromDatabase(long guildId) {
		Optional<GuildSettings> fresh = repository.findByGuildId(guildId);
		if(fresh.isPresent()) {
			cache.put(fresh.get());
		} else {
			cache.remove(guildId);
		}
		return fresh;
	}

	private GuildSettings defaultConfig(long guildId) {
		return GuildSettings.builder()
			.guildId(guildId)
			.signOpen("open")
			.signClosed("closed")
			.build();
	}

}

