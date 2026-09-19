package com.shampoon.speedysclient.ui;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.config.SpeedysWatermarkConfig;
import com.shampoon.speedysclient.features.MiniHudNotifications;
import com.shampoon.pvputils.features.SoundController;
import com.shampoon.pvputils.features.Zoom;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import me.shampoon.attackindicator.ModConfig;
import me.shampoon.cooldownitem.CooldownItemConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.artem.durabilitytint.DurabilityTintConfig;

/**
 * Экран настроек как в собранном speedysclient из jar: одна колонка, скролл, те же цвета панели и «свитчей».
 * Дополнительно к jar: Predict idle, цвета FT, снежок 5×5 в FT Helper. Вкл/выкл Landing, FT, Custom Hand — в главном меню.
 */
public final class SpeedysSettingsPopupScreen extends Screen {
    /** Как в твоём моде из jar ({@code drawRoundedRect} / main menu). */
    private static final int BG = -805306368;
    private static final int OUTLINE = -14996918;
    private static final int SWITCH_ON = -9545473;
    private static final int SWITCH_OFF = -11709847;
    private static final int SWITCH_KNOB = -1184001;

    private final Screen parent;
    private int scroll;

    public SpeedysSettingsPopupScreen(Screen parent) {
        super(Text.literal("Настройки функций"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();
        int cx = this.width / 2;
        int y = this.height / 2 - 150 + 18 - this.scroll;
        ModConfig ai = ModConfig.getConfig();
        ai.ensureCrosshairRgbFromEnum();

        if (this.visible(y)) {
            ButtonWidget hdrWm = ButtonWidget.builder(Text.literal("— Watermark —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrWm.active = false;
            this.addDrawableChild(hdrWm);
        }
        y += 19;
        if (this.visible(y)) {
            String wm = SpeedysWatermarkConfig.get().visible ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Watermark: " + wm), b -> {
                SpeedysWatermarkConfig.get().visible = !SpeedysWatermarkConfig.get().visible;
                SpeedysWatermarkConfig.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Armor HUD"), b -> {
                PVPUtils.CONFIG.armorHudEnabled = !PVPUtils.CONFIG.armorHudEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Effect Timer"), b -> {
                PVPUtils.CONFIG.effectTimerEnabled = !PVPUtils.CONFIG.effectTimerEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            String mode = PVPUtils.CONFIG.effectTimerListPanel ? "\u043f\u0430\u043d\u0435\u043b\u044c \u0441\u043f\u0438\u0441\u043a\u0430" : "\u0443 \u0438\u043a\u043e\u043d\u043e\u043a HUD";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("\u0422\u0430\u0439\u043c\u0435\u0440 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432: " + mode), b -> {
                PVPUtils.CONFIG.effectTimerListPanel = !PVPUtils.CONFIG.effectTimerListPanel;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(attackIndicatorSlider(cx - 110, y, ai));
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Цвет: Красный"), b -> {
                ai.applyCrosshairPreset(ModConfig.CrosshairColor.RED);
                ai.save();
                this.init();
            }).dimensions(cx - 110, y, 70, 16).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Зелёный"), b -> {
                ai.applyCrosshairPreset(ModConfig.CrosshairColor.GREEN);
                ai.save();
                this.init();
            }).dimensions(cx - 35, y, 70, 16).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Жёлтый"), b -> {
                ai.applyCrosshairPreset(ModConfig.CrosshairColor.YELLOW);
                ai.save();
                this.init();
            }).dimensions(cx + 40, y, 70, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Прицел R", ai.crosshairRgbR, v -> {
            ai.crosshairRgbR = v;
            ai.save();
        });
        y = this.addRgbSlider(cx, y, "Прицел G", ai.crosshairRgbG, v -> {
            ai.crosshairRgbG = v;
            ai.save();
        });
        y = this.addRgbSlider(cx, y, "Прицел B", ai.crosshairRgbB, v -> {
            ai.crosshairRgbB = v;
            ai.save();
        });
        if (this.visible(y)) {
            ButtonWidget hdrCd = ButtonWidget.builder(Text.literal("— Cooldown Items —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrCd.active = false;
            this.addDrawableChild(hdrCd);
        }
        y += 19;
        if (this.visible(y)) {
            CooldownItemConfig cd = CooldownItemConfig.get();
            String cdMode = cd.displayMode == CooldownItemConfig.DISPLAY_HUD_LIST ? "HUD список" : "Секунды на слотах";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Режим: " + cdMode), b -> {
                cd.displayMode = cd.displayMode == CooldownItemConfig.DISPLAY_SECONDS
                        ? CooldownItemConfig.DISPLAY_HUD_LIST
                        : CooldownItemConfig.DISPLAY_SECONDS;
                CooldownItemConfig.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(durabilityStrengthSlider(cx - 110, y));
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(zoomSlider(cx - 110, y));
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(autoPotionDelaySlider(cx - 110, y));
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrEh = ButtonWidget.builder(Text.literal("— Eat Helper —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrEh.active = false;
            this.addDrawableChild(hdrEh);
        }
        y += 19;
        if (this.visible(y)) {
            String eh = PVPUtils.CONFIG.eatHelperEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Eat Helper: " + eh), b -> {
                PVPUtils.CONFIG.eatHelperEnabled = !PVPUtils.CONFIG.eatHelperEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Eat Helper R", PVPUtils.CONFIG.eatHelperR, v -> PVPUtils.CONFIG.eatHelperR = v);
        y = this.addRgbSlider(cx, y, "Eat Helper G", PVPUtils.CONFIG.eatHelperG, v -> PVPUtils.CONFIG.eatHelperG = v);
        y = this.addRgbSlider(cx, y, "Eat Helper B", PVPUtils.CONFIG.eatHelperB, v -> PVPUtils.CONFIG.eatHelperB = v);
        if (this.visible(y)) {
            ButtonWidget hdrPh = ButtonWidget.builder(Text.literal("— Potion Highlighter —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrPh.active = false;
            this.addDrawableChild(hdrPh);
        }
        y += 19;
        if (this.visible(y)) {
            String ph = PVPUtils.CONFIG.potionHighlighterEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Potion Highlighter: " + ph), b -> {
                PVPUtils.CONFIG.potionHighlighterEnabled = !PVPUtils.CONFIG.potionHighlighterEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "PH «плохо» R", PVPUtils.CONFIG.potionHlBadR, v -> PVPUtils.CONFIG.potionHlBadR = v);
        y = this.addRgbSlider(cx, y, "PH «плохо» G", PVPUtils.CONFIG.potionHlBadG, v -> PVPUtils.CONFIG.potionHlBadG = v);
        y = this.addRgbSlider(cx, y, "PH «плохо» B", PVPUtils.CONFIG.potionHlBadB, v -> PVPUtils.CONFIG.potionHlBadB = v);
        y = this.addRgbSlider(cx, y, "PH «микс» R", PVPUtils.CONFIG.potionHlMixR, v -> PVPUtils.CONFIG.potionHlMixR = v);
        y = this.addRgbSlider(cx, y, "PH «микс» G", PVPUtils.CONFIG.potionHlMixG, v -> PVPUtils.CONFIG.potionHlMixG = v);
        y = this.addRgbSlider(cx, y, "PH «микс» B", PVPUtils.CONFIG.potionHlMixB, v -> PVPUtils.CONFIG.potionHlMixB = v);
        y = this.addRgbSlider(cx, y, "PH «хорошо» R", PVPUtils.CONFIG.potionHlGoodR, v -> PVPUtils.CONFIG.potionHlGoodR = v);
        y = this.addRgbSlider(cx, y, "PH «хорошо» G", PVPUtils.CONFIG.potionHlGoodG, v -> PVPUtils.CONFIG.potionHlGoodG = v);
        y = this.addRgbSlider(cx, y, "PH «хорошо» B", PVPUtils.CONFIG.potionHlGoodB, v -> PVPUtils.CONFIG.potionHlGoodB = v);
        if (this.visible(y)) {
            ButtonWidget hdrTh = ButtonWidget.builder(Text.literal("— Target HUD —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrTh.active = false;
            this.addDrawableChild(hdrTh);
        }
        y += 19;
        if (this.visible(y)) {
            String offLabel = PVPUtils.CONFIG.targetHudShowOffhand ? "\u0412\u043a\u043b" : "\u0412\u044b\u043a\u043b";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430 (\u0442\u0435\u043a\u0441\u0442 \u0432\u043d\u0438\u0437\u0443): " + offLabel), b -> {
                PVPUtils.CONFIG.targetHudShowOffhand = !PVPUtils.CONFIG.targetHudShowOffhand;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrCr = ButtonWidget.builder(Text.literal("— Custom Ratio —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrCr.active = false;
            this.addDrawableChild(hdrCr);
        }
        y += 19;
        if (this.visible(y)) {
            String cr = PVPUtils.CONFIG.customRatioEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Custom Ratio: " + cr), b -> {
                PVPUtils.CONFIG.customRatioEnabled = !PVPUtils.CONFIG.customRatioEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            TextFieldWidget ratioField = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("CustomRatio"));
            ratioField.setMaxLength(16);
            ratioField.setPlaceholder(Text.literal("Формат: 4:3 ... 16:9"));
            ratioField.setText(PVPUtils.CONFIG.customRatioValue == null ? "16:9" : PVPUtils.CONFIG.customRatioValue);
            ratioField.setChangedListener(s -> {
                PVPUtils.CONFIG.customRatioValue = s;
                PVPUtils.CONFIG.save();
            });
            this.addDrawableChild(ratioField);
        }
        y += 21;
        if (this.visible(y)) {
            ButtonWidget hdrCw = ButtonWidget.builder(Text.literal("— Custom World —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrCw.active = false;
            this.addDrawableChild(hdrCw);
        }
        y += 19;
        if (this.visible(y)) {
            String cw = PVPUtils.CONFIG.customWorldEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Custom World: " + cw), b -> {
                PVPUtils.CONFIG.customWorldEnabled = !PVPUtils.CONFIG.customWorldEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Custom World Sky R", PVPUtils.CONFIG.customWorldSkyR, v -> PVPUtils.CONFIG.customWorldSkyR = v);
        y = this.addRgbSlider(cx, y, "Custom World Sky G", PVPUtils.CONFIG.customWorldSkyG, v -> PVPUtils.CONFIG.customWorldSkyG = v);
        y = this.addRgbSlider(cx, y, "Custom World Sky B", PVPUtils.CONFIG.customWorldSkyB, v -> PVPUtils.CONFIG.customWorldSkyB = v);
        if (this.visible(y)) {
            String sky = PVPUtils.CONFIG.customWorldSkyType == 0 ? "Облака (ванила)" : "Кометы";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Кастомное небо: " + sky), b -> {
                PVPUtils.CONFIG.customWorldSkyType = (PVPUtils.CONFIG.customWorldSkyType + 1) % 2;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y) && PVPUtils.CONFIG.customWorldSkyType == 1) {
            y = this.addIntSlider(cx, y, "Комет в минуту", PVPUtils.CONFIG.customWorldCometsPerMinute, 1, 10, v -> PVPUtils.CONFIG.customWorldCometsPerMinute = v);
            y = this.addIntSlider(cx, y, "Полёт кометы (сек), затем 3с затух.", PVPUtils.CONFIG.customWorldCometFlightSeconds, 6, 30, v -> PVPUtils.CONFIG.customWorldCometFlightSeconds = v);
            y = this.addRgbSlider(cx, y, "Цвет комет R", PVPUtils.CONFIG.customWorldCometR, v -> PVPUtils.CONFIG.customWorldCometR = v);
            y = this.addRgbSlider(cx, y, "Цвет комет G", PVPUtils.CONFIG.customWorldCometG, v -> PVPUtils.CONFIG.customWorldCometG = v);
            y = this.addRgbSlider(cx, y, "Цвет комет B", PVPUtils.CONFIG.customWorldCometB, v -> PVPUtils.CONFIG.customWorldCometB = v);
        }
        if (this.visible(y)) {
            String fog = PVPUtils.CONFIG.customWorldFogEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Custom World Fog: " + fog), b -> {
                PVPUtils.CONFIG.customWorldFogEnabled = !PVPUtils.CONFIG.customWorldFogEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addIntSlider(cx, y, "Fog distance (blocks)", PVPUtils.CONFIG.customWorldFogDistanceBlocks, 10, 100, v -> PVPUtils.CONFIG.customWorldFogDistanceBlocks = v);
        y = this.addRgbSlider(cx, y, "Custom World Fog R", PVPUtils.CONFIG.customWorldFogR, v -> PVPUtils.CONFIG.customWorldFogR = v);
        y = this.addRgbSlider(cx, y, "Custom World Fog G", PVPUtils.CONFIG.customWorldFogG, v -> PVPUtils.CONFIG.customWorldFogG = v);
        y = this.addRgbSlider(cx, y, "Custom World Fog B", PVPUtils.CONFIG.customWorldFogB, v -> PVPUtils.CONFIG.customWorldFogB = v);
        if (this.visible(y)) {
            ButtonWidget hdrWp = ButtonWidget.builder(Text.literal("— World Particles —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrWp.active = false;
            this.addDrawableChild(hdrWp);
        }
        y += 19;
        if (this.visible(y)) {
            String type = switch (PVPUtils.CONFIG.worldParticlesType) {
                case 1 -> "❆ Снежинка";
                case 2 -> "$ Доллар";
                default -> "★ Звезда";
            };
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Тип: " + type), b -> {
                PVPUtils.CONFIG.worldParticlesType = (PVPUtils.CONFIG.worldParticlesType + 1) % 3;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            String mode = PVPUtils.CONFIG.worldParticlesFalling ? "Падают" : "Парят";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Режим: " + mode), b -> {
                PVPUtils.CONFIG.worldParticlesFalling = !PVPUtils.CONFIG.worldParticlesFalling;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            TextFieldWidget chunksField = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("WpChunks"));
            chunksField.setMaxLength(1);
            chunksField.setPlaceholder(Text.literal("Чанки зоны: 1..4"));
            chunksField.setText(String.valueOf(PVPUtils.CONFIG.worldParticlesChunks));
            chunksField.setChangedListener(s -> {
                try {
                    int n = Integer.parseInt(s.trim());
                    PVPUtils.CONFIG.worldParticlesChunks = Math.max(1, Math.min(4, n));
                    PVPUtils.CONFIG.save();
                } catch (NumberFormatException ignored) {
                }
            });
            this.addDrawableChild(chunksField);
        }
        y += 21;
        y = this.addFloatSlider(cx, y, "Скорость падения", PVPUtils.CONFIG.worldParticlesFallSpeed, 0.1f, 3.0f, v -> PVPUtils.CONFIG.worldParticlesFallSpeed = v);
        y = this.addFloatSlider(cx, y, "Время жизни (сек)", PVPUtils.CONFIG.worldParticlesLifetimeSeconds, 1.0f, 10.0f, v -> PVPUtils.CONFIG.worldParticlesLifetimeSeconds = v);
        y = this.addRgbSlider(cx, y, "World Particles R", PVPUtils.CONFIG.worldParticlesR, v -> PVPUtils.CONFIG.worldParticlesR = v);
        y = this.addRgbSlider(cx, y, "World Particles G", PVPUtils.CONFIG.worldParticlesG, v -> PVPUtils.CONFIG.worldParticlesG = v);
        y = this.addRgbSlider(cx, y, "World Particles B", PVPUtils.CONFIG.worldParticlesB, v -> PVPUtils.CONFIG.worldParticlesB = v);
        y = this.addSoundSlider(cx, y, "Звук: опыт", PVPUtils.CONFIG.soundControllerExpOrbVolume, v -> {
            PVPUtils.CONFIG.soundControllerExpOrbVolume = v;
            PVPUtils.CONFIG.save();
        });
        y = this.addSoundSlider(cx, y, "Звук: иссушитель", PVPUtils.CONFIG.soundControllerWitherVolume, v -> {
            PVPUtils.CONFIG.soundControllerWitherVolume = v;
            PVPUtils.CONFIG.save();
        });
        y = this.addSoundSlider(cx, y, "Звук: трезубец", PVPUtils.CONFIG.soundControllerTridentVolume, v -> {
            PVPUtils.CONFIG.soundControllerTridentVolume = v;
            PVPUtils.CONFIG.save();
        });
        if (this.visible(y)) {
            this.addDrawableChild(fullBrightOnSlider(cx - 110, y));
        }
        y += 19;
        if (this.visible(y)) {
            this.addDrawableChild(fullBrightOffSlider(cx - 110, y));
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Trail R", PVPUtils.CONFIG.trailR, v -> PVPUtils.CONFIG.trailR = v);
        y = this.addRgbSlider(cx, y, "Trail G", PVPUtils.CONFIG.trailG, v -> PVPUtils.CONFIG.trailG = v);
        y = this.addRgbSlider(cx, y, "Trail B", PVPUtils.CONFIG.trailB, v -> PVPUtils.CONFIG.trailB = v);
        y = this.addGradientColorStopsRow(cx, y, "Trail", PVPUtils.CONFIG.trailColorStops, () -> {
            if (PVPUtils.CONFIG.trailColorStops < 3) {
                PVPUtils.CONFIG.trailColorStops++;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        }, () -> {
            if (PVPUtils.CONFIG.trailColorStops > 1) {
                PVPUtils.CONFIG.trailColorStops--;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        });
        if (PVPUtils.CONFIG.trailColorStops >= 2) {
            y = this.addRgbSlider(cx, y, "Trail 2 R", PVPUtils.CONFIG.trailR2, v -> PVPUtils.CONFIG.trailR2 = v);
            y = this.addRgbSlider(cx, y, "Trail 2 G", PVPUtils.CONFIG.trailG2, v -> PVPUtils.CONFIG.trailG2 = v);
            y = this.addRgbSlider(cx, y, "Trail 2 B", PVPUtils.CONFIG.trailB2, v -> PVPUtils.CONFIG.trailB2 = v);
        }
        if (PVPUtils.CONFIG.trailColorStops >= 3) {
            y = this.addRgbSlider(cx, y, "Trail 3 R", PVPUtils.CONFIG.trailR3, v -> PVPUtils.CONFIG.trailR3 = v);
            y = this.addRgbSlider(cx, y, "Trail 3 G", PVPUtils.CONFIG.trailG3, v -> PVPUtils.CONFIG.trailG3 = v);
            y = this.addRgbSlider(cx, y, "Trail 3 B", PVPUtils.CONFIG.trailB3, v -> PVPUtils.CONFIG.trailB3 = v);
        }
        y = this.addGradientAnimToggleRow(cx, y, "Trail", PVPUtils.CONFIG.trailGradientAnim, () -> {
            PVPUtils.CONFIG.trailGradientAnim = !PVPUtils.CONFIG.trailGradientAnim;
            PVPUtils.CONFIG.save();
            this.init();
        });
        y = this.addRgbSlider(cx, y, "China Hat R", PVPUtils.CONFIG.chinaHatR, v -> PVPUtils.CONFIG.chinaHatR = v);
        y = this.addRgbSlider(cx, y, "China Hat G", PVPUtils.CONFIG.chinaHatG, v -> PVPUtils.CONFIG.chinaHatG = v);
        y = this.addRgbSlider(cx, y, "China Hat B", PVPUtils.CONFIG.chinaHatB, v -> PVPUtils.CONFIG.chinaHatB = v);
        y = this.addGradientColorStopsRow(cx, y, "China Hat", PVPUtils.CONFIG.chinaHatColorStops, () -> {
            if (PVPUtils.CONFIG.chinaHatColorStops < 3) {
                PVPUtils.CONFIG.chinaHatColorStops++;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        }, () -> {
            if (PVPUtils.CONFIG.chinaHatColorStops > 1) {
                PVPUtils.CONFIG.chinaHatColorStops--;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        });
        if (PVPUtils.CONFIG.chinaHatColorStops >= 2) {
            y = this.addRgbSlider(cx, y, "China Hat 2 R", PVPUtils.CONFIG.chinaHatR2, v -> PVPUtils.CONFIG.chinaHatR2 = v);
            y = this.addRgbSlider(cx, y, "China Hat 2 G", PVPUtils.CONFIG.chinaHatG2, v -> PVPUtils.CONFIG.chinaHatG2 = v);
            y = this.addRgbSlider(cx, y, "China Hat 2 B", PVPUtils.CONFIG.chinaHatB2, v -> PVPUtils.CONFIG.chinaHatB2 = v);
        }
        if (PVPUtils.CONFIG.chinaHatColorStops >= 3) {
            y = this.addRgbSlider(cx, y, "China Hat 3 R", PVPUtils.CONFIG.chinaHatR3, v -> PVPUtils.CONFIG.chinaHatR3 = v);
            y = this.addRgbSlider(cx, y, "China Hat 3 G", PVPUtils.CONFIG.chinaHatG3, v -> PVPUtils.CONFIG.chinaHatG3 = v);
            y = this.addRgbSlider(cx, y, "China Hat 3 B", PVPUtils.CONFIG.chinaHatB3, v -> PVPUtils.CONFIG.chinaHatB3 = v);
        }
        y = this.addGradientAnimToggleRow(cx, y, "China Hat", PVPUtils.CONFIG.chinaHatGradientAnim, () -> {
            PVPUtils.CONFIG.chinaHatGradientAnim = !PVPUtils.CONFIG.chinaHatGradientAnim;
            PVPUtils.CONFIG.save();
            this.init();
        });
        y = this.addRgbSlider(cx, y, "Jump Circle R", PVPUtils.CONFIG.jumpCircleR, v -> PVPUtils.CONFIG.jumpCircleR = v);
        y = this.addRgbSlider(cx, y, "Jump Circle G", PVPUtils.CONFIG.jumpCircleG, v -> PVPUtils.CONFIG.jumpCircleG = v);
        if (this.visible(y = this.addRgbSlider(cx, y, "Jump Circle B", PVPUtils.CONFIG.jumpCircleB, v -> PVPUtils.CONFIG.jumpCircleB = v))) {
            String mode = PVPUtils.CONFIG.jumpCircleSpreadByBlocks ? "По блокам" : "Обычный круг";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Jump Circle: " + mode), b -> {
                PVPUtils.CONFIG.jumpCircleSpreadByBlocks = !PVPUtils.CONFIG.jumpCircleSpreadByBlocks;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrBo = ButtonWidget.builder(Text.literal("— Block Overlay —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrBo.active = false;
            this.addDrawableChild(hdrBo);
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Block Overlay R", PVPUtils.CONFIG.blockOverlayR, v -> PVPUtils.CONFIG.blockOverlayR = v);
        y = this.addRgbSlider(cx, y, "Block Overlay G", PVPUtils.CONFIG.blockOverlayG, v -> PVPUtils.CONFIG.blockOverlayG = v);
        y = this.addRgbSlider(cx, y, "Block Overlay B", PVPUtils.CONFIG.blockOverlayB, v -> PVPUtils.CONFIG.blockOverlayB = v);
        y = this.addGradientColorStopsRow(cx, y, "Overlay", PVPUtils.CONFIG.blockOverlayColorStops, () -> {
            if (PVPUtils.CONFIG.blockOverlayColorStops < 3) {
                PVPUtils.CONFIG.blockOverlayColorStops++;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        }, () -> {
            if (PVPUtils.CONFIG.blockOverlayColorStops > 1) {
                PVPUtils.CONFIG.blockOverlayColorStops--;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        });
        if (PVPUtils.CONFIG.blockOverlayColorStops >= 2) {
            y = this.addRgbSlider(cx, y, "Overlay 2 R", PVPUtils.CONFIG.blockOverlayR2, v -> PVPUtils.CONFIG.blockOverlayR2 = v);
            y = this.addRgbSlider(cx, y, "Overlay 2 G", PVPUtils.CONFIG.blockOverlayG2, v -> PVPUtils.CONFIG.blockOverlayG2 = v);
            y = this.addRgbSlider(cx, y, "Overlay 2 B", PVPUtils.CONFIG.blockOverlayB2, v -> PVPUtils.CONFIG.blockOverlayB2 = v);
        }
        if (PVPUtils.CONFIG.blockOverlayColorStops >= 3) {
            y = this.addRgbSlider(cx, y, "Overlay 3 R", PVPUtils.CONFIG.blockOverlayR3, v -> PVPUtils.CONFIG.blockOverlayR3 = v);
            y = this.addRgbSlider(cx, y, "Overlay 3 G", PVPUtils.CONFIG.blockOverlayG3, v -> PVPUtils.CONFIG.blockOverlayG3 = v);
            y = this.addRgbSlider(cx, y, "Overlay 3 B", PVPUtils.CONFIG.blockOverlayB3, v -> PVPUtils.CONFIG.blockOverlayB3 = v);
        }
        y = this.addGradientAnimToggleRow(cx, y, "Overlay", PVPUtils.CONFIG.blockOverlayGradientAnim, () -> {
            PVPUtils.CONFIG.blockOverlayGradientAnim = !PVPUtils.CONFIG.blockOverlayGradientAnim;
            PVPUtils.CONFIG.save();
            this.init();
        });
        if (this.visible(y)) {
            String sidesLabel = PVPUtils.CONFIG.blockOverlaySidesEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Обводка по бокам (рёбра): " + sidesLabel), b -> {
                PVPUtils.CONFIG.blockOverlaySidesEnabled = !PVPUtils.CONFIG.blockOverlaySidesEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Бока R", PVPUtils.CONFIG.blockOverlaySidesR, v -> PVPUtils.CONFIG.blockOverlaySidesR = v);
        y = this.addRgbSlider(cx, y, "Бока G", PVPUtils.CONFIG.blockOverlaySidesG, v -> PVPUtils.CONFIG.blockOverlaySidesG = v);
        y = this.addRgbSlider(cx, y, "Бока B", PVPUtils.CONFIG.blockOverlaySidesB, v -> PVPUtils.CONFIG.blockOverlaySidesB = v);
        y += 19;
        if (this.visible(y)) {
            String bh = PVPUtils.CONFIG.blockHoverSmooth ? "\u0412\u043a\u043b" : "\u0412\u044b\u043a\u043b";
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("\u041f\u043b\u0430\u0432\u043d\u044b\u0439 \u043f\u0440\u0438\u0446\u0435\u043b \u0431\u043b\u043e\u043a\u0438: " + bh), b -> {
                PVPUtils.CONFIG.blockHoverSmooth = !PVPUtils.CONFIG.blockHoverSmooth;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrHit = ButtonWidget.builder(Text.literal("— Hit Color —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrHit.active = false;
            this.addDrawableChild(hdrHit);
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Hit Color R", PVPUtils.CONFIG.hitColorR, v -> PVPUtils.CONFIG.hitColorR = v);
        y = this.addRgbSlider(cx, y, "Hit Color G", PVPUtils.CONFIG.hitColorG, v -> PVPUtils.CONFIG.hitColorG = v);
        y = this.addRgbSlider(cx, y, "Hit Color B", PVPUtils.CONFIG.hitColorB, v -> PVPUtils.CONFIG.hitColorB = v);
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrHb = ButtonWidget.builder(Text.literal("— Custom Hitboxes —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrHb.active = false;
            this.addDrawableChild(hdrHb);
        }
        y += 19;
        if (this.visible(y)) {
            String fillHb = PVPUtils.CONFIG.customHitboxFillEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Заливка хитбокса: " + fillHb), b -> {
                PVPUtils.CONFIG.customHitboxFillEnabled = !PVPUtils.CONFIG.customHitboxFillEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "HB обводка R", PVPUtils.CONFIG.customHitboxBoxR, v -> PVPUtils.CONFIG.customHitboxBoxR = v);
        y = this.addRgbSlider(cx, y, "HB обводка G", PVPUtils.CONFIG.customHitboxBoxG, v -> PVPUtils.CONFIG.customHitboxBoxG = v);
        y = this.addRgbSlider(cx, y, "HB обводка B", PVPUtils.CONFIG.customHitboxBoxB, v -> PVPUtils.CONFIG.customHitboxBoxB = v);
        y = this.addRgbSlider(cx, y, "HB линия взгляда R", PVPUtils.CONFIG.customHitboxEyeR, v -> PVPUtils.CONFIG.customHitboxEyeR = v);
        y = this.addRgbSlider(cx, y, "HB линия взгляда G", PVPUtils.CONFIG.customHitboxEyeG, v -> PVPUtils.CONFIG.customHitboxEyeG = v);
        y = this.addRgbSlider(cx, y, "HB линия взгляда B", PVPUtils.CONFIG.customHitboxEyeB, v -> PVPUtils.CONFIG.customHitboxEyeB = v);
        y = this.addRgbSlider(cx, y, "HB голова R", PVPUtils.CONFIG.customHitboxHeadR, v -> PVPUtils.CONFIG.customHitboxHeadR = v);
        y = this.addRgbSlider(cx, y, "HB голова G", PVPUtils.CONFIG.customHitboxHeadG, v -> PVPUtils.CONFIG.customHitboxHeadG = v);
        y = this.addRgbSlider(cx, y, "HB голова B", PVPUtils.CONFIG.customHitboxHeadB, v -> PVPUtils.CONFIG.customHitboxHeadB = v);
        y = this.addRgbSlider(cx, y, "HB заливка R", PVPUtils.CONFIG.customHitboxFillR, v -> PVPUtils.CONFIG.customHitboxFillR = v);
        y = this.addRgbSlider(cx, y, "HB заливка G", PVPUtils.CONFIG.customHitboxFillG, v -> PVPUtils.CONFIG.customHitboxFillG = v);
        y = this.addRgbSlider(cx, y, "HB заливка B", PVPUtils.CONFIG.customHitboxFillB, v -> PVPUtils.CONFIG.customHitboxFillB = v);
        y += 19;
        y = this.addRgbSlider(cx, y, "Target ESP R", PVPUtils.CONFIG.targetEspR, v -> PVPUtils.CONFIG.targetEspR = v);
        y = this.addRgbSlider(cx, y, "Target ESP G", PVPUtils.CONFIG.targetEspG, v -> PVPUtils.CONFIG.targetEspG = v);
        y = this.addRgbSlider(cx, y, "Target ESP B", PVPUtils.CONFIG.targetEspB, v -> PVPUtils.CONFIG.targetEspB = v);
        if (this.visible(y += 19)) {
            String modeLabel = switch (PVPUtils.CONFIG.targetEspMode) {
                case 0 -> "Круг (качание по высоте)";
                case 2 -> "Души (частицы)";
                default -> "Сферы (орбита)";
            };
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Target ESP тип: " + modeLabel), b -> {
                PVPUtils.CONFIG.targetEspMode = (PVPUtils.CONFIG.targetEspMode + 1) % 3;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdr = ButtonWidget.builder(Text.literal("— Predict / FT —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdr.active = false;
            this.addDrawableChild(hdr);
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Predict Hit R", PVPUtils.CONFIG.predictionHitR, v -> PVPUtils.CONFIG.predictionHitR = v);
        y = this.addRgbSlider(cx, y, "Predict Hit G", PVPUtils.CONFIG.predictionHitG, v -> PVPUtils.CONFIG.predictionHitG = v);
        y = this.addRgbSlider(cx, y, "Predict Hit B", PVPUtils.CONFIG.predictionHitB, v -> PVPUtils.CONFIG.predictionHitB = v);
        y = this.addRgbSlider(cx, y, "Predict Idle R", PVPUtils.CONFIG.predictionIdleR, v -> PVPUtils.CONFIG.predictionIdleR = v);
        y = this.addRgbSlider(cx, y, "Predict Idle G", PVPUtils.CONFIG.predictionIdleG, v -> PVPUtils.CONFIG.predictionIdleG = v);
        y = this.addRgbSlider(cx, y, "Predict Idle B", PVPUtils.CONFIG.predictionIdleB, v -> PVPUtils.CONFIG.predictionIdleB = v);
        y += 19;
        y = this.addRgbSlider(cx, y, "FT цель R", PVPUtils.CONFIG.ftHelperHitR, v -> PVPUtils.CONFIG.ftHelperHitR = v);
        y = this.addRgbSlider(cx, y, "FT цель G", PVPUtils.CONFIG.ftHelperHitG, v -> PVPUtils.CONFIG.ftHelperHitG = v);
        y = this.addRgbSlider(cx, y, "FT цель B", PVPUtils.CONFIG.ftHelperHitB, v -> PVPUtils.CONFIG.ftHelperHitB = v);
        y = this.addRgbSlider(cx, y, "FT без цели R", PVPUtils.CONFIG.ftHelperIdleR, v -> PVPUtils.CONFIG.ftHelperIdleR = v);
        y = this.addRgbSlider(cx, y, "FT без цели G", PVPUtils.CONFIG.ftHelperIdleG, v -> PVPUtils.CONFIG.ftHelperIdleG = v);
        y = this.addRgbSlider(cx, y, "FT без цели B", PVPUtils.CONFIG.ftHelperIdleB, v -> PVPUtils.CONFIG.ftHelperIdleB = v);
        y += 19;
        y = this.addRgbSlider(cx, y, "Nimb R", PVPUtils.CONFIG.nimbR, v -> PVPUtils.CONFIG.nimbR = v);
        y = this.addRgbSlider(cx, y, "Nimb G", PVPUtils.CONFIG.nimbG, v -> PVPUtils.CONFIG.nimbG = v);
        y = this.addRgbSlider(cx, y, "Nimb B", PVPUtils.CONFIG.nimbB, v -> PVPUtils.CONFIG.nimbB = v);
        y = this.addGradientColorStopsRow(cx, y, "Nimb", PVPUtils.CONFIG.nimbColorStops, () -> {
            if (PVPUtils.CONFIG.nimbColorStops < 3) {
                PVPUtils.CONFIG.nimbColorStops++;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        }, () -> {
            if (PVPUtils.CONFIG.nimbColorStops > 1) {
                PVPUtils.CONFIG.nimbColorStops--;
                PVPUtils.CONFIG.normalizeAfterImport();
                PVPUtils.CONFIG.save();
                this.init();
            }
        });
        if (PVPUtils.CONFIG.nimbColorStops >= 2) {
            y = this.addRgbSlider(cx, y, "Nimb 2 R", PVPUtils.CONFIG.nimbR2, v -> PVPUtils.CONFIG.nimbR2 = v);
            y = this.addRgbSlider(cx, y, "Nimb 2 G", PVPUtils.CONFIG.nimbG2, v -> PVPUtils.CONFIG.nimbG2 = v);
            y = this.addRgbSlider(cx, y, "Nimb 2 B", PVPUtils.CONFIG.nimbB2, v -> PVPUtils.CONFIG.nimbB2 = v);
        }
        if (PVPUtils.CONFIG.nimbColorStops >= 3) {
            y = this.addRgbSlider(cx, y, "Nimb 3 R", PVPUtils.CONFIG.nimbR3, v -> PVPUtils.CONFIG.nimbR3 = v);
            y = this.addRgbSlider(cx, y, "Nimb 3 G", PVPUtils.CONFIG.nimbG3, v -> PVPUtils.CONFIG.nimbG3 = v);
            y = this.addRgbSlider(cx, y, "Nimb 3 B", PVPUtils.CONFIG.nimbB3, v -> PVPUtils.CONFIG.nimbB3 = v);
        }
        y = this.addGradientAnimToggleRow(cx, y, "Nimb", PVPUtils.CONFIG.nimbGradientAnim, () -> {
            PVPUtils.CONFIG.nimbGradientAnim = !PVPUtils.CONFIG.nimbGradientAnim;
            PVPUtils.CONFIG.save();
            this.init();
        });
        if (this.visible(y)) {
            ButtonWidget hdr = ButtonWidget.builder(Text.literal("=== Рука (Custom Hand) ==="), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdr.active = false;
            this.addDrawableChild(hdr);
        }
        y += 19;
        y = this.addFloatSlider(cx, y, "Hand X", PVPUtils.CONFIG.customHandOffsetX, -1.5f, 1.5f, v -> PVPUtils.CONFIG.customHandOffsetX = v);
        y = this.addFloatSlider(cx, y, "Hand Y", PVPUtils.CONFIG.customHandOffsetY, -1.5f, 1.5f, v -> PVPUtils.CONFIG.customHandOffsetY = v);
        y = this.addFloatSlider(cx, y, "Hand Z", PVPUtils.CONFIG.customHandOffsetZ, -1.5f, 1.5f, v -> PVPUtils.CONFIG.customHandOffsetZ = v);
        y = this.addFloatSlider(cx, y, "Hand Scale", PVPUtils.CONFIG.customHandScale, 0.2f, 2.0f, v -> PVPUtils.CONFIG.customHandScale = v);
        y += 19;
        if (this.visible(y)) {
            String fx = PVPUtils.CONFIG.customHandFixedPose ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Фикс. поза (только свинг, без клона): " + fx), b -> {
                PVPUtils.CONFIG.customHandFixedPose = !PVPUtils.CONFIG.customHandFixedPose;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdr = ButtonWidget.builder(Text.literal("— Поза руки (XYZ), без свинга —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdr.active = false;
            this.addDrawableChild(hdr);
        }
        y += 19;
        y = this.addIntSlider(cx, y, "Рука поза X", PVPUtils.CONFIG.customHandPoseX, -180, 180, v -> PVPUtils.CONFIG.customHandPoseX = v);
        y = this.addIntSlider(cx, y, "Рука поза Y", PVPUtils.CONFIG.customHandPoseY, -180, 180, v -> PVPUtils.CONFIG.customHandPoseY = v);
        y = this.addIntSlider(cx, y, "Рука поза Z", PVPUtils.CONFIG.customHandPoseZ, -180, 180, v -> PVPUtils.CONFIG.customHandPoseZ = v);
        if (this.visible(y)) {
            ButtonWidget hdr = ButtonWidget.builder(Text.literal("— Свинг (XYZ × удар), отдельно от позы —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdr.active = false;
            this.addDrawableChild(hdr);
        }
        y += 19;
        if (this.visible(y)) {
            String sw = PVPUtils.CONFIG.customHandSwingEnabled ? "Вкл" : "Выкл";
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Custom Swing: " + sw), b -> {
                PVPUtils.CONFIG.customHandSwingEnabled = !PVPUtils.CONFIG.customHandSwingEnabled;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        y = this.addIntSlider(cx, y, "Рука свинг X", PVPUtils.CONFIG.customHandSwingX, -180, 180, v -> PVPUtils.CONFIG.customHandSwingX = v);
        y = this.addIntSlider(cx, y, "Рука свинг Y", PVPUtils.CONFIG.customHandSwingY, -180, 180, v -> PVPUtils.CONFIG.customHandSwingY = v);
        y = this.addIntSlider(cx, y, "Рука свинг Z", PVPUtils.CONFIG.customHandSwingZ, -180, 180, v -> PVPUtils.CONFIG.customHandSwingZ = v);
        if (this.visible(y)) {
            ButtonWidget hdrFr = ButtonWidget.builder(Text.literal("— Friend System —"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrFr.active = false;
            this.addDrawableChild(hdrFr);
        }
        y += 19;
        if (this.visible(y)) {
            TextFieldWidget friendField = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("Friend"));
            friendField.setMaxLength(256);
            friendField.setPlaceholder(Text.literal("Тимейты: Ник1, Ник2 , ..."));
            friendField.setText(PVPUtils.CONFIG.friendNickname == null ? "" : PVPUtils.CONFIG.friendNickname);
            friendField.setChangedListener(s -> {
                PVPUtils.CONFIG.friendNickname = s;
                PVPUtils.CONFIG.save();
            });
            this.addDrawableChild(friendField);
        }
        y += 21;
        if (this.visible(y)) {
            TextFieldWidget lockSlotsField = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("LockSlots"));
            lockSlotsField.setMaxLength(48);
            lockSlotsField.setPlaceholder(Text.literal("Lock slots: 1,5,8"));
            lockSlotsField.setText(PVPUtils.CONFIG.lockSlotsCsv == null ? "" : PVPUtils.CONFIG.lockSlotsCsv);
            lockSlotsField.setChangedListener(s -> {
                PVPUtils.CONFIG.lockSlotsCsv = s;
                PVPUtils.CONFIG.save();
            });
            this.addDrawableChild(lockSlotsField);
        }
        y += 21;
        if (this.visible(y)) {
            TextFieldWidget item1Field = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("ItemSwap1"));
            item1Field.setMaxLength(96);
            item1Field.setPlaceholder(Text.literal("1 предмет: введите название"));
            item1Field.setText(PVPUtils.CONFIG.itemSwapItem1Name == null ? "" : PVPUtils.CONFIG.itemSwapItem1Name);
            item1Field.setChangedListener(s -> {
                PVPUtils.CONFIG.itemSwapItem1Name = s;
                PVPUtils.CONFIG.save();
            });
            this.addDrawableChild(item1Field);
        }
        y += 21;
        if (this.visible(y)) {
            TextFieldWidget item2Field = new TextFieldWidget(this.textRenderer, cx - 110, y, 220, 18, Text.literal("ItemSwap2"));
            item2Field.setMaxLength(96);
            item2Field.setPlaceholder(Text.literal("2 предмет: введите название"));
            item2Field.setText(PVPUtils.CONFIG.itemSwapItem2Name == null ? "" : PVPUtils.CONFIG.itemSwapItem2Name);
            item2Field.setChangedListener(s -> {
                PVPUtils.CONFIG.itemSwapItem2Name = s;
                PVPUtils.CONFIG.save();
            });
            this.addDrawableChild(item2Field);
        }
        y += 21;
        if (this.visible(y)) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Item Swap рука: " + (PVPUtils.CONFIG.itemSwapUseOffhand ? "Левая" : "Правая")), b -> {
                PVPUtils.CONFIG.itemSwapUseOffhand = !PVPUtils.CONFIG.itemSwapUseOffhand;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            String notifyMode = PVPUtils.CONFIG.miniHudNotifications
                    ? "\u041c\u0438\u043d\u0438 HUD"
                    : "\u041a\u043b\u0430\u0441\u0441\u0438\u043a\u0430";
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f (Swap / \u0422\u0440\u0430\u043f\u0430 / \u0422\u043e\u0442\u0435\u043c): " + notifyMode), b -> {
                PVPUtils.CONFIG.miniHudNotifications = !PVPUtils.CONFIG.miniHudNotifications;
                if (!PVPUtils.CONFIG.miniHudNotifications) {
                    MiniHudNotifications.clearForMiniHudDisabled();
                }
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            ButtonWidget hdrMn = ButtonWidget.builder(Text.literal("\u2014 \u041c\u0438\u043d\u0438-\u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f: \u0440\u0430\u0437\u043c\u0435\u0440\u044b \u2014"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdrMn.active = false;
            this.addDrawableChild(hdrMn);
        }
        y += 19;
        y = this.addIntSlider(cx, y, "\u0422\u0440\u0430\u043f\u043a\u0430: \u0432\u044b\u0441\u043e\u0442\u0430 \u043f\u043b\u0430\u0448\u043a\u0438", PVPUtils.CONFIG.miniNotifyTrapPillH, 16, 40, v -> {
            PVPUtils.CONFIG.miniNotifyTrapPillH = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        y = this.addIntSlider(cx, y, "\u0422\u0440\u0430\u043f\u043a\u0430: \u0438\u043a\u043e\u043d\u043a\u0430 (px)", PVPUtils.CONFIG.miniNotifyTrapIconPx, 8, 24, v -> {
            PVPUtils.CONFIG.miniNotifyTrapIconPx = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        y = this.addIntSlider(cx, y, "Swap: \u0432\u044b\u0441\u043e\u0442\u0430", PVPUtils.CONFIG.miniNotifySwapPillH, 16, 40, v -> {
            PVPUtils.CONFIG.miniNotifySwapPillH = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        y = this.addIntSlider(cx, y, "Swap: \u0438\u043a\u043e\u043d\u043a\u0430 (px)", PVPUtils.CONFIG.miniNotifySwapIconPx, 8, 24, v -> {
            PVPUtils.CONFIG.miniNotifySwapIconPx = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        y = this.addIntSlider(cx, y, "\u0422\u043e\u0442\u0435\u043c: \u0432\u044b\u0441\u043e\u0442\u0430", PVPUtils.CONFIG.miniNotifyTotemPillH, 16, 40, v -> {
            PVPUtils.CONFIG.miniNotifyTotemPillH = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        y = this.addIntSlider(cx, y, "\u0422\u043e\u0442\u0435\u043c: \u0438\u043a\u043e\u043d\u043a\u0430 (px)", PVPUtils.CONFIG.miniNotifyTotemIconPx, 8, 24, v -> {
            PVPUtils.CONFIG.miniNotifyTotemIconPx = v;
            PVPUtils.CONFIG.normalizeAfterImport();
        });
        if (this.visible(y)) {
            ButtonWidget hdr = ButtonWidget.builder(
                    Text.literal("Трапка 5×5 / 7×7: общий режим для таймера и FT"), b -> {
            }).dimensions(cx - 110, y, 220, 16).build();
            hdr.active = false;
            this.addDrawableChild(hdr);
        }
        y += 19;
        if (this.visible(y)) {
            boolean normal = !PVPUtils.CONFIG.trapTimerDragonMode;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Трапка: обычная 5×5" + (normal ? " §a(выбрано)" : "")), b -> {
                PVPUtils.CONFIG.trapTimerDragonMode = false;
                PVPUtils.CONFIG.ftHelperDragonTrapMode = false;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        y += 19;
        if (this.visible(y)) {
            boolean dragon = PVPUtils.CONFIG.trapTimerDragonMode;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Трапка: драконья 7×7" + (dragon ? " §a(выбрано)" : "")), b -> {
                PVPUtils.CONFIG.trapTimerDragonMode = true;
                PVPUtils.CONFIG.ftHelperDragonTrapMode = true;
                PVPUtils.CONFIG.save();
                this.init();
            }).dimensions(cx - 110, y, 220, 16).build());
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), b -> this.close())
                .dimensions(cx - 55, this.height / 2 + 150 - 20, 110, 16).build());
    }

    private SliderWidget attackIndicatorSlider(int x, int y, ModConfig ai) {
        double v = (ai.effectiveScale() - 0.5f) / 2.5f;
        return new SliderWidget(x, y, 220, 16, Text.literal(""), v) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                float s = 0.5f + (float) this.value * 2.5f;
                this.setMessage(Text.literal(String.format("Attack Indicator %.2f", s)));
            }

            @Override
            protected void applyValue() {
                float s = 0.5f + (float) this.value * 2.5f;
                ai.scale = s;
                ai.save();
            }
        };
    }

    private SliderWidget durabilityStrengthSlider(int x, int y) {
        double v = DurabilityTintConfig.getStrength();
        return new SliderWidget(x, y, 220, 16, Text.literal(""), v) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                int pct = Math.round((float) this.value * 100.0f);
                this.setMessage(Text.literal("Durability tint: " + pct + "%"));
            }

            @Override
            protected void applyValue() {
                DurabilityTintConfig.setStrength((float) this.value);
                DurabilityTintConfig.save();
            }
        };
    }

    /** Подпись слайдера Auto Potion: 1 с … 15 мин (900 с). */
    private static String formatDelaySeconds(float sec) {
        int total = Math.round(sec);
        total = Math.max(1, Math.min(900, total));
        if (total < 60) {
            return total + " с";
        }
        int m = total / 60;
        int s = total % 60;
        if (s == 0) {
            return m + " мин";
        }
        return m + " мин " + s + " с";
    }

    private SliderWidget autoPotionDelaySlider(int x, int y) {
        float min = 1.0f;
        float max = 900.0f;
        float cur = PVPUtils.CONFIG.autoPotionDelaySeconds;
        double v01 = (cur - min) / (max - min);
        return new SliderWidget(x, y, 220, 16, Text.literal(""), Math.max(0.0, Math.min(1.0, v01))) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                float sec = min + (float) (this.value * (max - min));
                this.setMessage(Text.literal("Auto Potion: интервал " + SpeedysSettingsPopupScreen.formatDelaySeconds(sec)));
            }

            @Override
            protected void applyValue() {
                PVPUtils.CONFIG.autoPotionDelaySeconds = Math.max(min, Math.min(max, min + (float) (this.value * (max - min))));
                PVPUtils.CONFIG.save();
            }
        };
    }

    private SliderWidget zoomSlider(int x, int y) {
        double v = (PVPUtils.CONFIG.zoomLevel - 1) / 49.0;
        return new SliderWidget(x, y, 220, 16, Text.literal(""), v) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                int lvl = Zoom.clampLevel((int) Math.round(1.0 + this.value * 49.0));
                this.setMessage(Text.literal("Zoom: " + lvl));
            }

            @Override
            protected void applyValue() {
                PVPUtils.CONFIG.zoomLevel = Zoom.clampLevel((int) Math.round(1.0 + this.value * 49.0));
                PVPUtils.CONFIG.save();
            }
        };
    }

    private SliderWidget fullBrightOnSlider(int x, int y) {
        double v = (PVPUtils.CONFIG.fullBrightGammaOn - 100) / 1900.0;
        return new SliderWidget(x, y, 220, 16, Text.literal(""), Math.max(0.0, Math.min(1.0, v))) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                int g = (int) Math.round(100.0 + this.value * 1900.0);
                this.setMessage(Text.literal("Fullbright ON gamma: " + g));
            }

            @Override
            protected void applyValue() {
                PVPUtils.CONFIG.fullBrightGammaOn = (int) Math.round(100.0 + this.value * 1900.0);
                PVPUtils.CONFIG.save();
            }
        };
    }

    private SliderWidget fullBrightOffSlider(int x, int y) {
        double v = (PVPUtils.CONFIG.fullBrightGammaOff - 100) / 1900.0;
        return new SliderWidget(x, y, 220, 16, Text.literal(""), Math.max(0.0, Math.min(1.0, v))) {
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                int g = (int) Math.round(100.0 + this.value * 1900.0);
                this.setMessage(Text.literal("Fullbright OFF gamma: " + g));
            }

            @Override
            protected void applyValue() {
                PVPUtils.CONFIG.fullBrightGammaOff = (int) Math.round(100.0 + this.value * 1900.0);
                PVPUtils.CONFIG.save();
            }
        };
    }

    private int addGradientColorStopsRow(int cx, int y, String shortName, int stops, Runnable inc, Runnable dec) {
        if (this.visible(y)) {
            ButtonWidget addB = ButtonWidget.builder(Text.literal(shortName + " +цвет (" + stops + "/3)"), b -> inc.run())
                    .dimensions(cx - 110, y, 152, 16).build();
            addB.active = stops < 3;
            this.addDrawableChild(addB);
            ButtonWidget decB = ButtonWidget.builder(Text.literal("\u2212"), b -> dec.run()).dimensions(cx + 48, y, 62, 16).build();
            decB.active = stops > 1;
            this.addDrawableChild(decB);
        }
        return y + 19;
    }

    private int addGradientAnimToggleRow(int cx, int y, String shortName, boolean anim, Runnable toggle) {
        if (this.visible(y)) {
            String lab = anim ? "\u0412\u043a\u043b" : "\u0412\u044b\u043a\u043b";
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(shortName + " \u043f\u0435\u0440\u0435\u043b\u0438\u0432: " + lab),
                    b -> toggle.run()).dimensions(cx - 110, y, 220, 16).build());
        }
        return y + 19;
    }

    private int addSoundSlider(int cx, int y, String prefix, float unit, Consumer<Float> apply) {
        if (this.visible(y)) {
            float clamped = SoundController.clampUnit(unit);
            this.addDrawableChild(new SliderWidget(cx - 110, y, 220, 16, Text.literal(""), clamped) {
                {
                    this.updateMessage();
                }

                @Override
                protected void updateMessage() {
                    int pct = Math.round(SoundController.clampUnit((float) this.value) * 100.0f);
                    this.setMessage(Text.literal(prefix + ": " + pct + "%"));
                }

                @Override
                protected void applyValue() {
                    apply.accept(SoundController.clampUnit((float) this.value));
                }
            });
        }
        return y + 19;
    }

    private int addRgbSlider(int cx, int y, String label, int currentChannel, IntConsumer setChannel) {
        if (this.visible(y)) {
            double v01 = currentChannel / 255.0;
            this.addDrawableChild(new SliderWidget(cx - 110, y, 220, 16, Text.literal(""), v01) {
                {
                    this.updateMessage();
                }

                @Override
                protected void updateMessage() {
                    int c = (int) Math.round(this.value * 255.0);
                    this.setMessage(Text.literal(label + ": " + c));
                }

                @Override
                protected void applyValue() {
                    setChannel.accept((int) Math.round(this.value * 255.0));
                    PVPUtils.CONFIG.save();
                }
            });
        }
        return y + 19;
    }

    private int addIntSlider(int cx, int y, String label, int current, int min, int max, IntConsumer apply) {
        if (this.visible(y)) {
            double v01 = (double) (current - min) / (double) Math.max(1, max - min);
            this.addDrawableChild(new SliderWidget(cx - 110, y, 220, 16, Text.literal(""), v01) {
                {
                    this.updateMessage();
                }

                @Override
                protected void updateMessage() {
                    int v = (int) Math.round(min + this.value * (max - min));
                    this.setMessage(Text.literal(label + ": " + v));
                }

                @Override
                protected void applyValue() {
                    apply.accept((int) Math.round(min + this.value * (max - min)));
                    PVPUtils.CONFIG.save();
                }
            });
        }
        return y + 19;
    }

    private int addFloatSlider(int cx, int y, String label, float current, float min, float max, Consumer<Float> apply) {
        if (this.visible(y)) {
            double v01 = (current - min) / Math.max(1.0E-4f, max - min);
            this.addDrawableChild(new SliderWidget(cx - 110, y, 220, 16, Text.literal(""), v01) {
                {
                    this.updateMessage();
                }

                @Override
                protected void updateMessage() {
                    float v = (float) (min + this.value * (max - min));
                    this.setMessage(Text.literal(label + ": " + String.format("%.2f", v)));
                }

                @Override
                protected void applyValue() {
                    apply.accept((float) (min + this.value * (max - min)));
                    PVPUtils.CONFIG.save();
                }
            });
        }
        return y + 19;
    }

    private boolean visible(int y) {
        int top = this.height / 2 - 150 + 14;
        int bottom = this.height / 2 + 150 - 30;
        return y >= top && y <= bottom;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int l = this.width / 2 - 140;
        int t = this.height / 2 - 150;
        int r = this.width / 2 + 140;
        int b = this.height / 2 + 150;
        this.drawRoundedRect(context, l, t, r, b, BG);
        this.drawRoundedOutline(context, l, t, r, b, OUTLINE);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, t + 6, -1);
        super.render(context, mouseX, mouseY, delta);
        this.drawSwitch(context, this.width / 2 + 72, this.height / 2 - 150 + 21 - this.scroll, PVPUtils.CONFIG.armorHudEnabled);
        this.drawSwitch(context, this.width / 2 + 72, this.height / 2 - 150 + 40 - this.scroll, PVPUtils.CONFIG.effectTimerEnabled);
        this.drawSwitch(context, this.width / 2 + 72, this.height / 2 - 150 + 59 - this.scroll, PVPUtils.CONFIG.effectTimerListPanel);
    }

    private void drawSwitch(DrawContext context, int x, int y, boolean on) {
        this.drawRoundedRect(context, x, y, x + 30, y + 10, on ? SWITCH_ON : SWITCH_OFF);
        int kx = on ? x + 20 : x + 1;
        this.drawRoundedRect(context, kx, y + 1, kx + 9, y + 9, SWITCH_KNOB);
    }

    private void drawRoundedRect(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left + 3, top, right - 3, bottom, color);
        context.fill(left, top + 3, right, bottom - 3, color);
        context.fill(left + 1, top + 1, left + 3, top + 3, color);
        context.fill(right - 3, top + 1, right - 1, top + 3, color);
        context.fill(left + 1, bottom - 3, left + 3, bottom - 1, color);
        context.fill(right - 3, bottom - 3, right - 1, bottom - 1, color);
    }

    private void drawRoundedOutline(DrawContext context, int left, int top, int right, int bottom, int color) {
        context.fill(left + 3, top, right - 3, top + 1, color);
        context.fill(left + 3, bottom - 1, right - 3, bottom, color);
        context.fill(left, top + 3, left + 1, bottom - 3, color);
        context.fill(right - 1, top + 3, right, bottom - 3, color);
        context.fill(left + 1, top + 1, left + 3, top + 2, color);
        context.fill(right - 3, top + 1, right - 1, top + 2, color);
        context.fill(left + 1, bottom - 2, left + 3, bottom - 1, color);
        context.fill(right - 3, bottom - 2, right - 1, bottom - 1, color);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.getFocused() instanceof SliderWidget) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        this.scroll = Math.max(0, this.scroll - (int) (verticalAmount * 18.0));
        this.init();
        return true;
    }

    @Override
    public void close() {
        if (PVPUtils.CONFIG != null) {
            PVPUtils.CONFIG.save();
        }
        ModConfig.getConfig().save();
        DurabilityTintConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
