/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.client.gui.widget.SliderWidget
 *  net.minecraft.text.Text
 */
package ru.artem.durabilitytint.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import ru.artem.durabilitytint.DurabilityTintConfig;

public class DurabilityTintConfigScreen
extends Screen {
    private final Screen parent;
    private boolean enabled;
    private double strength;

    public DurabilityTintConfigScreen(Screen parent) {
        super((Text)Text.literal((String)"Durability Armor Hint Settings"));
        this.parent = parent;
    }

    protected void init() {
        this.enabled = DurabilityTintConfig.isEnabled();
        this.strength = DurabilityTintConfig.getStrength();
        int centerX = this.width / 2;
        int y = this.height / 4;
        ButtonWidget enabledButton = ButtonWidget.builder((Text)this.getEnabledText(), button -> {
            this.enabled = !this.enabled;
            button.setMessage(this.getEnabledText());
        }).dimensions(centerX - 100, y, 200, 20).build();
        this.addDrawableChild(enabledButton);
        StrengthSliderWidget slider = new StrengthSliderWidget(centerX - 100, y + 28, 200, 20, this.strength);
        this.addDrawableChild(slider);
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)"Save"), button -> {
            DurabilityTintConfig.setEnabled(this.enabled);
            DurabilityTintConfig.setStrength((float)this.strength);
            DurabilityTintConfig.save();
            this.client.setScreen(this.parent);
        }).dimensions(centerX - 100, y + 60, 97, 20).build());
        this.addDrawableChild(ButtonWidget.builder((Text)Text.literal((String)"Cancel"), button -> this.client.setScreen(this.parent)).dimensions(centerX + 3, y + 60, 97, 20).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private Text getEnabledText() {
        return Text.literal((String)("Color tint: " + (this.enabled ? "ON" : "OFF")));
    }

    private class StrengthSliderWidget
    extends SliderWidget {
        StrengthSliderWidget(int x, int y, int width, int height, double value) {
            super(x, y, width, height, (Text)Text.empty(), value);
            this.updateMessage();
        }

        protected void updateMessage() {
            int percent = (int)Math.round(this.value * 100.0);
            this.setMessage((Text)Text.literal((String)("Color visibility: " + percent + "%")));
        }

        protected void applyValue() {
            DurabilityTintConfigScreen.this.strength = this.value;
        }
    }
}

