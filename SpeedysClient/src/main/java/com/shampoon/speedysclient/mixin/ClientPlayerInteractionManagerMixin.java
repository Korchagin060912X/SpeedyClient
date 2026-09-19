/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.TargetEspFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void speedys$blockFriendHit(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.friendSystemEnabled) {
            return;
        }
        String csv = PVPUtils.CONFIG.friendNickname;
        if (csv == null || csv.isBlank()) {
            return;
        }
        if (!(target instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity fp = (PlayerEntity)target;
        if (!speedys$matchesFriendCsv(csv, fp.getGameProfile().getName())) {
            return;
        }
        if (player.getWorld().isClient() && player == MinecraftClient.getInstance().player) {
            player.sendMessage((Text)Text.literal((String)"\u00a7l\u00a71[SpeedyClient]\u00a7r \u0422\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0448\u044c \u0443\u0434\u0430\u0440\u0438\u0442\u044c \u0441\u0432\u043e\u0435\u0433\u043e \u0442\u0438\u043c\u0435\u0439\u0442\u0430!"), false);
        }
        ci.cancel();
    }

    @Inject(method={"method_2918(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;)V"}, at={@At(value="TAIL")})
    private void speedys$rememberTargetForEsp(PlayerEntity player, Entity target, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.targetEspEnabled) {
            return;
        }
        if (target instanceof LivingEntity) {
            TargetEspFeature.onHit(target);
        }
    }

    private static boolean speedys$matchesFriendCsv(String csv, String name) {
        if (csv == null || csv.isBlank() || name == null) {
            return false;
        }
        for (String part : csv.split(",")) {
            String candidate = part.trim();
            if (!candidate.isEmpty() && name.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}

