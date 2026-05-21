package dev.excale.barkeeper.sign;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import dev.excale.barkeeper.discord.DiscordProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SignSettingsCache {

	private final Map<Long, SignSettings> cache;

	public SignSettingsCache(
		DiscordProperties props
	) {

		log.info("Initializing SignSettingsCache with cache size: {}", props.cacheSize());

		this.cache = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, SignSettings> eldest) {
				return size() > props.cacheSize();
			}
		};

	}

	public synchronized Optional<SignSettings> get(long guildId) {
		return Optional.ofNullable(cache.get(guildId));
	}

	public synchronized void put(SignSettings signSettings) {
		cache.put(signSettings.getGuildId(), signSettings);
	}

	public synchronized void remove(long guildId) {
		cache.remove(guildId);
	}

}
