package io.github.musicintegration.hud;

import java.util.Locale;

public enum MediaSource {
	SPOTIFY,
	YANDEX,
	BROWSER,
	OTHER;

	public static MediaSource fromAppUserModelId(String id) {
		if (id == null || id.isEmpty()) {
			return OTHER;
		}
		String l = id.toLowerCase(Locale.ROOT);
		if (l.contains("spotify")) {
			return SPOTIFY;
		}
		if (l.contains("yandex")) {
			return YANDEX;
		}
		if (l.contains("chrome") || l.contains("chromium") || l.contains("msedge") || l.contains("firefox")
			|| l.contains("opera") || l.contains("brave") || l.contains("vivaldi") || l.contains("browser")
			|| l.contains("mozilla")) {
			return BROWSER;
		}
		return OTHER;
	}

	public int argbIconColor() {
		return switch (this) {
			case SPOTIFY -> 0xFF1DB954;
			case YANDEX -> 0xFFFC3F1D;
			case BROWSER -> 0xFF4A90D9;
			case OTHER -> 0xFF9E9E9E;
		};
	}
}
