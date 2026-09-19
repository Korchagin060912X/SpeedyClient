/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.hud.InGameHud
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={InGameHud.class})
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient field_2035;
    @Shadow
    @Final
    private static Identifier field_45304;

    @Redirect(method={"method_1736(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V"))
    private void attackindicator$renderScaledColoredCrosshair(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (!field_45304.equals((Object)sprite)) {
            context.drawGuiTexture(pipeline, sprite, x, y, width, height);
            return;
        }
        ModConfig config = ModConfig.getConfig();
        if (!config.enabled) {
            context.drawGuiTexture(pipeline, sprite, x, y, width, height);
            return;
        }
        boolean highlight = this.shouldHighlightTarget();
        int color = highlight ? 0xFF000000 | config.selectedColorRgb() : -1;
        float scale = config.effectiveScale();
        Matrix3x2fStack matrices = context.getMatrices();
        float centerX = (float)x + (float)width / 2.0f;
        float centerY = (float)y + (float)height / 2.0f;
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);
        if (highlight) {
            this.drawSolidCrosshair(context, x, y, width, height, color);
        } else {
            context.drawGuiTexture(pipeline, sprite, x, y, width, height, color);
        }
        matrices.popMatrix();
    }

    private boolean shouldHighlightTarget() {
        LivingEntity living;
        if (this.field_2035.player == null || this.field_2035.world == null) {
            return false;
        }
        HitResult hitResult = this.field_2035.crosshairTarget;
        if (!(hitResult instanceof EntityHitResult)) {
            return false;
        }
        EntityHitResult entityHitResult = (EntityHitResult)hitResult;
        Entity targeted = entityHitResult.getEntity();
        if (!(targeted instanceof LivingEntity) || !(living = (LivingEntity)targeted).isAlive() || targeted == this.field_2035.player) {
            return false;
        }
        double interactionRange = this.field_2035.player.getEntityInteractionRange();
        if (this.field_2035.player.squaredDistanceTo(entityHitResult.getPos()) > interactionRange * interactionRange) {
            return false;
        }
        return this.field_2035.player.canSee(targeted);
    }

    private void drawSolidCrosshair(DrawContext context, int x, int y, int width, int height, int color) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int halfVertical = Math.max(3, height / 2 - 1);
        int halfHorizontal = Math.max(3, width / 2 - 1);
        int thickness = Math.max(1, width / 8);
        context.fill(centerX - thickness / 2, centerY - halfVertical, centerX + (thickness + 1) / 2, centerY + halfVertical + 1, color);
        context.fill(centerX - halfHorizontal, centerY - thickness / 2, centerX + halfHorizontal + 1, centerY + (thickness + 1) / 2, color);
    }
}

