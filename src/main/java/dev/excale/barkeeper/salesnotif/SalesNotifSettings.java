package dev.excale.barkeeper.salesnotif;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

// JPA
@Entity
@Table(name = "sales_notifier")
public class SalesNotifSettings {

	@Id
	@Column(name = "guild_id", nullable = false)
	private Long guildId;

	@Column(name = "channel_id", nullable = false)
	private Long channelId;

	@Column(name = "enabled", nullable = false)
	private Boolean enabled;

}
