package dev.excale.barkeeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "guild_settings")
public class GuildSettings {

	@Id
	@Column(name = "guild_id", nullable = false)
	private Long guildId;

	@Column(name = "channel_id")
	private Long channelId;

	@Column(name = "sign_open", nullable = false, length = 100)
	private String signOpen;

	@Column(name = "sign_closed", nullable = false, length = 100)
	private String signClosed;

}

