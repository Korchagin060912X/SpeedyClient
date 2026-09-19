/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.shampoon.pvputils.PVPUtils
 *  com.shampoon.pvputils.features.SoundController
 *  com.shampoon.pvputils.features.Zoom
 *  me.shampoon.attackindicator.ModConfig
 *  me.shampoon.attackindicator.ModConfig$CrosshairColor
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_4185$class_4241
 *  net.minecraft.class_437
 *  ru.artem.durabilitytint.DurabilityTintConfig
 */
package com.shampoon.speedysclient.ui;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.pvputils.features.SoundController;
import com.shampoon.pvputils.features.Zoom;
import java.lang.invoke.LambdaMetafactory;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import me.shampoon.attackindicator.ModConfig;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import ru.artem.durabilitytint.DurabilityTintConfig;

public final class SpeedysSettingsPopupScreen
extends class_437 {
    private static final int BG = -805306368;
    private static final int OUTLINE = -14996918;
    private static final int W = 280;
    private static final int H = 300;
    private static final int SWITCH_ON = -9545473;
    private static final int SWITCH_OFF = -11709847;
    private static final int SWITCH_KNOB = -1184001;
    private final class_437 parent;
    private int scroll = 0;

    public SpeedysSettingsPopupScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0444\u0443\u043d\u043a\u0446\u0438\u0439"));
        this.parent = parent;
    }

    protected void method_25426() {
        this.method_37067();
        int cx = this.field_22789 / 2;
        int y = this.field_22790 / 2 - 150 + 18 - this.scroll;
        ModConfig ai = ModConfig.getConfig();
        if (this.visible(y)) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Armor HUD"), b -> {
                PVPUtils.CONFIG.armorHudEnabled = !PVPUtils.CONFIG.armorHudEnabled;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Effect Timer"), b -> {
                PVPUtils.CONFIG.effectTimerEnabled = !PVPUtils.CONFIG.effectTimerEnabled;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        if (this.visible(y += 19)) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0426\u0432\u0435\u0442: \u041a\u0440\u0430\u0441\u043d\u044b\u0439"), b -> {
                ai.color = ModConfig.CrosshairColor.RED;
                ai.save();
            }).method_46434(cx - 110, y, 70, 16).method_46431());
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0417\u0435\u043b\u0435\u043d\u044b\u0439"), b -> {
                ai.color = ModConfig.CrosshairColor.GREEN;
                ai.save();
            }).method_46434(cx - 35, y, 70, 16).method_46431());
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u0416\u0435\u043b\u0442\u044b\u0439"), b -> {
                ai.color = ModConfig.CrosshairColor.YELLOW;
                ai.save();
            }).method_46434(cx + 40, y, 70, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            float vis = DurabilityTintConfig.getStrength();
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        if (this.visible(y += 19)) {
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        y += 19;
        y = this.addSoundSlider(cx, y, "\u0417\u0432\u0443\u043a: \u043e\u043f\u044b\u0442", PVPUtils.CONFIG.soundControllerExpOrbVolume, v -> {
            PVPUtils.CONFIG.soundControllerExpOrbVolume = v.floatValue();
            PVPUtils.CONFIG.save();
        });
        y = this.addSoundSlider(cx, y, "\u0417\u0432\u0443\u043a: \u0438\u0441\u0441\u0443\u0448\u0438\u0442\u0435\u043b\u044c", PVPUtils.CONFIG.soundControllerWitherVolume, v -> {
            PVPUtils.CONFIG.soundControllerWitherVolume = v.floatValue();
            PVPUtils.CONFIG.save();
        });
        if (this.visible(y = this.addSoundSlider(cx, y, "\u0417\u0432\u0443\u043a: \u0442\u0440\u0435\u0437\u0443\u0431\u0435\u0446", PVPUtils.CONFIG.soundControllerTridentVolume, v -> {
            PVPUtils.CONFIG.soundControllerTridentVolume = v.floatValue();
            PVPUtils.CONFIG.save();
        }))) {
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        if (this.visible(y += 19)) {
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Trail R", PVPUtils.CONFIG.trailR, v -> {
            PVPUtils.CONFIG.trailR = v;
        });
        y = this.addRgbSlider(cx, y, "Trail G", PVPUtils.CONFIG.trailG, v -> {
            PVPUtils.CONFIG.trailG = v;
        });
        y = this.addRgbSlider(cx, y, "Trail B", PVPUtils.CONFIG.trailB, v -> {
            PVPUtils.CONFIG.trailB = v;
        });
        y = this.addRgbSlider(cx, y, "China Hat R", PVPUtils.CONFIG.chinaHatR, v -> {
            PVPUtils.CONFIG.chinaHatR = v;
        });
        y = this.addRgbSlider(cx, y, "China Hat G", PVPUtils.CONFIG.chinaHatG, v -> {
            PVPUtils.CONFIG.chinaHatG = v;
        });
        y = this.addRgbSlider(cx, y, "China Hat B", PVPUtils.CONFIG.chinaHatB, v -> {
            PVPUtils.CONFIG.chinaHatB = v;
        });
        y = this.addRgbSlider(cx, y, "Jump Circle R", PVPUtils.CONFIG.jumpCircleR, v -> {
            PVPUtils.CONFIG.jumpCircleR = v;
        });
        y = this.addRgbSlider(cx, y, "Jump Circle G", PVPUtils.CONFIG.jumpCircleG, v -> {
            PVPUtils.CONFIG.jumpCircleG = v;
        });
        if (this.visible(y = this.addRgbSlider(cx, y, "Jump Circle B", PVPUtils.CONFIG.jumpCircleB, v -> {
            PVPUtils.CONFIG.jumpCircleB = v;
        }))) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("Jump Circle: " + (PVPUtils.CONFIG.jumpCircleSpreadByBlocks ? "\u041f\u043e \u0431\u043b\u043e\u043a\u0430\u043c" : "\u041e\u0431\u044b\u0447\u043d\u044b\u0439 \u043a\u0440\u0443\u0433"))), b -> {
                PVPUtils.CONFIG.jumpCircleSpreadByBlocks = !PVPUtils.CONFIG.jumpCircleSpreadByBlocks;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        y += 19;
        y = this.addRgbSlider(cx, y, "Target ESP R", PVPUtils.CONFIG.targetEspR, v -> {
            PVPUtils.CONFIG.targetEspR = v;
        });
        y = this.addRgbSlider(cx, y, "Target ESP G", PVPUtils.CONFIG.targetEspG, v -> {
            PVPUtils.CONFIG.targetEspG = v;
        });
        y = this.addRgbSlider(cx, y, "Target ESP B", PVPUtils.CONFIG.targetEspB, v -> {
            PVPUtils.CONFIG.targetEspB = v;
        });
        y = this.addRgbSlider(cx, y, "Predict Hit R", PVPUtils.CONFIG.predictionHitR, v -> {
            PVPUtils.CONFIG.predictionHitR = v;
        });
        y = this.addRgbSlider(cx, y, "Predict Hit G", PVPUtils.CONFIG.predictionHitG, v -> {
            PVPUtils.CONFIG.predictionHitG = v;
        });
        y = this.addRgbSlider(cx, y, "Predict Hit B", PVPUtils.CONFIG.predictionHitB, v -> {
            PVPUtils.CONFIG.predictionHitB = v;
        });
        y = this.addRgbSlider(cx, y, "Nimb R", PVPUtils.CONFIG.nimbR, v -> {
            PVPUtils.CONFIG.nimbR = v;
        });
        y = this.addRgbSlider(cx, y, "Nimb G", PVPUtils.CONFIG.nimbG, v -> {
            PVPUtils.CONFIG.nimbG = v;
        });
        if (this.visible(y = this.addRgbSlider(cx, y, "Nimb B", PVPUtils.CONFIG.nimbB, v -> {
            PVPUtils.CONFIG.nimbB = v;
        }))) {
            ((class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"=== Visuals / Custom Hand ==="), (class_4185.class_4241)(class_4185.class_4241)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/class_4185;)V, lambda$init$27(net.minecraft.class_4185 ), (Lnet/minecraft/class_4185;)V)()).method_46434((int)(cx - 110), (int)y, (int)220, (int)16).method_46431())).field_22763 = false;
        }
        y += 19;
        y = this.addFloatSlider(cx, y, "Hand X", PVPUtils.CONFIG.customHandOffsetX, -1.5f, 1.5f, v -> {
            PVPUtils.CONFIG.customHandOffsetX = v.floatValue();
        });
        y = this.addFloatSlider(cx, y, "Hand Y", PVPUtils.CONFIG.customHandOffsetY, -1.5f, 1.5f, v -> {
            PVPUtils.CONFIG.customHandOffsetY = v.floatValue();
        });
        y = this.addFloatSlider(cx, y, "Hand Z", PVPUtils.CONFIG.customHandOffsetZ, -1.5f, 1.5f, v -> {
            PVPUtils.CONFIG.customHandOffsetZ = v.floatValue();
        });
        y = this.addFloatSlider(cx, y, "Hand Scale", PVPUtils.CONFIG.customHandScale, 0.2f, 2.0f, v -> {
            PVPUtils.CONFIG.customHandScale = v.floatValue();
        });
        y = this.addIntSlider(cx, y, "Hand Position Rotate", PVPUtils.CONFIG.customHandPosRotation, -180, 180, v -> {
            PVPUtils.CONFIG.customHandPosRotation = v;
        });
        if (this.visible(y = this.addIntSlider(cx, y, "Hand Hit Rotate", PVPUtils.CONFIG.customHandHitRotation, -180, 180, v -> {
            PVPUtils.CONFIG.customHandHitRotation = v;
        }))) {
            class_342 friendField = new class_342(this.field_22793, cx - 110, y, 220, 18, (class_2561)class_2561.method_43470((String)"Friend"));
            friendField.method_1880(48);
            friendField.method_47404((class_2561)class_2561.method_43470((String)"\u041d\u0438\u043a \u0434\u0440\u0443\u0433\u0430 (Friend System)"));
            friendField.method_1852(PVPUtils.CONFIG.friendNickname == null ? "" : PVPUtils.CONFIG.friendNickname);
            friendField.method_1863(s -> {
                PVPUtils.CONFIG.friendNickname = s;
                PVPUtils.CONFIG.save();
            });
            this.method_37063((class_364)friendField);
        }
        if (this.visible(y += 21)) {
            class_342 lockSlotsField = new class_342(this.field_22793, cx - 110, y, 220, 18, (class_2561)class_2561.method_43470((String)"LockSlots"));
            lockSlotsField.method_1880(48);
            lockSlotsField.method_47404((class_2561)class_2561.method_43470((String)"Lock slots: 1,5,8"));
            lockSlotsField.method_1852(PVPUtils.CONFIG.lockSlotsCsv == null ? "" : PVPUtils.CONFIG.lockSlotsCsv);
            lockSlotsField.method_1863(s -> {
                PVPUtils.CONFIG.lockSlotsCsv = s;
                PVPUtils.CONFIG.save();
            });
            this.method_37063((class_364)lockSlotsField);
        }
        if (this.visible(y += 21)) {
            class_342 item1Field = new class_342(this.field_22793, cx - 110, y, 220, 18, (class_2561)class_2561.method_43470((String)"ItemSwap1"));
            item1Field.method_1880(96);
            item1Field.method_47404((class_2561)class_2561.method_43470((String)"1 \u043f\u0440\u0435\u0434\u043c\u0435\u0442: \u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
            item1Field.method_1852(PVPUtils.CONFIG.itemSwapItem1Name == null ? "" : PVPUtils.CONFIG.itemSwapItem1Name);
            item1Field.method_1863(s -> {
                PVPUtils.CONFIG.itemSwapItem1Name = s;
                PVPUtils.CONFIG.save();
            });
            this.method_37063((class_364)item1Field);
        }
        if (this.visible(y += 21)) {
            class_342 item2Field = new class_342(this.field_22793, cx - 110, y, 220, 18, (class_2561)class_2561.method_43470((String)"ItemSwap2"));
            item2Field.method_1880(96);
            item2Field.method_47404((class_2561)class_2561.method_43470((String)"2 \u043f\u0440\u0435\u0434\u043c\u0435\u0442: \u0432\u0432\u0435\u0434\u0438\u0442\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435"));
            item2Field.method_1852(PVPUtils.CONFIG.itemSwapItem2Name == null ? "" : PVPUtils.CONFIG.itemSwapItem2Name);
            item2Field.method_1863(s -> {
                PVPUtils.CONFIG.itemSwapItem2Name = s;
                PVPUtils.CONFIG.save();
            });
            this.method_37063((class_364)item2Field);
        }
        if (this.visible(y += 21)) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("Item Swap \u0440\u0443\u043a\u0430: " + (PVPUtils.CONFIG.itemSwapUseOffhand ? "\u041b\u0435\u0432\u0430\u044f" : "\u041f\u0440\u0430\u0432\u0430\u044f"))), b -> {
                PVPUtils.CONFIG.itemSwapUseOffhand = !PVPUtils.CONFIG.itemSwapUseOffhand;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            boolean normal = !PVPUtils.CONFIG.trapTimerDragonMode;
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("Trap Timer: \u041e\u0431\u044b\u0447\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430." + (normal ? " \u00a7a(\u0432\u044b\u0431\u0440\u0430\u043d\u043e)" : ""))), b -> {
                PVPUtils.CONFIG.trapTimerDragonMode = false;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            boolean dragon = PVPUtils.CONFIG.trapTimerDragonMode;
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("Trap Timer: \u0414\u0440\u0430\u043a\u043e\u043d\u044c\u044f \u0442\u0440\u0430\u043f\u043a\u0430." + (dragon ? " \u00a7a(\u0432\u044b\u0431\u0440\u0430\u043d\u043e)" : ""))), b -> {
                PVPUtils.CONFIG.trapTimerDragonMode = true;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            boolean normalTrap = !PVPUtils.CONFIG.ftHelperDragonTrapMode;
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("FT Helper: \u0422\u0440\u0430\u043f\u043a\u0430 \u043e\u0431\u044b\u0447\u043d\u0430\u044f." + (normalTrap ? " \u00a7a(\u0432\u044b\u0431\u0440\u0430\u043d\u043e)" : ""))), b -> {
                PVPUtils.CONFIG.ftHelperDragonTrapMode = false;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        if (this.visible(y += 19)) {
            boolean dragonTrap = PVPUtils.CONFIG.ftHelperDragonTrapMode;
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)("FT Helper: \u0422\u0440\u0430\u043f\u043a\u0430 \u0414\u0440\u0430\u043a\u043e\u043d\u044c\u044f." + (dragonTrap ? " \u00a7a(\u0432\u044b\u0431\u0440\u0430\u043d\u043e)" : ""))), b -> {
                PVPUtils.CONFIG.ftHelperDragonTrapMode = true;
                PVPUtils.CONFIG.save();
                this.method_25426();
            }).method_46434(cx - 110, y, 220, 16).method_46431());
        }
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"\u041d\u0430\u0437\u0430\u0434"), b -> this.method_25419()).method_46434(cx - 55, this.field_22790 / 2 + 150 - 20, 110, 16).method_46431());
    }

    private int addRgbSlider(int cx, int y, String label, int currentChannel, IntConsumer setChannel) {
        if (this.visible(y)) {
            double v01 = (double)currentChannel / 255.0;
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        return y + 19;
    }

    private int addSoundSlider(int cx, int y, String prefix, float unit, Consumer<Float> apply) {
        if (this.visible(y)) {
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        return y + 19;
    }

    private int addIntSlider(int cx, int y, String label, int current, int min, int max, IntConsumer apply) {
        if (this.visible(y)) {
            double v01 = (double)(current - min) / (double)Math.max(1, max - min);
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        return y + 19;
    }

    private int addFloatSlider(int cx, int y, String label, float current, float min, float max, Consumer<Float> apply) {
        if (this.visible(y)) {
            double v01 = (double)(current - min) / Math.max(1.0E-4, (double)(max - min));
            this.method_37063((class_364)new /* Unavailable Anonymous Inner Class!! */);
        }
        return y + 19;
    }

    private boolean visible(int y) {
        int top = this.field_22790 / 2 - 150 + 14;
        int bottom = this.field_22790 / 2 + 150 - 30;
        return y >= top && y <= bottom;
    }

    private static double fullBrightSliderValue(int userGamma) {
        return (double)(userGamma - 100) / 1900.0;
    }

    private static int fullBrightUserFromSlider(double sliderValue01) {
        return (int)Math.round(100.0 + sliderValue01 * 1900.0);
    }

    private static double zoomSliderValue(int level) {
        return (double)(level - 1) / 49.0;
    }

    private static int zoomLevelFromSlider(double sliderValue01) {
        return Zoom.clampLevel((int)(1 + (int)Math.round(sliderValue01 * 49.0)));
    }

    private static String soundPct(float unit) {
        return Math.round(SoundController.clampUnit((float)unit) * 100.0f) + "%";
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.method_25420(context, mouseX, mouseY, delta);
        int l = this.field_22789 / 2 - 140;
        int t = this.field_22790 / 2 - 150;
        int r = this.field_22789 / 2 + 140;
        int b = this.field_22790 / 2 + 150;
        this.drawRoundedRect(context, l, t, r, b, -805306368);
        this.drawRoundedOutline(context, l, t, r, b, -14996918);
        context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, t + 6, 0xFFFFFF);
        super.method_25394(context, mouseX, mouseY, delta);
        this.drawSwitch(context, this.field_22789 / 2 + 72, this.field_22790 / 2 - 150 + 21 - this.scroll, PVPUtils.CONFIG.armorHudEnabled);
        this.drawSwitch(context, this.field_22789 / 2 + 72, this.field_22790 / 2 - 150 + 40 - this.scroll, PVPUtils.CONFIG.effectTimerEnabled);
    }

    private void drawSwitch(class_332 context, int x, int y, boolean on) {
        this.drawRoundedRect(context, x, y, x + 30, y + 10, on ? -9545473 : -11709847);
        int kx = on ? x + 20 : x + 1;
        this.drawRoundedRect(context, kx, y + 1, kx + 9, y + 9, -1184001);
    }

    private void drawRoundedRect(class_332 context, int l, int t, int r, int b, int color) {
        context.method_25294(l + 3, t, r - 3, b, color);
        context.method_25294(l, t + 3, r, b - 3, color);
        context.method_25294(l + 1, t + 1, l + 3, t + 3, color);
        context.method_25294(r - 3, t + 1, r - 1, t + 3, color);
        context.method_25294(l + 1, b - 3, l + 3, b - 1, color);
        context.method_25294(r - 3, b - 3, r - 1, b - 1, color);
    }

    private void drawRoundedOutline(class_332 context, int l, int t, int r, int b, int color) {
        context.method_25294(l + 3, t, r - 3, t + 1, color);
        context.method_25294(l + 3, b - 1, r - 3, b, color);
        context.method_25294(l, t + 3, l + 1, b - 3, color);
        context.method_25294(r - 1, t + 3, r, b - 3, color);
        context.method_25294(l + 1, t + 1, l + 3, t + 2, color);
        context.method_25294(r - 3, t + 1, r - 1, t + 2, color);
        context.method_25294(l + 1, b - 2, l + 3, b - 1, color);
        context.method_25294(r - 3, b - 2, r - 1, b - 1, color);
    }

    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll = Math.max(0, this.scroll - (int)(verticalAmount * 18.0));
        this.method_25426();
        return true;
    }

    public void method_25419() {
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }

    public boolean method_25421() {
        return false;
    }

    private static /* synthetic */ void lambda$init$27(class_4185 b) {
    }
}
