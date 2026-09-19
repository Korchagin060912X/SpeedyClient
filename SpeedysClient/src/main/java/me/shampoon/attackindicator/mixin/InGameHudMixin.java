/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_239
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_3966
 *  org.joml.Matrix3x2fStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package me.shampoon.attackindicator.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import me.shampoon.attackindicator.ModConfig;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_239;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_329.class})
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private class_310 field_2035;
    @Shadow
    @Final
    private static class_2960 field_45304;

    @Redirect(method={"method_1736(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"))
    private void attackindicator$renderScaledColoredCrosshair(class_332 context, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height) {
        if (!field_45304.equals((Object)sprite)) {
            context.method_52706(pipeline, sprite, x, y, width, height);
            return;
        }
        ModConfig config = ModConfig.getConfig();
        if (!config.enabled) {
            context.method_52706(pipeline, sprite, x, y, width, height);
            return;
        }
        boolean highlight = this.shouldHighlightTarget();
        int color = highlight ? 0xFF000000 | config.selectedColorRgb() : -1;
        float scale = config.effectiveScale();
        Matrix3x2fStack matrices = context.method_51448();
        float centerX = (float)x + (float)width / 2.0f;
        float centerY = (float)y + (float)height / 2.0f;
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);
        if (highlight) {
            this.drawSolidCrosshair(context, x, y, width, height, color);
        } else {
            context.method_52707(pipeline, sprite, x, y, width, height, color);
        }
        matrices.popMatrix();
    }

    private boolean shouldHighlightTarget() {
        class_1309 living;
        if (this.field_2035.field_1724 == null || this.field_2035.field_1687 == null) {
            return false;
        }
        class_239 hitResult = this.field_2035.field_1765;
        if (!(hitResult instanceof class_3966)) {
            return false;
        }
        class_3966 entityHitResult = (class_3966)hitResult;
        class_1297 targeted = entityHitResult.method_17782();
        if (!(targeted instanceof class_1309) || !(living = (class_1309)targeted).method_5805() || targeted == this.field_2035.field_1724) {
            return false;
        }
        double interactionRange = this.field_2035.field_1724.method_55755();
        if (this.field_2035.field_1724.method_5707(entityHitResult.method_17784()) > interactionRange * interactionRange) {
            return false;
        }
        return this.field_2035.field_1724.method_6057(targeted);
    }

    private void drawSolidCrosshair(class_332 context, int x, int y, int width, int height, int color) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int halfVertical = Math.max(3, height / 2 - 1);
        int halfHorizontal = Math.max(3, width / 2 - 1);
        int thickness = Math.max(1, width / 8);
        context.method_25294(centerX - thickness / 2, centerY - halfVertical, centerX + (thickness + 1) / 2, centerY + halfVertical + 1, color);
        context.method_25294(centerX - halfHorizontal, centerY - thickness / 2, centerX + halfHorizontal + 1, centerY + (thickness + 1) / 2, color);
    }
}

