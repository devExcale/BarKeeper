package dev.excale.barkeeper.steam;

import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SteamUtil {

	public static final Pattern REGEX_STEAM_STORE_URL = Pattern.compile(
		"(?:https?://)?store\\.steampowered\\.com/app/(\\d+)/?.*"
	);

}
