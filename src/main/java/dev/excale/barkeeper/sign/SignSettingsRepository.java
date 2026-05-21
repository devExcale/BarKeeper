package dev.excale.barkeeper.sign;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignSettingsRepository extends JpaRepository<SignSettings, Long> {

    Optional<SignSettings> findByGuildId(Long guildId);
}
