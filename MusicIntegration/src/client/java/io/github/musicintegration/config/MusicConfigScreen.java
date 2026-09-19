package io.github.musicintegration.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MusicConfigScreen {
	private MusicConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ModConfig cfg = ModConfig.get();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.literal("Music Integration"));

		ConfigEntryBuilder eb = ConfigEntryBuilder.create();
		ConfigCategory hud = builder.getOrCreateCategory(Component.literal("HUD"));

		hud.addEntry(eb.startIntField(Component.literal("Vertical offset (px)"), cfg.hudYOffset)
			.setDefaultValue(6)
			.setMin(0)
			.setMax(240)
			.setSaveConsumer(v -> cfg.hudYOffset = v)
			.build());

		hud.addEntry(eb.startIntField(Component.literal("Max title width (px)"), cfg.maxTitleWidth)
			.setDefaultValue(200)
			.setMin(80)
			.setMax(400)
			.setSaveConsumer(v -> cfg.maxTitleWidth = v)
			.build());

		hud.addEntry(eb.startBooleanToggle(Component.literal("Show skip buttons (full HUD)"), cfg.showSkipButtons)
			.setDefaultValue(true)
			.setSaveConsumer(v -> cfg.showSkipButtons = v)
			.build());

		builder.setSavingRunnable(ModConfig::save);
		return builder.build();
	}
}
