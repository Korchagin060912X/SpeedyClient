package io.github.musicintegration;

import io.github.musicintegration.config.ModConfig;
import io.github.musicintegration.gsmtc.GsmtcService;
import io.github.musicintegration.hud.MusicHud;
import io.github.musicintegration.platform.WindowsSupport;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MusicIntegrationClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(MusicIntegration.MOD_ID);

	@Override
	public void onInitializeClient() {
		ModConfig.load();

		if (WindowsSupport.isWindows()) {
			GsmtcService.start();
			if (!WindowsSupport.isGsmtcSupportedOs()) {
				LOGGER.warn("Music Integration: GSMTC works best on Windows 10/11.");
			}
		}

		// Привязка к CHAT наследует условия под-слоя чата и может не вызываться в игре — ставим после BOSS_BAR.
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.BOSS_BAR,
			ResourceLocation.fromNamespaceAndPath(MusicIntegration.MOD_ID, "music_hud"),
			MusicHud::render
		);

		ClientTickEvents.END_CLIENT_TICK.register(MusicHud::tick);
		MusicHud.registerScreenClicks();
	}
}
