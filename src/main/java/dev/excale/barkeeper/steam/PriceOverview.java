package dev.excale.barkeeper.steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PriceOverview {

	private String currency;

	@JsonProperty("initial")
	private Integer initialPrice;

	@JsonProperty("final")
	private Integer finalPrice;

	@JsonProperty("discount_percent")
	private Integer discountPercent;

	@JsonProperty("initial_formatted")
	private String initialFormatted;

	@JsonProperty("final_formatted")
	private String finalFormatted;

}

