package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.EatHelperFeature;
import com.shampoon.speedysclient.features.PotionHighlighterFeature;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudEatHelperMixin {

    @Inject(method = "renderHotbarItem", at = @At("RETURN"))
    private void speedys$eatHelperAfterHotbarItem(
            DrawContext context,
            int x,
            int y,
            RenderTickCounter tickCounter,
            PlayerEntity player,
            ItemStack stack,
            int seed,
            CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.eatHelperEnabled || player == null) {
            return;
        }
        int idx = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i) == stack) {
                idx = i;
                break;
            }
        }
        if (idx < 0 || !EatHelperFeature.isHighlighted(idx)) {
            return;
        }
        EatHelperFeature.drawSlotHighlight(context, x, y);
    }

    @Inject(method = "renderHotbarItem", at = @At("RETURN"))
    private void speedys$potionHighlighterAfterHotbarItem(
            DrawContext context,
            int x,
            int y,
            RenderTickCounter tickCounter,
            PlayerEntity player,
            ItemStack stack,
            int seed,
            CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.potionHighlighterEnabled || player == null) {
            return;
        }
        PotionHighlighterFeature.Tier tier = PotionHighlighterFeature.tierForSplashPotion(stack);
        if (tier == null) {
            return;
        }
        PotionHighlighterFeature.drawSlotFillStatic(context, x, y, tier);
    }
}
