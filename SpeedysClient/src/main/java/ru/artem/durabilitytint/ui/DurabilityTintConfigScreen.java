/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_357
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 */
package ru.artem.durabilitytint.ui;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_357;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import ru.artem.durabilitytint.DurabilityTintConfig;

public class DurabilityTintConfigScreen
extends class_437 {
    private final class_437 parent;
    private boolean enabled;
    private double strength;

    public DurabilityTintConfigScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"Durability Armor Hint Settings"));
        this.parent = parent;
    }

    protected void method_25426() {
        this.enabled = DurabilityTintConfig.isEnabled();
        this.strength = DurabilityTintConfig.getStrength();
        int centerX = this.field_22789 / 2;
        int y = this.field_22790 / 4;
        class_4185 enabledButton = class_4185.method_46430((class_2561)this.getEnabledText(), button -> {
            this.enabled = !this.enabled;
            button.method_25355(this.getEnabledText());
        }).method_46434(centerX - 100, y, 200, 20).method_46431();
        this.method_37063((class_364)enabledButton);
        StrengthSliderWidget slider = new StrengthSliderWidget(centerX - 100, y + 28, 200, 20, this.strength);
        this.method_37063((class_364)slider);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Save"), button -> {
            DurabilityTintConfig.setEnabled(this.enabled);
            DurabilityTintConfig.setStrength((float)this.strength);
            DurabilityTintConfig.save();
            this.field_22787.method_1507(this.parent);
        }).method_46434(centerX - 100, y + 60, 97, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), button -> this.field_22787.method_1507(this.parent)).method_46434(centerX + 3, y + 60, 97, 20).method_46431());
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        this.method_25420(context, mouseX, mouseY, delta);
        context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 20, 0xFFFFFF);
        super.method_25394(context, mouseX, mouseY, delta);
    }

    private class_2561 getEnabledText() {
        return class_2561.method_43470((String)("Color tint: " + (this.enabled ? "ON" : "OFF")));
    }

    private class StrengthSliderWidget
    extends class_357 {
        StrengthSliderWidget(int x, int y, int width, int height, double value) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), value);
            this.method_25346();
        }

        protected void method_25346() {
            int percent = (int)Math.round(this.field_22753 * 100.0);
            this.method_25355((class_2561)class_2561.method_43470((String)("Color visibility: " + percent + "%")));
        }

        protected void method_25344() {
            DurabilityTintConfigScreen.this.strength = this.field_22753;
        }
    }
}

