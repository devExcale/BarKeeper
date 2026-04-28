package dev.excale.barkeeper.util;

import java.util.regex.Pattern;

public class SteamUtil {

	public static final Pattern REGEX_STEAM_STORE_URL = Pattern.compile(
		"(?:https?://)?store\\.steampowered\\.com/app/(\\d+)/?.*"
	);

}
