package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.EatHelperFeature;
import com.shampoon.speedysclient.features.PotionHighlighterFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenEatHelperMixin {

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void speedys$eatHelperAfterSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.eatHelperEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || slot.inventory != client.player.getInventory()) {
            return;
        }
        if (!EatHelperFeature.isHighlighted(slot.getIndex())) {
            return;
        }
        // Координаты слота уже в системе GUI после translate(guiLeft, guiTop) — как в vanilla drawSlot.
        EatHelperFeature.drawSlotHighlight(context, slot.x, slot.y);
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void speedys$potionHighlighterAfterSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.potionHighlighterEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || slot.inventory != client.player.getInventory()) {
            return;
        }
        PotionHighlighterFeature.Tier tier = PotionHighlighterFeature.tierForSplashPotion(slot.getStack());
        if (tier == null) {
            return;
        }
        PotionHighlighterFeature.drawSlotFillStatic(context, slot.x, slot.y, tier);
    }
}
