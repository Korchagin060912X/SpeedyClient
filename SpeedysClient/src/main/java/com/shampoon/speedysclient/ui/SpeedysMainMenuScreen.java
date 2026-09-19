/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.RenderPipelines
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package com.shampoon.speedysclient.ui;

import com.pearllandingpredictor.PearlLandingConfig;
import com.pearllandingpredictor.PearlLandingHooks;
import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.integration.ItemScrollerIntegration;
import com.shampoon.speedysclient.config.SpeedysWatermarkConfig;
import com.shampoon.speedysclient.hud.SpeedysWatermark;
import com.shampoon.speedysclient.ui.SpeedysConfigScreen;
import com.shampoon.speedysclient.ui.SpeedysSettingsPopupScreen;
import com.shampoon.speedysclient.ui.WaymarksScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import me.shampoon.attackindicator.ModConfig;
import me.shampoon.cooldownitem.CooldownItemConfig;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.artem.durabilitytint.DurabilityTintConfig;

public final class SpeedysMainMenuScreen
extends Screen {
    private static final int PANEL_BG = -805306368;
    private static final int PANEL_OUTLINE = -14996918;
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 224;
    private static final int BTN_H = 16;
    private static final int ROW = 22;
    private static final Identifier MENU_SETTINGS_ICON = Identifier.of((String)"speedysclient", (String)"textures/gui/menu_settings.png");
    private static final Identifier MENU_CONFIGS_ICON = Identifier.of((String)"speedysclient", (String)"textures/gui/menu_configs.png");
    private static final Identifier MENU_HOME_ICON = Identifier.of((String)"speedysclient", (String)"textures/gui/menu_home.png");
    private static final Identifier MENU_WAYMARKS_ICON = Identifier.of((String)"speedysclient", (String)"textures/gui/id.png");
    private final Screen parent;
    private int tab = 0;
    private int utilsScroll = 0;
    private int visualsScroll = 0;
    private final List<ToggleEntry> toggles = new ArrayList<ToggleEntry>();
    private final List<MenuButtonEntry> menuButtons = new ArrayList<MenuButtonEntry>();

    public SpeedysMainMenuScreen(Screen parent) {
        super((Text)Text.literal((String)"Speedys Client"));
        this.parent = parent;
    }

    protected void init() {
        this.clearChildren();
        this.toggles.clear();
        this.menuButtons.clear();
        int left = this.panelLeft();
        int top = this.panelTop();
        int cx = this.width / 2;
        this.addMenuButton(left + 10, top + 8, 112, 16, "Huds", this.tab == 0, () -> {
            this.tab = 0;
            this.utilsScroll = 0;
            this.visualsScroll = 0;
            this.init();
        });
        this.addMenuButton(left + 128, top + 8, 112, 16, "Utils", this.tab == 1, () -> {
            this.tab = 1;
            this.utilsScroll = 0;
            this.visualsScroll = 0;
            this.init();
        });
        this.addMenuButton(left + 246, top + 8, 112, 16, "Visuals", this.tab == 2, () -> {
            this.tab = 2;
            this.utilsScroll = 0;
            this.visualsScroll = 0;
            this.init();
        });
        int bottomY = top + 224 - 22;
        this.addMenuButton(left + 10, bottomY, 16, 16, "", false, MENU_SETTINGS_ICON, () -> {
            if (this.client != null) {
                this.client.setScreen((Screen)new SpeedysSettingsPopupScreen(this));
            }
        });
        this.addMenuButton(left + 30, bottomY, 16, 16, "", false, MENU_CONFIGS_ICON, () -> {
            if (this.client != null) {
                this.client.setScreen((Screen)new SpeedysConfigScreen(this));
            }
        });
        this.addMenuButton(left + 50, bottomY, 16, 16, "", false, MENU_HOME_ICON, () -> {
            this.tab = 1;
            this.init();
        });
        this.addMenuButton(left + 70, bottomY, 16, 16, "", false, MENU_WAYMARKS_ICON, () -> {
            if (this.client != null) {
                this.client.setScreen((Screen)new WaymarksScreen(this));
            }
        });
        int logsW = Math.max(44, this.textRenderer.getWidth("Logs") + 12);
        int logsH = 16;
        this.addMenuButton(this.width - logsW - 8, 8, logsW, logsH, "Logs", false, () -> {
            if (this.client != null) {
                this.client.setScreen((Screen)new SpeedysChangelogScreen(this));
            }
        });
        if (this.tab == 0) {
            this.initHuds(cx, top + 30);
        } else if (this.tab == 1) {
            this.initUtils(cx, top + 30 - this.utilsScroll);
        } else {
            this.initVisuals(cx, top + 30 - this.visualsScroll);
        }
        this.addMenuButton(cx - 85, top + 224 - 22, 170, 16, "\u0413\u043e\u0442\u043e\u0432\u043e", false, this::close);
    }

    private void initHuds(int cx, int y) {
        int sidePadding = 10;
        int rowW = 400 - sidePadding * 2;
        int x = this.panelLeft() + sidePadding;
        if (this.visibleY(y)) {
            this.addToggle(x, y, rowW, "Watermark", () -> SpeedysWatermarkConfig.get().visible, () -> {
                SpeedysWatermarkConfig.get().visible = !SpeedysWatermarkConfig.get().visible;
                SpeedysWatermarkConfig.save();
            }, () -> this.openFeatureSettings("Watermark"));
        }
        if (this.visibleY(y += 22)) {
            this.addToggle(x, y, rowW, "Target HUD", () -> PVPUtils.CONFIG.targetHudEnabled, () -> {
                PVPUtils.CONFIG.targetHudEnabled = !PVPUtils.CONFIG.targetHudEnabled;
                PVPUtils.CONFIG.save();
            }, () -> this.openFeatureSettings("Target HUD"));
        }
        if (this.visibleY(y += 22)) {
            this.addToggle(x, y, rowW, "Armor HUD", () -> PVPUtils.CONFIG.armorHudEnabled, () -> {
                PVPUtils.CONFIG.armorHudEnabled = !PVPUtils.CONFIG.armorHudEnabled;
                PVPUtils.CONFIG.save();
            }, () -> this.openFeatureSettings("Armor HUD"));
        }
        if (this.visibleY(y += 22)) {
            this.addToggle(x, y, rowW, "Effect Timer", () -> PVPUtils.CONFIG.effectTimerEnabled, () -> {
                PVPUtils.CONFIG.effectTimerEnabled = !PVPUtils.CONFIG.effectTimerEnabled;
                PVPUtils.CONFIG.save();
            }, () -> this.openFeatureSettings("Effect Timer"));
        }
        if (this.visibleY(y += 22)) {
            this.addToggle(x, y, rowW, "Inv HUD", () -> PVPUtils.CONFIG.invHudEnabled, () -> {
                PVPUtils.CONFIG.invHudEnabled = !PVPUtils.CONFIG.invHudEnabled;
                PVPUtils.CONFIG.save();
            }, () -> this.openFeatureSettings("Inv HUD"));
        }
    }

    private void initUtils(int cx, int y) {
        ModConfig ai = ModConfig.getConfig();
        int i = 0;
        i = this.gridToggle(i, cx, this.utilsScroll, "Attack Indicator", () -> ai.enabled, () -> {
            ai.enabled = !ai.enabled;
            ai.save();
        }, () -> this.openFeatureSettings("Attack Indicator"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Cooldown Items", () -> CooldownItemConfig.get().enabled, () -> {
            CooldownItemConfig.get().enabled = !CooldownItemConfig.get().enabled;
            CooldownItemConfig.save();
        }, () -> this.openFeatureSettings("Cooldown Items"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Durability Armor Tint", DurabilityTintConfig::isEnabled, () -> {
            DurabilityTintConfig.setEnabled(!DurabilityTintConfig.isEnabled());
            DurabilityTintConfig.save();
        }, () -> this.openFeatureSettings("Durability Armor Tint"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Landing Predictions", PearlLandingHooks::isEnabled, () -> PearlLandingConfig.setEnabledPersist(!PearlLandingHooks.isEnabled()), () -> this.openFeatureSettings("Landing Predictions"));
        i = this.gridToggle(i, cx, this.utilsScroll, "FT Helper", () -> PVPUtils.CONFIG.ftHelperEnabled, () -> {
            PVPUtils.CONFIG.ftHelperEnabled = !PVPUtils.CONFIG.ftHelperEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("FT Helper"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Totem Tracker", () -> PVPUtils.CONFIG.totemTrackerEnabled, () -> {
            PVPUtils.CONFIG.totemTrackerEnabled = !PVPUtils.CONFIG.totemTrackerEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Totem Tracker"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Lock slots", () -> PVPUtils.CONFIG.lockSlotsEnabled, () -> {
            PVPUtils.CONFIG.lockSlotsEnabled = !PVPUtils.CONFIG.lockSlotsEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Lock slots"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Item Swap", () -> PVPUtils.CONFIG.itemSwapEnabled, () -> {
            PVPUtils.CONFIG.itemSwapEnabled = !PVPUtils.CONFIG.itemSwapEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Item Swap"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Auto Sprint", () -> PVPUtils.CONFIG.autoSprintEnabled, () -> {
            PVPUtils.CONFIG.autoSprintEnabled = !PVPUtils.CONFIG.autoSprintEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Auto Sprint"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Item Scroller", () -> PVPUtils.CONFIG.itemScrollerEnabled, () -> {
            PVPUtils.CONFIG.itemScrollerEnabled = !PVPUtils.CONFIG.itemScrollerEnabled;
            ItemScrollerIntegration.applyConfigToItemScroller();
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Item Scroller"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Item Picker", () -> PVPUtils.CONFIG.itemPickerEnabled, () -> {
            PVPUtils.CONFIG.itemPickerEnabled = !PVPUtils.CONFIG.itemPickerEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Item Picker"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Trap Timer", () -> PVPUtils.CONFIG.trapTimerEnabled, () -> {
            PVPUtils.CONFIG.trapTimerEnabled = !PVPUtils.CONFIG.trapTimerEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Trap Timer"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Eat Helper", () -> PVPUtils.CONFIG.eatHelperEnabled, () -> {
            PVPUtils.CONFIG.eatHelperEnabled = !PVPUtils.CONFIG.eatHelperEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Eat Helper"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Potion Highlighter", () -> PVPUtils.CONFIG.potionHighlighterEnabled, () -> {
            PVPUtils.CONFIG.potionHighlighterEnabled = !PVPUtils.CONFIG.potionHighlighterEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Potion Highlighter"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Anti Invis", () -> PVPUtils.CONFIG.antiInvisEnabled, () -> {
            PVPUtils.CONFIG.antiInvisEnabled = !PVPUtils.CONFIG.antiInvisEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Anti Invis"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Friend System", () -> PVPUtils.CONFIG.friendSystemEnabled, () -> {
            PVPUtils.CONFIG.friendSystemEnabled = !PVPUtils.CONFIG.friendSystemEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Friend System"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Auto Potion", () -> PVPUtils.CONFIG.autoPotionEnabled, () -> {
            PVPUtils.CONFIG.autoPotionEnabled = !PVPUtils.CONFIG.autoPotionEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Auto Potion"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Fast XP", () -> PVPUtils.CONFIG.fastXPEnabled, () -> {
            PVPUtils.CONFIG.fastXPEnabled = !PVPUtils.CONFIG.fastXPEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Fast XP"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Zoom", () -> PVPUtils.CONFIG.zoomEnabled, () -> {
            PVPUtils.CONFIG.zoomEnabled = !PVPUtils.CONFIG.zoomEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Zoom"));
        i = this.gridToggle(i, cx, this.utilsScroll, "NoFluid", () -> PVPUtils.CONFIG.noFluidEnabled, () -> {
            PVPUtils.CONFIG.noFluidEnabled = !PVPUtils.CONFIG.noFluidEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("NoFluid"));
        i = this.gridToggle(i, cx, this.utilsScroll, "Sound Control", () -> PVPUtils.CONFIG.soundControllerEnabled, () -> {
            PVPUtils.CONFIG.soundControllerEnabled = !PVPUtils.CONFIG.soundControllerEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Sound Control"));
        i = this.gridToggle(i, cx, this.utilsScroll, "RemoveMisc", () -> PVPUtils.CONFIG.removeMiscEnabled, () -> {
            PVPUtils.CONFIG.removeMiscEnabled = !PVPUtils.CONFIG.removeMiscEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("RemoveMisc"));
        i = this.gridToggle(i, cx, this.utilsScroll, "  \u2514 \u041e\u0433\u043e\u043d\u044c", () -> PVPUtils.CONFIG.removeMiscHideFire, () -> {
            PVPUtils.CONFIG.removeMiscHideFire = !PVPUtils.CONFIG.removeMiscHideFire;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("  \u2514 \u041e\u0433\u043e\u043d\u044c"));
        i = this.gridToggle(i, cx, this.utilsScroll, "  \u2514 \u0422\u043e\u0442\u0435\u043c", () -> PVPUtils.CONFIG.removeMiscHideTotemAnimation, () -> {
            PVPUtils.CONFIG.removeMiscHideTotemAnimation = !PVPUtils.CONFIG.removeMiscHideTotemAnimation;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("  \u2514 \u0422\u043e\u0442\u0435\u043c"));
        i = this.gridToggle(i, cx, this.utilsScroll, "  \u2514 \u0421\u043b\u0435\u043f\u043e\u0442\u0430", () -> PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay, () -> {
            PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay = !PVPUtils.CONFIG.removeMiscHideBlindnessDarknessOverlay;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("  \u2514 \u0421\u043b\u0435\u043f\u043e\u0442\u0430"));
        this.gridToggle(i, cx, this.utilsScroll, "  \u2514 \u041f\u043e\u0433\u043e\u0434\u0430", () -> PVPUtils.CONFIG.removeMiscHidePrecipitation, () -> {
            PVPUtils.CONFIG.removeMiscHidePrecipitation = !PVPUtils.CONFIG.removeMiscHidePrecipitation;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("  \u2514 \u041f\u043e\u0433\u043e\u0434\u0430"));
    }

    private void initVisuals(int cx, int y) {
        int i = 0;
        i = this.gridToggle(i, cx, this.visualsScroll, "Block Overlay", () -> PVPUtils.CONFIG.blockOverlayEnabled, () -> {
            PVPUtils.CONFIG.blockOverlayEnabled = !PVPUtils.CONFIG.blockOverlayEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Block Overlay"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Hit Color", () -> PVPUtils.CONFIG.hitColorEnabled, () -> {
            PVPUtils.CONFIG.hitColorEnabled = !PVPUtils.CONFIG.hitColorEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Hit Color"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Custom Hitboxes", () -> PVPUtils.CONFIG.customHitboxesEnabled, () -> {
            PVPUtils.CONFIG.customHitboxesEnabled = !PVPUtils.CONFIG.customHitboxesEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Custom Hitboxes"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Trail", () -> PVPUtils.CONFIG.trailEnabled, () -> {
            PVPUtils.CONFIG.trailEnabled = !PVPUtils.CONFIG.trailEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Trail"));
        i = this.gridToggle(i, cx, this.visualsScroll, "China Hat", () -> PVPUtils.CONFIG.chinaHatEnabled, () -> {
            PVPUtils.CONFIG.chinaHatEnabled = !PVPUtils.CONFIG.chinaHatEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("China Hat"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Jump Circle", () -> PVPUtils.CONFIG.jumpCircleEnabled, () -> {
            PVPUtils.CONFIG.jumpCircleEnabled = !PVPUtils.CONFIG.jumpCircleEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Jump Circle"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Target ESP", () -> PVPUtils.CONFIG.targetEspEnabled, () -> {
            PVPUtils.CONFIG.targetEspEnabled = !PVPUtils.CONFIG.targetEspEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Target ESP"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Nimb", () -> PVPUtils.CONFIG.nimbEnabled, () -> {
            PVPUtils.CONFIG.nimbEnabled = !PVPUtils.CONFIG.nimbEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Nimb"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Custom Hand", () -> PVPUtils.CONFIG.customHandEnabled, () -> {
            PVPUtils.CONFIG.customHandEnabled = !PVPUtils.CONFIG.customHandEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Custom Hand"));
        i = this.gridToggle(i, cx, this.visualsScroll, "World Particles", () -> PVPUtils.CONFIG.worldParticlesEnabled, () -> {
            PVPUtils.CONFIG.worldParticlesEnabled = !PVPUtils.CONFIG.worldParticlesEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("World Particles"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Custom Ratio", () -> PVPUtils.CONFIG.customRatioEnabled, () -> {
            PVPUtils.CONFIG.customRatioEnabled = !PVPUtils.CONFIG.customRatioEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Custom Ratio"));
        i = this.gridToggle(i, cx, this.visualsScroll, "Custom World", () -> PVPUtils.CONFIG.customWorldEnabled, () -> {
            PVPUtils.CONFIG.customWorldEnabled = !PVPUtils.CONFIG.customWorldEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Custom World"));
        this.gridToggle(i, cx, this.visualsScroll, "Fullbright", () -> PVPUtils.CONFIG.fullBrightEnabled, () -> {
            PVPUtils.CONFIG.fullBrightEnabled = !PVPUtils.CONFIG.fullBrightEnabled;
            PVPUtils.CONFIG.save();
        }, () -> this.openFeatureSettings("Fullbright"));
    }

    private int gridToggle(int index, int cx, int contentScroll, String name, BooleanSupplier state, Runnable flip, Runnable openSettings) {
        int sidePadding = 10;
        int rowW = 400 - sidePadding * 2;
        int row = index;
        int x = this.panelLeft() + sidePadding;
        int y = this.panelTop() + 30 + row * 22 - contentScroll;
        if (this.visibleY(y)) {
            this.addToggle(x, y, rowW, name, state, flip, openSettings);
        }
        return index + 1;
    }

    private void addToggle(int x, int y, int w, String name, BooleanSupplier state, Runnable flip, Runnable openSettings) {
        this.toggles.add(new ToggleEntry(x, y, w, 16, name, state, flip, openSettings));
    }

    private void addMenuButton(int x, int y, int w, int h, String label, boolean active, Runnable onClick) {
        this.menuButtons.add(new MenuButtonEntry(x, y, w, h, label, active, null, onClick));
    }

    private void addMenuButton(int x, int y, int w, int h, String label, boolean active, Identifier icon, Runnable onClick) {
        this.menuButtons.add(new MenuButtonEntry(x, y, w, h, label, active, icon, onClick));
    }

    private int rowToggle(int cx, int y, String name, BooleanSupplier state, Runnable flip) {
        if (this.visibleY(y)) {
            this.addToggle(cx - 90, y, 180, name, state, flip, () -> this.openFeatureSettings(name));
        }
        return y + 22;
    }

    private void openFeatureSettings(String featureName) {
        if (this.client != null) {
            this.client.setScreen((Screen)new SpeedysSettingsPopupScreen(this));
        }
    }

    private boolean visibleY(int y) {
        return y >= this.panelTop() + 30 && y < this.panelTop() + 224 - 28;
    }

    private int panelLeft() {
        return this.width / 2 - 200;
    }

    private int panelTop() {
        return this.height / 2 - 112;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        String string;
        context.fill(0, 0, this.width, this.height, -1610612736);
        this.drawRoundedRect(context, this.panelLeft(), this.panelTop(), this.panelLeft() + 400, this.panelTop() + 224, -805306368);
        this.drawRoundedOutline(context, this.panelLeft(), this.panelTop(), this.panelLeft() + 400, this.panelTop() + 224, -14996918);
        int cx = this.width / 2;
        int titleY = this.panelTop() - 14;
        int iconS = 16;
        float titleScale = 1.25f;
        String titleLeft = "Speedys";
        String titleRight = " Client";
        int titleW = this.textRenderer.getWidth(titleLeft) + this.textRenderer.getWidth(titleRight);
        int scaledTitleW = (int)Math.ceil((float)titleW * titleScale);
        int totalW = iconS + 6 + scaledTitleW;
        int startX = cx - totalW / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SpeedysWatermark.BOOT_ICON, startX, titleY - 1, 0.0f, 0.0f, iconS, iconS, iconS, iconS);
        int textX = startX + iconS + 6;
        int textY = titleY + 2;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(titleScale, titleScale);
        int sx = (int)((float)textX / titleScale);
        int sy = (int)((float)textY / titleScale);
        context.drawText(this.textRenderer, titleLeft, sx, sy, -14996918, false);
        context.drawText(this.textRenderer, titleRight, sx + this.textRenderer.getWidth(titleLeft), sy, -1, false);
        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, deltaTicks);
        for (MenuButtonEntry menuButtonEntry : this.menuButtons) {
            this.drawMenuButton(context, menuButtonEntry);
        }
        ToggleEntry hovered = null;
        for (ToggleEntry entry : this.toggles) {
            this.drawToggle(context, entry);
            if (mouseX < entry.x || mouseX > entry.x + entry.w || mouseY < entry.y || mouseY > entry.y + entry.h) continue;
            hovered = entry;
        }
        if (hovered != null && (string = this.descriptionFor(hovered.name)) != null && !string.isEmpty()) {
            this.drawDescriptionTooltip(context, mouseX, mouseY, hovered.name, string);
        }
    }

    private void drawToggle(DrawContext context, ToggleEntry e) {
        this.drawRoundedRect(context, e.x, e.y, e.x + e.w, e.y + e.h, -15592419);
        this.drawRoundedOutline(context, e.x, e.y, e.x + e.w, e.y + e.h, -14534042);
        context.drawText(this.textRenderer, e.name, e.x + 6, e.y + 4, -1, false);
        int sw = 28;
        int sh = 10;
        int sx = e.x + e.w - sw - 5;
        int sy = e.y + (e.h - sh) / 2;
        boolean on = e.state.getAsBoolean();
        this.drawRoundedRect(context, sx, sy, sx + sw, sy + sh, on ? -9545473 : -11709847);
        int kx = on ? sx + sw - sh + 1 : sx + 1;
        this.drawRoundedRect(context, kx, sy + 1, kx + sh - 2, sy + sh - 1, -1184001);
    }

    private void drawMenuButton(DrawContext context, MenuButtonEntry button) {
        int bg = button.active ? -15063228 : -15592419;
        int outline = button.active ? -11901272 : -14534042;
        int textColor = -1;
        this.drawRoundedRect(context, button.x, button.y, button.x + button.w, button.y + button.h, bg);
        this.drawRoundedOutline(context, button.x, button.y, button.x + button.w, button.y + button.h, outline);
        if (button.icon != null) {
            int size = Math.min(button.w - 4, button.h - 4);
            int ix = button.x + (button.w - size) / 2;
            int iy = button.y + (button.h - size) / 2;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, button.icon, ix, iy, 0.0f, 0.0f, size, size, size, size);
        } else {
            int textX = button.x + (button.w - this.textRenderer.getWidth(button.label)) / 2;
            context.drawText(this.textRenderer, button.label, textX, button.y + 4, textColor, false);
        }
    }

    private void drawRoundedRect(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, b, color);
        context.fill(l, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 3, color);
        context.fill(r - 3, t + 1, r - 1, t + 3, color);
        context.fill(l + 1, b - 3, l + 3, b - 1, color);
        context.fill(r - 3, b - 3, r - 1, b - 1, color);
    }

    private void drawRoundedOutline(DrawContext context, int l, int t, int r, int b, int color) {
        context.fill(l + 3, t, r - 3, t + 1, color);
        context.fill(l + 3, b - 1, r - 3, b, color);
        context.fill(l, t + 3, l + 1, b - 3, color);
        context.fill(r - 1, t + 3, r, b - 3, color);
        context.fill(l + 1, t + 1, l + 3, t + 2, color);
        context.fill(r - 3, t + 1, r - 1, t + 2, color);
        context.fill(l + 1, b - 2, l + 3, b - 1, color);
        context.fill(r - 3, b - 2, r - 1, b - 1, color);
    }

    private String descriptionFor(String name) {
        String normalized;
        return switch (normalized = name.trim()) {
            case "Watermark" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u043a\u043b/\u0432\u044b\u043a\u043b \u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0443";
            case "Armor HUD" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u0431\u0440\u043e\u043d\u044e \u0438 \u0435\u0435 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c";
            case "Target HUD" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0445\u0430\u0434 \u0441 \u043d\u0438\u043a\u043e\u043c, HP \u0438 \u0442\u0435\u0433\u043e\u043c \u0438\u0437 \u0442\u0430\u0431\u0430; \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u043c\u043e\u0436\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0441\u0442\u0440\u043e\u043a\u0443 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u0432 \u043b\u0435\u0432\u043e\u0439 \u0440\u0443\u043a\u0435 (\u0431\u0435\u0437 \u0438\u043a\u043e\u043d\u043a\u0438)";
            case "Effect Timer" -> "\u0422\u0430\u0439\u043c\u0435\u0440\u044b \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432: \u0443 \u0438\u043a\u043e\u043d\u043e\u043a \u0438\u043b\u0438 \u043f\u0430\u043d\u0435\u043b\u044c \u0441\u043f\u0438\u0441\u043a\u0430 (\u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438). \u041f\u0435\u0440\u0435\u0442\u0430\u0441\u043a\u0438\u0432\u0430\u043d\u0438\u0435 \u0432 \u0447\u0430\u0442\u0435.";
            case "Inv HUD" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 3x9 \u044f\u0447\u0435\u0439\u043a\u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f \u0440\u044f\u0434\u043e\u043c \u0441 \u0445\u043e\u0442\u0431\u0430\u0440\u043e\u043c";
            case "Attack Indicator" -> "\u041a\u0440\u0435\u0441\u0442\u0438\u043a \u043c\u0435\u043d\u044f\u0435\u0442 \u0446\u0432\u0435\u0442 \u043a\u043e\u0433\u0434\u0430 \u043c\u043e\u0436\u043d\u043e \u0443\u0434\u0430\u0440\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430";
            case "Hit Color" -> "\u041f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435 \u043c\u0435\u043d\u044f\u0435\u0442 \u0446\u0432\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430";
            case "Durability Armor Tint" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u0441\u043a\u043e\u043b\u044c\u043a\u043e \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438 \u0443 \u0431\u0440\u043e\u043d\u0438 \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0430";
            case "Auto Sprint" -> "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u043e\u043c \u0441\u043f\u0440\u0438\u043d\u0442\u0438\u0442";
            case "Item Picker" -> "\u0412\u044b\u0432\u043e\u0434\u0438\u0442 \u0432 \u0447\u0430\u0442 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043e\u0431 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0435 \u043a\u043e\u0442\u043e\u0440\u044b\u0439 \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043d";
            case "Fullbright" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u0432 \u0442\u0435\u043c\u043d\u043e\u0442\u0435";
            case "NoFluid" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u043f\u043e\u0434 \u0432\u043e\u0434\u043e\u0439/\u043b\u0430\u0432\u043e\u0439";
            case "RemoveMisc", "\u2514 \u041e\u0433\u043e\u043d\u044c", "\u2514 \u0422\u043e\u0442\u0435\u043c", "\u2514 \u0421\u043b\u0435\u043f\u043e\u0442\u0430", "\u2514 \u041f\u043e\u0433\u043e\u0434\u0430" -> "\u041c\u043e\u0436\u043d\u043e \u0443\u0431\u0440\u0430\u0442\u044c \u043d\u0435\u043d\u0443\u0436\u043d\u044b\u0435 \u0432\u0435\u0449\u0438 \u0441 \u044d\u043a\u0440\u0430\u043d\u0430";
            case "Cooldown Items" -> "\u041a\u0414 \u043d\u0430 \u0441\u043b\u043e\u0442\u0430\u0445 \u0438\u043b\u0438 \u043f\u0430\u043d\u0435\u043b\u044c HUD (\u0440\u0435\u0436\u0438\u043c \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445)";
            case "Landing Predictions" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u043a\u0443\u0434\u0430 \u043f\u0440\u0438\u0437\u0435\u043c\u043b\u0438\u0442\u0441\u044f \u043f\u0435\u0440\u043a\u0430 \u0438 \u0442.\u0434";
            case "Item Scroller" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0431\u044b\u0441\u0442\u0440\u043e \u0443\u0431\u0438\u0440\u0430\u0442\u044c \u0438 \u0434\u043e\u0441\u0442\u0430\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b";
            case "Sound Control" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043c\u0435\u043d\u044c\u0448\u0430\u0442\u044c \u0433\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c \u043d\u0435\u043d\u0443\u0436\u043d\u044b\u0445 \u0432\u0435\u0449\u0435\u0439";
            case "Block Overlay" -> "\u0414\u0435\u043b\u0430\u0435\u0442 \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u0443\u044e \u043e\u0431\u0432\u043e\u0434\u043a\u0443 \u0431\u043b\u043e\u043a\u0430. \u0414\u043e 3 \u0446\u0432\u0435\u0442\u043e\u0432 \u0438 \u043f\u0435\u0440\u0435\u043b\u0438\u0432 \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 (\u043b\u0438\u0446\u043e \u0431\u043b\u043e\u043a\u0430)";
            case "Custom Hitboxes" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043f\u043e\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0435 \u0425\u0438\u0442\u0431\u043e\u043a\u0441\u044b";
            case "Trail" -> "\u041a\u043e\u0433\u0434\u0430 \u0442\u044b \u0431\u0435\u0436\u0438\u0448\u044c \u043f\u043e\u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0422\u0440\u044d\u0438\u043b. \u0414\u043e 3 \u0446\u0432\u0435\u0442\u043e\u0432 \u0438 \u0430\u043d\u0438\u043c. \u043f\u0435\u0440\u0435\u043b\u0438\u0432 \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445";
            case "China Hat" -> "\u041d\u0430\u0434 \u0433\u043e\u043b\u043e\u0432\u043e\u0439 \u043f\u043e\u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u043a\u0438\u0442\u0430\u0439\u0441\u043a\u0430\u044f \u0448\u043b\u044f\u043f\u043a\u0430. \u0414\u043e 3 \u0446\u0432\u0435\u0442\u043e\u0432 \u0438 \u043f\u0435\u0440\u0435\u043b\u0438\u0432 \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445";
            case "Jump Circle" -> "\u041a\u043e\u0433\u0434\u0430 \u0442\u044b \u043f\u0440\u044b\u0433\u0430\u0435\u0448\u044c \u043f\u043e\u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u041a\u0440\u0443\u0436\u043e\u043a";
            case "Target ESP" -> "\u041f\u043e\u0441\u043b\u0435 \u0443\u0434\u0430\u0440\u0430 \u043f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0446\u0435\u043b\u0438: \u043a\u0440\u0443\u0433, \u0441\u0444\u0435\u0440\u044b \u0438\u043b\u0438 \u0447\u0430\u0441\u0442\u0438\u0446\u044b (\u0442\u0438\u043f \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445)";
            case "Nimb" -> "\u041d\u0430\u0434 \u0433\u043e\u043b\u043e\u0432\u043e\u0439 \u0438\u0433\u0440\u043e\u043a\u0430 \u043f\u043e\u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u043d\u0438\u043c\u0431. \u0414\u043e 3 \u0446\u0432\u0435\u0442\u043e\u0432 \u0438 \u043f\u0435\u0440\u0435\u043b\u0438\u0432 \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445";
            case "Custom Hand" -> "\u041f\u043e\u0437\u0438\u0446\u0438\u044f, \u043c\u0430\u0441\u0448\u0442\u0430\u0431, \u043f\u043e\u0437\u0430 XYZ \u0438 \u0441\u0432\u0438\u043d\u0433. \u041e\u043f\u0446\u0438\u044f \u00ab\u0424\u0438\u043a\u0441. \u043f\u043e\u0437\u0430\u00bb: \u043c\u0435\u0447 \u043d\u0435 \u0437\u0430\u0432\u0430\u043b\u0438\u0432\u0430\u0435\u0442\u0441\u044f \u043e\u0442 \u0432\u0437\u0433\u043b\u044f\u0434\u0430 \u0438 \u0441\u043c\u0435\u043d\u044b \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430, \u0434\u0432\u0438\u0436\u0435\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u0438\u043d\u0433\u043e\u043c (\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438).";
            case "World Particles" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043f\u043e\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u043f\u0430\u0440\u0442\u0438\u043a\u043b\u044b \u0432 \u043c\u0438\u0440\u0435";
            case "Trap Timer" -> "\u041a\u043e\u0433\u0434\u0430 \u044e\u0437\u0430\u0435\u0448\u044c \u0422\u0440\u0430\u043f\u0443 \u043f\u043e\u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0442\u0430\u0439\u043c\u0435\u0440 \u0434\u043e \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0422\u0440\u0430\u043f\u044b. \u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435: \u043a\u043b\u0430\u0441\u0441\u0438\u043a\u0430 (\u0441\u0442\u0440\u043e\u043a\u0430 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f) \u0438\u043b\u0438 \u043c\u0438\u043d\u0438-HUD \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445.";
            case "Eat Helper" -> "\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u043b\u0443\u0447\u0448\u0443\u044e \u0435\u0434\u0443 \u0438 \u0437\u0435\u043b\u044c\u044f \u043c\u0433\u043d\u043e\u0432\u0435\u043d\u043d\u043e\u0433\u043e \u0438\u0441\u0446\u0435\u043b\u0435\u043d\u0438\u044f I/II \u043f\u0440\u0438 \u043d\u0438\u0437\u043a\u043e\u043c HP \u0438 \u0433\u043e\u043b\u043e\u0434\u0435 (\u0445\u043e\u0442\u0431\u0430\u0440 \u0438 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c)";
            case "Potion Highlighter" -> "\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0432\u0437\u0440\u044b\u0432\u043d\u044b\u0435 \u0437\u0435\u043b\u044c\u044f (splash) \u043f\u043e \u0434\u0435\u0431\u0430\u0444\u0444\u0430\u043c \u0438 \u0431\u0430\u0444\u0444\u0430\u043c";
            case "Anti Invis" -> "\u0415\u0441\u043b\u0438 \u043d\u0430 \u0438\u0433\u0440\u043e\u043a\u0435 \u0435\u0441\u0442\u044c \u0445\u043e\u0442\u044f \u0431\u044b \u043e\u0434\u0438\u043d \u044d\u043b\u0435\u043c\u0435\u043d\u0442 \u043e\u0434\u0435\u0436\u0434\u044b \u0438\u043b\u0438 \u0432 \u0440\u0443\u043a\u0430\u0445 \u0447\u0442\u043e-\u0442\u043e, \u0442\u044b \u0435\u0433\u043e \u0432\u0438\u0434\u0438\u0448\u044c";
            case "Friend System" -> "\u041d\u0435 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0431\u0438\u0442\u044c \u0442\u0438\u043c\u0435\u0439\u0442\u043e\u0432; \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445 \u0441\u043f\u0438\u0441\u043e\u043a \u043d\u0438\u043a\u043e\u0432 \u0447\u0435\u0440\u0435\u0437 \u0437\u0430\u043f\u044f\u0442\u0443\u044e (\u041d\u0438\u043a1, \u041d\u0438\u043a2)";
            case "Custom Ratio" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043f\u043e\u0441\u0442\u0430\u0432\u0438\u0442\u044c \u043a\u0430\u0441\u0442\u043e\u043c\u043d\u043e\u0435 \u0440\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u0438\u0435";
            case "Custom World" -> "\u0426\u0432\u0435\u0442 \u043d\u0435\u0431\u0430/\u0442\u0443\u043c\u0430\u043d\u0430; \u043d\u0435\u0431\u043e: \u043e\u0431\u043b\u0430\u043a\u0430 \u0438\u043b\u0438 \u043a\u043e\u043c\u0435\u0442\u044b (\u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438)";
            case "Auto Potion" -> "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u043e\u043c \u043f\u044c\u0451\u0442 \u0437\u0435\u043b\u044c\u0435 \u0432 \u0440\u0443\u043a\u0435";
            case "FT Helper" -> "\u041f\u043e\u043c\u043e\u0433\u0430\u0435\u0442 \u0441\u043e \u0441\u043f\u0435\u0446. \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430\u043c\u0438 FT";
            case "Totem Tracker" -> "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u043a\u0430\u043a\u043e\u0439 \u0442\u043e\u0442\u0435\u043c \u0442\u044b \u0441\u043d\u0435\u0441 \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u0443. \u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435: \u043a\u043b\u0430\u0441\u0441\u0438\u043a\u0430 \u0438\u043b\u0438 \u043c\u0438\u043d\u0438-HUD \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445.";
            case "Lock slots" -> "\u0411\u043b\u043e\u043a\u0438\u0440\u0443\u0435\u0442 \u0441\u043b\u043e\u0442\u044b \u0438\u0437 \u043a\u043e\u0442\u043e\u0440\u044b\u0445 \u043d\u0435\u043b\u044c\u0437\u044f \u0432\u044b\u043a\u0438\u043d\u0443\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442";
            case "Item Swap" -> "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0431\u044b\u0441\u0442\u0440\u043e \u0441\u0432\u0430\u043f\u0430\u0442\u044c \u043f\u043e \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u0443 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b. \u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435: \u043a\u043b\u0430\u0441\u0441\u0438\u043a\u0430 \u0438\u043b\u0438 \u043c\u0438\u043d\u0438-HUD \u0432 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430\u0445.";
            default -> null;
        };
    }

    private void drawDescriptionTooltip(DrawContext context, int mouseX, int mouseY, String title, String description) {
        List<String> lines = this.wrapText(description, 180);
        int maxText = this.textRenderer.getWidth(title);
        for (String line : lines) {
            maxText = Math.max(maxText, this.textRenderer.getWidth(line));
        }
        int w = maxText + 12;
        Objects.requireNonNull(this.textRenderer);
        int n = lines.size();
        Objects.requireNonNull(this.textRenderer);
        int h = 8 + 9 + 4 + n * (9 + 1);
        int x = mouseX + 10;
        int y = mouseY + 10;
        if (x + w > this.width - 4) {
            x = this.width - w - 4;
        }
        if (y + h > this.height - 4) {
            y = this.height - h - 4;
        }
        this.drawRoundedRect(context, x, y, x + w, y + h, -535817702);
        this.drawRoundedOutline(context, x, y, x + w, y + h, -14534042);
        context.drawText(this.textRenderer, title, x + 6, y + 4, -6309633, false);
        Objects.requireNonNull(this.textRenderer);
        int ty = y + 6 + 9 + 2;
        for (String line : lines) {
            context.drawText(this.textRenderer, line, x + 6, ty, -1, false);
            Objects.requireNonNull(this.textRenderer);
            ty += 9 + 1;
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        ArrayList<String> out = new ArrayList<String>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate;
            String string = candidate = current.isEmpty() ? word : String.valueOf(current) + " " + word;
            if (this.textRenderer.getWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (!current.isEmpty()) {
                out.add(current.toString());
                current.setLength(0);
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
        return out;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.tab == 1) {
            this.utilsScroll = Math.max(0, this.utilsScroll - (int)(verticalAmount * 18.0));
            this.init();
            return true;
        }
        if (this.tab == 2) {
            this.visualsScroll = Math.max(0, this.visualsScroll - (int)(verticalAmount * 18.0));
            this.init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (MenuButtonEntry menuButton : this.menuButtons) {
                if (!(mouseX >= (double)menuButton.x) || !(mouseX <= (double)(menuButton.x + menuButton.w)) || !(mouseY >= (double)menuButton.y) || !(mouseY <= (double)(menuButton.y + menuButton.h))) continue;
                menuButton.onClick.run();
                return true;
            }
            for (ToggleEntry entry : this.toggles) {
                if (!(mouseX >= (double)entry.x) || !(mouseX <= (double)(entry.x + entry.w)) || !(mouseY >= (double)entry.y) || !(mouseY <= (double)(entry.y + entry.h))) continue;
                entry.flip.run();
                this.init();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    public boolean shouldPause() {
        return false;
    }

    private static final class ToggleEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final String name;
        private final BooleanSupplier state;
        private final Runnable flip;
        private final Runnable openSettings;

        private ToggleEntry(int x, int y, int w, int h, String name, BooleanSupplier state, Runnable flip, Runnable openSettings) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.name = name;
            this.state = state;
            this.flip = flip;
            this.openSettings = openSettings;
        }
    }

    private static final class MenuButtonEntry {
        private final int x;
        private final int y;
        private final int w;
        private final int h;
        private final String label;
        private final boolean active;
        private final Identifier icon;
        private final Runnable onClick;

        private MenuButtonEntry(int x, int y, int w, int h, String label, boolean active, Identifier icon, Runnable onClick) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.active = active;
            this.icon = icon;
            this.onClick = onClick;
        }
    }
}

