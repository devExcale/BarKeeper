package dev.excale.barkeeper.component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import dev.excale.barkeeper.entity.GuildSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GuildSettingsCache {

	private final Map<Long, GuildSettings> cache;

	public GuildSettingsCache(
		@Value("${discord.cache-size:20}") int maxEntries
	) {

		this.cache = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, GuildSettings> eldest) {
				return size() > maxEntries;
			}
		};

	}

	public synchronized Optional<GuildSettings> get(long guildId) {
		return Optional.ofNullable(cache.get(guildId));
	}

	public synchronized void put(GuildSettings guildSettings) {
		cache.put(guildSettings.getGuildId(), guildSettings);
	}

	public synchronized void remove(long guildId) {
		cache.remove(guildId);
	}

}



