/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package com.shampoon.pvputils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shampoon.pvputils.features.SoundController;
import com.shampoon.pvputils.features.Zoom;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class PVPUtilsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pvputils.json");
    public boolean itemPickerEnabled = true;
    public boolean itemScrollerEnabled = true;
    public boolean fastXPEnabled = true;
    public boolean autoSprintEnabled = true;
    public boolean armorHudEnabled = true;
    public boolean invHudEnabled = false;
    public float invHudScale = 1.0f;
    public int invHudOffsetX = 100;
    public int invHudOffsetY = -70;
    public boolean armorHelperEnabled = true;
    public boolean effectTimerEnabled = true;
    /** Список эффектов в стиле панели (новая разметка); false — таймеры у иконок ванильного HUD. */
    public boolean effectTimerListPanel = false;
    /** Смещение HUD таймеров: классика — от правого края / строк; панель — от позиции по умолчанию (правый верх). Перетаскивание в чате. */
    public int effectTimerHudOffsetX = 0;
    public int effectTimerHudOffsetY = 0;
    public boolean fullBrightEnabled = false;
    public int fullBrightGammaOn = 1500;
    public int fullBrightGammaOff = 100;
    public boolean removeMiscEnabled = true;
    public boolean removeMiscHideFire = false;
    public boolean removeMiscHideTotemAnimation = false;
    public boolean removeMiscHideBlindnessDarknessOverlay = false;
    public boolean removeMiscHidePrecipitation = false;
    public boolean zoomEnabled = true;
    public int zoomLevel = 4;
    public boolean noFluidEnabled = false;
    public boolean soundControllerEnabled = false;
    public float soundControllerExpOrbVolume = 1.0f;
    public float soundControllerWitherVolume = 1.0f;
    public float soundControllerTridentVolume = 1.0f;
    public boolean trailEnabled = false;
    public int trailR = 0;
    public int trailG = 255;
    public int trailB = 255;
    public boolean chinaHatEnabled = false;
    public int chinaHatR = 255;
    public int chinaHatG = 100;
    public int chinaHatB = 100;
    public boolean jumpCircleEnabled = false;
    public boolean jumpCircleSpreadByBlocks = false;
    public int jumpCircleR = 255;
    public int jumpCircleG = 255;
    public int jumpCircleB = 255;
    public boolean targetEspEnabled = false;
    /** Обводка блока под прицелом (линии). */
    public boolean blockOverlayEnabled = false;
    public int blockOverlayR = 255;
    public int blockOverlayG = 255;
    public int blockOverlayB = 255;
    /** Отдельная обводка вертикальных рёбер («бока»). */
    public boolean blockOverlaySidesEnabled = false;
    public int blockOverlaySidesR = 255;
    public int blockOverlaySidesG = 160;
    public int blockOverlaySidesB = 0;
    /** 0 = круг (качается по высоте), 1 = сферы по орбите, 2 = частицы душ / листвы */
    public int targetEspMode = 1;
    public int targetEspR = 255;
    public int targetEspG = 255;
    public int targetEspB = 255;
    public boolean nimbEnabled = false;
    public int nimbR = 255;
    public int nimbG = 255;
    public int nimbB = 0;
    /** 1–3 стопы для градиента (Trail / China Hat / Block Overlay лицо / Nimb). */
    public int trailColorStops = 1;
    public boolean trailGradientAnim = false;
    public int trailR2 = 255;
    public int trailG2 = 0;
    public int trailB2 = 255;
    public int trailR3 = 0;
    public int trailG3 = 255;
    public int trailB3 = 255;
    public int chinaHatColorStops = 1;
    public boolean chinaHatGradientAnim = false;
    public int chinaHatR2 = 100;
    public int chinaHatG2 = 255;
    public int chinaHatB2 = 100;
    public int chinaHatR3 = 255;
    public int chinaHatG3 = 100;
    public int chinaHatB3 = 255;
    public int blockOverlayColorStops = 1;
    public boolean blockOverlayGradientAnim = false;
    public int blockOverlayR2 = 0;
    public int blockOverlayG2 = 255;
    public int blockOverlayB2 = 255;
    public int blockOverlayR3 = 255;
    public int blockOverlayG3 = 0;
    public int blockOverlayB3 = 255;
    public int nimbColorStops = 1;
    public boolean nimbGradientAnim = false;
    public int nimbR2 = 255;
    public int nimbG2 = 128;
    public int nimbB2 = 0;
    public int nimbR3 = 128;
    public int nimbG3 = 0;
    public int nimbB3 = 255;
    public int predictionHitR = 60;
    public int predictionHitG = 255;
    public int predictionHitB = 60;
    public int predictionIdleR = 160;
    public int predictionIdleG = 160;
    public int predictionIdleB = 160;
    public boolean fullBrightVisualsTabEnabled = false;
    /** Плавный переход наведения с блока на блок (клиент). */
    public boolean blockHoverSmooth = false;
    public boolean customHandEnabled = false;
    public float customHandOffsetX = 0.0f;
    public float customHandOffsetY = 0.0f;
    public float customHandOffsetZ = 0.0f;
    public float customHandScale = 1.0f;
    /**
     * Основная рука: не клонить предмет от pitch камеры и анимации смены (equip),
     * только поза из слайдеров + свинг.
     */
    public boolean customHandFixedPose = true;
    /** Поворот руки в покое, оси независимы (градусы, −180…180). */
    public int customHandPoseX = 0;
    public int customHandPoseY = 0;
    public int customHandPoseZ = 0;
    /** Доп. поворот при взмахе, оси независимы (умножается на прогресс удара). */
    public boolean customHandSwingEnabled = true;
    public int customHandSwingX = 0;
    public int customHandSwingY = 0;
    public int customHandSwingZ = 0;
    /** Старые поля из pvputils.json — один раз переносятся в poseZ / swingX и обнуляются. */
    public int customHandPosRotation = 0;
    public int customHandHitRotation = 0;
    public boolean trapTimerEnabled = false;
    public boolean trapTimerDragonMode = false;
    public boolean ftHelperEnabled = false;
    public boolean ftHelperDragonTrapMode = false;
    public int ftHelperHitR = 60;
    public int ftHelperHitG = 255;
    public int ftHelperHitB = 60;
    public int ftHelperIdleR = 160;
    public int ftHelperIdleG = 160;
    public int ftHelperIdleB = 160;
    public boolean targetHudEnabled = false;
    /** Target HUD: внизу панели строка с названием предмета в левой руке (без иконки). */
    public boolean targetHudShowOffhand = false;
    public boolean totemTrackerEnabled = false;
    public boolean lockSlotsEnabled = false;
    public String lockSlotsCsv = "";
    public boolean itemSwapEnabled = false;
    public String itemSwapItem1Name = "";
    public String itemSwapItem2Name = "";
    public boolean itemSwapUseOffhand = false;
    /**
     * Уведомления Item Swap и Trap Timer: {@code false} — строка действия (классика),
     * {@code true} — мини-HUD над сеткой Inv HUD (до 3 строк, отдельное перетаскивание в чате).
     */
    public boolean miniHudNotifications = false;
    public int miniHudNotifyOffsetX = 0;
    public int miniHudNotifyOffsetY = 0;
    /** Высота плашки и размер иконки (px) для типов мини-уведомлений. */
    public int miniNotifyTrapPillH = 22;
    public int miniNotifyTrapIconPx = 12;
    public int miniNotifySwapPillH = 20;
    public int miniNotifySwapIconPx = 10;
    public int miniNotifyTotemPillH = 20;
    public int miniNotifyTotemIconPx = 11;
    public boolean antiInvisEnabled = false;
    public boolean friendSystemEnabled = false;
    /** Тимейты: ники через запятую, напр. {@code Player1, Player2}. */
    public String friendNickname = "";
    /** Custom Ratio: локальная подмена соотношения сторон (только визуально). */
    public boolean customRatioEnabled = false;
    /** Формат "W:H", например "4:3" или "16:9". */
    public String customRatioValue = "16:9";
    /** Custom World: кастомный цвет неба/тумана. */
    public boolean customWorldEnabled = false;
    public int customWorldSkyR = 120;
    public int customWorldSkyG = 180;
    public int customWorldSkyB = 255;
    /** 0 — облака (ванила), 1 — кометы (без облаков). */
    public int customWorldSkyType = 0;
    /** Комет в минуту (1–10), только для типа неба «кометы». */
    public int customWorldCometsPerMinute = 2;
    /** Время полёта кометы в секундах (6–30), затем 3 с затухания. */
    public int customWorldCometFlightSeconds = 12;
    public int customWorldCometR = 220;
    public int customWorldCometG = 235;
    public int customWorldCometB = 255;
    public boolean customWorldFogEnabled = false;
    public int customWorldFogDistanceBlocks = 48;
    public int customWorldFogR = 120;
    public int customWorldFogG = 180;
    public int customWorldFogB = 255;
    public boolean worldParticlesEnabled = false;
    /** 0=звезда, 1=снежинка, 2=доллар. */
    public int worldParticlesType = 0;
    /** Размер зоны спавна по чанкам (N x N), 1..4. */
    public int worldParticlesChunks = 1;
    /** true — падают, false — парят в воздухе. */
    public boolean worldParticlesFalling = false;
    /** Скорость падения 0.1..3.0 */
    public float worldParticlesFallSpeed = 1.0f;
    /** Время жизни 1..10 секунд (потом затухают). */
    public float worldParticlesLifetimeSeconds = 4.0f;
    public int worldParticlesR = 255;
    public int worldParticlesG = 255;
    public int worldParticlesB = 255;
    /** Подсказка еды / зелий исцеления I–II при низком HP и/или голоде (обводка слотов). */
    public boolean eatHelperEnabled = false;
    public int eatHelperR = 80;
    public int eatHelperG = 255;
    public int eatHelperB = 120;
    /** Подсветка зелий: больше плохих эффектов — красный, поровну — жёлтый, больше хороших — зелёный. */
    public boolean potionHighlighterEnabled = false;
    public int potionHlBadR = 255;
    public int potionHlBadG = 70;
    public int potionHlBadB = 70;
    public int potionHlMixR = 255;
    public int potionHlMixG = 210;
    public int potionHlMixB = 60;
    public int potionHlGoodR = 70;
    public int potionHlGoodG = 255;
    public int potionHlGoodB = 100;
    /** Авто-питьё зелья из основной руки (обычное зелье). */
    public boolean autoPotionEnabled = false;
    /** Интервал между глотками, секунды (1…900, т.е. до 15 мин; настраивается в меню). */
    public float autoPotionDelaySeconds = 3.0f;
    /** Цвет «вспышки» при уроне (игроки и мобы). */
    public boolean hitColorEnabled = false;
    public int hitColorR = 255;
    public int hitColorG = 60;
    public int hitColorB = 60;
    /** Кастомные хитбоксы сущностей (обводка + опциональная заливка). */
    public boolean customHitboxesEnabled = false;
    /** Обводка основного AABB. */
    public int customHitboxBoxR = 255;
    public int customHitboxBoxG = 255;
    public int customHitboxBoxB = 255;
    /** Линия взгляда (от глаз по направлению). */
    public int customHitboxEyeR = 0;
    public int customHitboxEyeG = 255;
    public int customHitboxEyeB = 255;
    /** Обводка зоны головы (над глазами). */
    public int customHitboxHeadR = 255;
    public int customHitboxHeadG = 200;
    public int customHitboxHeadB = 0;
    public boolean customHitboxFillEnabled = false;
    public int customHitboxFillR = 255;
    public int customHitboxFillG = 80;
    public int customHitboxFillB = 80;

    /**
     * Gson для отсутствующих {@code int} подставляет 0 — восстанавливаем дефолт только если ключа нет в JSON.
     */
    public static void applyMissingCometColorFields(PVPUtilsConfig cfg, JsonObject configFields) {
        if (cfg == null || configFields == null) {
            return;
        }
        if (!configFields.has("customWorldCometR")) {
            cfg.customWorldCometR = 220;
        }
        if (!configFields.has("customWorldCometG")) {
            cfg.customWorldCometG = 235;
        }
        if (!configFields.has("customWorldCometB")) {
            cfg.customWorldCometB = 255;
        }
    }

    public static PVPUtilsConfig load() {
        if (Files.exists(CONFIG_PATH, new LinkOption[0])) {
            try {
                String json = Files.readString(CONFIG_PATH);
                JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
                PVPUtilsConfig config = (PVPUtilsConfig)GSON.fromJson(json, PVPUtilsConfig.class);
                if (config == null) {
                    config = new PVPUtilsConfig();
                } else {
                    PVPUtilsConfig.applyMissingCometColorFields(config, jo);
                    config.normalizeAfterImport();
                }
                return config;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        PVPUtilsConfig config = new PVPUtilsConfig();
        config.save();
        return config;
    }

    /** Вызов после Gson (файл конфига или пресет) — миграции полей и clamp. */
    public void normalizeAfterImport() {
        this.normalizeFullBright();
        this.normalizeZoom();
        this.normalizeSoundController();
        this.normalizeSpeedysColors();
        this.normalizeFriendNick();
        this.normalizeFeatureStrings();
        this.normalizeAutoPotion();
        this.normalizeEffectTimerHud();
        this.normalizeCustomRatio();
        this.normalizeCustomWorld();
        this.normalizeWorldParticles();
        this.normalizePotionHighlighter();
        this.normalizeMiniHudNotify();
    }

    private void normalizeAutoPotion() {
        this.autoPotionDelaySeconds = PVPUtilsConfig.clampFloat(this.autoPotionDelaySeconds, 1.0f, 900.0f);
    }

    private void normalizeEffectTimerHud() {
        this.effectTimerHudOffsetX = PVPUtilsConfig.clampInt(this.effectTimerHudOffsetX, -4000, 4000);
        this.effectTimerHudOffsetY = PVPUtilsConfig.clampInt(this.effectTimerHudOffsetY, -4000, 4000);
    }

    private void normalizeCustomRatio() {
        if (this.customRatioValue == null || this.customRatioValue.isBlank()) {
            this.customRatioValue = "16:9";
        } else {
            this.customRatioValue = this.customRatioValue.trim();
        }
    }

    private void normalizeCustomWorld() {
        this.customWorldSkyR = PVPUtilsConfig.clamp255(this.customWorldSkyR);
        this.customWorldSkyG = PVPUtilsConfig.clamp255(this.customWorldSkyG);
        this.customWorldSkyB = PVPUtilsConfig.clamp255(this.customWorldSkyB);
        this.customWorldSkyType = PVPUtilsConfig.clampInt(this.customWorldSkyType, 0, 1);
        this.customWorldCometsPerMinute = PVPUtilsConfig.clampInt(this.customWorldCometsPerMinute, 1, 10);
        this.customWorldCometFlightSeconds = PVPUtilsConfig.clampInt(this.customWorldCometFlightSeconds, 6, 30);
        this.customWorldCometR = PVPUtilsConfig.clamp255(this.customWorldCometR);
        this.customWorldCometG = PVPUtilsConfig.clamp255(this.customWorldCometG);
        this.customWorldCometB = PVPUtilsConfig.clamp255(this.customWorldCometB);
        this.customWorldFogDistanceBlocks = PVPUtilsConfig.clampInt(this.customWorldFogDistanceBlocks, 10, 100);
        this.customWorldFogR = PVPUtilsConfig.clamp255(this.customWorldFogR);
        this.customWorldFogG = PVPUtilsConfig.clamp255(this.customWorldFogG);
        this.customWorldFogB = PVPUtilsConfig.clamp255(this.customWorldFogB);
    }

    private void normalizeMiniHudNotify() {
        this.miniHudNotifyOffsetX = PVPUtilsConfig.clampInt(this.miniHudNotifyOffsetX, -4000, 4000);
        this.miniHudNotifyOffsetY = PVPUtilsConfig.clampInt(this.miniHudNotifyOffsetY, -4000, 4000);
        this.miniNotifyTrapPillH = PVPUtilsConfig.clampInt(this.miniNotifyTrapPillH, 16, 40);
        this.miniNotifySwapPillH = PVPUtilsConfig.clampInt(this.miniNotifySwapPillH, 16, 40);
        this.miniNotifyTotemPillH = PVPUtilsConfig.clampInt(this.miniNotifyTotemPillH, 16, 40);
        this.miniNotifyTrapIconPx = PVPUtilsConfig.clampInt(this.miniNotifyTrapIconPx, 8, this.miniNotifyTrapPillH - 4);
        this.miniNotifySwapIconPx = PVPUtilsConfig.clampInt(this.miniNotifySwapIconPx, 8, this.miniNotifySwapPillH - 4);
        this.miniNotifyTotemIconPx = PVPUtilsConfig.clampInt(this.miniNotifyTotemIconPx, 8, this.miniNotifyTotemPillH - 4);
    }

    private void normalizePotionHighlighter() {
        this.potionHlBadR = PVPUtilsConfig.clamp255(this.potionHlBadR);
        this.potionHlBadG = PVPUtilsConfig.clamp255(this.potionHlBadG);
        this.potionHlBadB = PVPUtilsConfig.clamp255(this.potionHlBadB);
        this.potionHlMixR = PVPUtilsConfig.clamp255(this.potionHlMixR);
        this.potionHlMixG = PVPUtilsConfig.clamp255(this.potionHlMixG);
        this.potionHlMixB = PVPUtilsConfig.clamp255(this.potionHlMixB);
        this.potionHlGoodR = PVPUtilsConfig.clamp255(this.potionHlGoodR);
        this.potionHlGoodG = PVPUtilsConfig.clamp255(this.potionHlGoodG);
        this.potionHlGoodB = PVPUtilsConfig.clamp255(this.potionHlGoodB);
    }

    private void normalizeWorldParticles() {
        this.worldParticlesType = PVPUtilsConfig.clampInt(this.worldParticlesType, 0, 2);
        this.worldParticlesChunks = PVPUtilsConfig.clampInt(this.worldParticlesChunks, 1, 4);
        this.worldParticlesFallSpeed = PVPUtilsConfig.clampFloat(this.worldParticlesFallSpeed, 0.1f, 3.0f);
        this.worldParticlesLifetimeSeconds = PVPUtilsConfig.clampFloat(this.worldParticlesLifetimeSeconds, 1.0f, 10.0f);
        this.worldParticlesR = PVPUtilsConfig.clamp255(this.worldParticlesR);
        this.worldParticlesG = PVPUtilsConfig.clamp255(this.worldParticlesG);
        this.worldParticlesB = PVPUtilsConfig.clamp255(this.worldParticlesB);
    }

    private void normalizeFullBright() {
        if (this.fullBrightGammaOn < 100 || this.fullBrightGammaOn > 2000) {
            this.fullBrightGammaOn = 1500;
        }
        if (this.fullBrightGammaOff < 100 || this.fullBrightGammaOff > 2000) {
            this.fullBrightGammaOff = 100;
        }
    }

    private void normalizeZoom() {
        this.zoomLevel = Zoom.clampLevel(this.zoomLevel);
    }

    private void normalizeSoundController() {
        this.soundControllerExpOrbVolume = SoundController.clampUnit(this.soundControllerExpOrbVolume);
        this.soundControllerWitherVolume = SoundController.clampUnit(this.soundControllerWitherVolume);
        this.soundControllerTridentVolume = SoundController.clampUnit(this.soundControllerTridentVolume);
    }

    private void normalizeSpeedysColors() {
        this.trailR = PVPUtilsConfig.clamp255(this.trailR);
        this.trailG = PVPUtilsConfig.clamp255(this.trailG);
        this.trailB = PVPUtilsConfig.clamp255(this.trailB);
        this.chinaHatR = PVPUtilsConfig.clamp255(this.chinaHatR);
        this.chinaHatG = PVPUtilsConfig.clamp255(this.chinaHatG);
        this.chinaHatB = PVPUtilsConfig.clamp255(this.chinaHatB);
        this.jumpCircleR = PVPUtilsConfig.clamp255(this.jumpCircleR);
        this.jumpCircleG = PVPUtilsConfig.clamp255(this.jumpCircleG);
        this.jumpCircleB = PVPUtilsConfig.clamp255(this.jumpCircleB);
        this.targetEspMode = PVPUtilsConfig.clampInt(this.targetEspMode, 0, 2);
        this.targetEspR = PVPUtilsConfig.clamp255(this.targetEspR);
        this.targetEspG = PVPUtilsConfig.clamp255(this.targetEspG);
        this.targetEspB = PVPUtilsConfig.clamp255(this.targetEspB);
        this.blockOverlayR = PVPUtilsConfig.clamp255(this.blockOverlayR);
        this.blockOverlayG = PVPUtilsConfig.clamp255(this.blockOverlayG);
        this.blockOverlayB = PVPUtilsConfig.clamp255(this.blockOverlayB);
        this.blockOverlaySidesR = PVPUtilsConfig.clamp255(this.blockOverlaySidesR);
        this.blockOverlaySidesG = PVPUtilsConfig.clamp255(this.blockOverlaySidesG);
        this.blockOverlaySidesB = PVPUtilsConfig.clamp255(this.blockOverlaySidesB);
        this.hitColorR = PVPUtilsConfig.clamp255(this.hitColorR);
        this.hitColorG = PVPUtilsConfig.clamp255(this.hitColorG);
        this.hitColorB = PVPUtilsConfig.clamp255(this.hitColorB);
        this.customHitboxBoxR = PVPUtilsConfig.clamp255(this.customHitboxBoxR);
        this.customHitboxBoxG = PVPUtilsConfig.clamp255(this.customHitboxBoxG);
        this.customHitboxBoxB = PVPUtilsConfig.clamp255(this.customHitboxBoxB);
        this.customHitboxEyeR = PVPUtilsConfig.clamp255(this.customHitboxEyeR);
        this.customHitboxEyeG = PVPUtilsConfig.clamp255(this.customHitboxEyeG);
        this.customHitboxEyeB = PVPUtilsConfig.clamp255(this.customHitboxEyeB);
        this.customHitboxHeadR = PVPUtilsConfig.clamp255(this.customHitboxHeadR);
        this.customHitboxHeadG = PVPUtilsConfig.clamp255(this.customHitboxHeadG);
        this.customHitboxHeadB = PVPUtilsConfig.clamp255(this.customHitboxHeadB);
        this.customHitboxFillR = PVPUtilsConfig.clamp255(this.customHitboxFillR);
        this.customHitboxFillG = PVPUtilsConfig.clamp255(this.customHitboxFillG);
        this.customHitboxFillB = PVPUtilsConfig.clamp255(this.customHitboxFillB);
        this.nimbR = PVPUtilsConfig.clamp255(this.nimbR);
        this.nimbG = PVPUtilsConfig.clamp255(this.nimbG);
        this.nimbB = PVPUtilsConfig.clamp255(this.nimbB);
        this.trailColorStops = PVPUtilsConfig.clampInt(this.trailColorStops, 1, 3);
        this.trailR2 = PVPUtilsConfig.clamp255(this.trailR2);
        this.trailG2 = PVPUtilsConfig.clamp255(this.trailG2);
        this.trailB2 = PVPUtilsConfig.clamp255(this.trailB2);
        this.trailR3 = PVPUtilsConfig.clamp255(this.trailR3);
        this.trailG3 = PVPUtilsConfig.clamp255(this.trailG3);
        this.trailB3 = PVPUtilsConfig.clamp255(this.trailB3);
        this.chinaHatColorStops = PVPUtilsConfig.clampInt(this.chinaHatColorStops, 1, 3);
        this.chinaHatR2 = PVPUtilsConfig.clamp255(this.chinaHatR2);
        this.chinaHatG2 = PVPUtilsConfig.clamp255(this.chinaHatG2);
        this.chinaHatB2 = PVPUtilsConfig.clamp255(this.chinaHatB2);
        this.chinaHatR3 = PVPUtilsConfig.clamp255(this.chinaHatR3);
        this.chinaHatG3 = PVPUtilsConfig.clamp255(this.chinaHatG3);
        this.chinaHatB3 = PVPUtilsConfig.clamp255(this.chinaHatB3);
        this.blockOverlayColorStops = PVPUtilsConfig.clampInt(this.blockOverlayColorStops, 1, 3);
        this.blockOverlayR2 = PVPUtilsConfig.clamp255(this.blockOverlayR2);
        this.blockOverlayG2 = PVPUtilsConfig.clamp255(this.blockOverlayG2);
        this.blockOverlayB2 = PVPUtilsConfig.clamp255(this.blockOverlayB2);
        this.blockOverlayR3 = PVPUtilsConfig.clamp255(this.blockOverlayR3);
        this.blockOverlayG3 = PVPUtilsConfig.clamp255(this.blockOverlayG3);
        this.blockOverlayB3 = PVPUtilsConfig.clamp255(this.blockOverlayB3);
        this.nimbColorStops = PVPUtilsConfig.clampInt(this.nimbColorStops, 1, 3);
        this.nimbR2 = PVPUtilsConfig.clamp255(this.nimbR2);
        this.nimbG2 = PVPUtilsConfig.clamp255(this.nimbG2);
        this.nimbB2 = PVPUtilsConfig.clamp255(this.nimbB2);
        this.nimbR3 = PVPUtilsConfig.clamp255(this.nimbR3);
        this.nimbG3 = PVPUtilsConfig.clamp255(this.nimbG3);
        this.nimbB3 = PVPUtilsConfig.clamp255(this.nimbB3);
        this.predictionHitR = PVPUtilsConfig.clamp255(this.predictionHitR);
        this.predictionHitG = PVPUtilsConfig.clamp255(this.predictionHitG);
        this.predictionHitB = PVPUtilsConfig.clamp255(this.predictionHitB);
        this.predictionIdleR = PVPUtilsConfig.clamp255(this.predictionIdleR);
        this.predictionIdleG = PVPUtilsConfig.clamp255(this.predictionIdleG);
        this.predictionIdleB = PVPUtilsConfig.clamp255(this.predictionIdleB);
        // Не тянуть цвета из Predict — иначе после сохранения/загрузки FT «прыгает» и кажется, что настройки не держатся.
        if (this.ftHelperHitR == 0 && this.ftHelperHitG == 0 && this.ftHelperHitB == 0
                && this.ftHelperIdleR == 0 && this.ftHelperIdleG == 0 && this.ftHelperIdleB == 0) {
            this.ftHelperHitR = 60;
            this.ftHelperHitG = 255;
            this.ftHelperHitB = 60;
            this.ftHelperIdleR = 160;
            this.ftHelperIdleG = 160;
            this.ftHelperIdleB = 160;
        }
        this.ftHelperHitR = PVPUtilsConfig.clamp255(this.ftHelperHitR);
        this.ftHelperHitG = PVPUtilsConfig.clamp255(this.ftHelperHitG);
        this.ftHelperHitB = PVPUtilsConfig.clamp255(this.ftHelperHitB);
        this.ftHelperIdleR = PVPUtilsConfig.clamp255(this.ftHelperIdleR);
        this.ftHelperIdleG = PVPUtilsConfig.clamp255(this.ftHelperIdleG);
        this.ftHelperIdleB = PVPUtilsConfig.clamp255(this.ftHelperIdleB);
        this.eatHelperR = PVPUtilsConfig.clamp255(this.eatHelperR);
        this.eatHelperG = PVPUtilsConfig.clamp255(this.eatHelperG);
        this.eatHelperB = PVPUtilsConfig.clamp255(this.eatHelperB);
        this.invHudScale = PVPUtilsConfig.clampFloat(this.invHudScale, 0.01f, 1.0f);
        this.customHandOffsetX = PVPUtilsConfig.clampFloat(this.customHandOffsetX, -1.5f, 1.5f);
        this.customHandOffsetY = PVPUtilsConfig.clampFloat(this.customHandOffsetY, -1.5f, 1.5f);
        this.customHandOffsetZ = PVPUtilsConfig.clampFloat(this.customHandOffsetZ, -1.5f, 1.5f);
        this.customHandScale = PVPUtilsConfig.clampFloat(this.customHandScale, 0.2f, 2.0f);
        this.migrateCustomHandRotations();
        this.customHandPoseX = PVPUtilsConfig.clamp180(this.customHandPoseX);
        this.customHandPoseY = PVPUtilsConfig.clamp180(this.customHandPoseY);
        this.customHandPoseZ = PVPUtilsConfig.clamp180(this.customHandPoseZ);
        this.customHandSwingX = PVPUtilsConfig.clamp180(this.customHandSwingX);
        this.customHandSwingY = PVPUtilsConfig.clamp180(this.customHandSwingY);
        this.customHandSwingZ = PVPUtilsConfig.clamp180(this.customHandSwingZ);
        this.customHandPosRotation = 0;
        this.customHandHitRotation = 0;
    }

    private void migrateCustomHandRotations() {
        if (this.customHandPoseX == 0 && this.customHandPoseY == 0 && this.customHandPoseZ == 0 && this.customHandPosRotation != 0) {
            this.customHandPoseZ = this.customHandPosRotation;
        }
        if (this.customHandSwingX == 0 && this.customHandSwingY == 0 && this.customHandSwingZ == 0 && this.customHandHitRotation != 0) {
            this.customHandSwingX = this.customHandHitRotation;
        }
    }

    private static int clamp180(int v) {
        return Math.max(-180, Math.min(180, v));
    }

    private void normalizeFriendNick() {
        if (this.friendNickname == null) {
            this.friendNickname = "";
        }
    }

    private void normalizeFeatureStrings() {
        if (this.lockSlotsCsv == null) {
            this.lockSlotsCsv = "";
        }
        if (this.itemSwapItem1Name == null) {
            this.itemSwapItem1Name = "";
        }
        if (this.itemSwapItem2Name == null) {
            this.itemSwapItem2Name = "";
        }
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clampFloat(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, (CharSequence)GSON.toJson((Object)this), new OpenOption[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

