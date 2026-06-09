package dev.excale.barkeeper.steam.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class StoreSearch {

	private Integer total;

	private List<StoreSearchItem> items;

}
