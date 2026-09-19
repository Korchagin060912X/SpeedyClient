package com.shampoon.speedysclient.ui;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class InvHudScaleScreen extends Screen {
    private final Screen parent;

    public InvHudScaleScreen(Screen parent) {
        super(Text.literal("Inv HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.addDrawableChild(new SliderWidget(cx - 120, cy - 10, 240, 20, Text.literal("Размер: 1.00"), invHudScaleToSlider(PVPUtils.CONFIG.invHudScale)) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(String.format("Размер: %.2f", sliderToInvHudScale(this.value))));
            }

            @Override
            protected void applyValue() {
                PVPUtils.CONFIG.invHudScale = sliderToInvHudScale(this.value);
                PVPUtils.CONFIG.save();
            }
        });
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> this.close()).dimensions(cx - 70, cy + 18, 140, 20).build());
    }

    private static double invHudScaleToSlider(float scale) {
        float clamped = Math.max(0.01f, Math.min(1.0f, scale));
        return (clamped - 0.01f) / 0.99f;
    }

    private static float sliderToInvHudScale(double slider) {
        double clamped = Math.max(0.0, Math.min(1.0, slider));
        return (float)(0.01 + clamped * 0.99);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 32, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
