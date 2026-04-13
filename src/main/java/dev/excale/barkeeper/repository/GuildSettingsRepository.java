package dev.excale.barkeeper.repository;

import java.util.Optional;

import dev.excale.barkeeper.entity.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildSettingsRepository extends JpaRepository<GuildSettings, Long> {

    Optional<GuildSettings> findByGuildId(Long guildId);
}

