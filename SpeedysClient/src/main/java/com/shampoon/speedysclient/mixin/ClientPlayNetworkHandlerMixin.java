/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.shampoon.speedysclient.mixin;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.SpeedysUtilsFeature;
import com.shampoon.speedysclient.features.TargetEspFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPlayNetworkHandler.class})
public class ClientPlayNetworkHandlerMixin {
    @Inject(method={"method_11148(Lnet/minecraft/class_2663;)V"}, at={@At(value="TAIL")})
    private void speedys$trackTotemPops(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() != 35) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }
        Entity entity = packet.getEntity((World)client.world);
        if (entity != null) {
            SpeedysUtilsFeature.onTotemPop(entity);
        }
    }

    /** Любой урон от локального игрока (рука, лук, трезубец, снаряды с owner = ты и т.д.) по {@link EntityDamageS2CPacket}. */
    @Inject(method = "onEntityDamage", at = @At("TAIL"))
    private void speedys$targetEspFromYourDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.targetEspEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }
        Entity victim = client.world.getEntityById(packet.entityId());
        if (!(victim instanceof LivingEntity) || victim == client.player) {
            return;
        }
        DamageSource src = packet.createDamageSource(client.world);
        if (!ClientPlayNetworkHandlerMixin.speedys$isDamageFromLocalPlayer(client, src)) {
            return;
        }
        TargetEspFeature.onHit(victim);
    }

    private static boolean speedys$isDamageFromLocalPlayer(MinecraftClient client, DamageSource src) {
        Entity player = client.player;
        Entity attacker = src.getAttacker();
        if (attacker == player) {
            return true;
        }
        Entity direct = src.getSource();
        return direct instanceof ProjectileEntity projectile && projectile.getOwner() == player;
    }
}

