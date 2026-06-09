package dev.excale.barkeeper.steam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreSearchItem {

	private String type;

	private String name;

	private Integer id;

	@JsonProperty("tiny_image")
	private String tinyImage;

}
