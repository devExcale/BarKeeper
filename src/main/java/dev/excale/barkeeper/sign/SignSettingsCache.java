package dev.excale.barkeeper.sign;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SignSettingsCache {

	private final Map<Long, SignSettings> cache;

	public SignSettingsCache(
		@Value("${discord.cache-size:20}") int maxEntries
	) {

		this.cache = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, SignSettings> eldest) {
				return size() > maxEntries;
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
