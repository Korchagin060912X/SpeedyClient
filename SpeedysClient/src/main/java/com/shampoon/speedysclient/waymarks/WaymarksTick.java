package com.shampoon.speedysclient.waymarks;

import com.shampoon.speedysclient.config.WaymarksConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Метка смерти при переходе «жив → мёртв».
 */
public final class WaymarksTick {
    private static boolean wasAlive = true;

    private WaymarksTick() {
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null || client.world == null) {
            wasAlive = true;
            return;
        }
        boolean alive = p.isAlive() && p.getHealth() > 1.0E-4f;
        if (wasAlive && !alive) {
            String dim = WaymarksConfig.currentDimensionId(client);
            WaymarksConfig.addDeathMarker(client, p.getX(), p.getY(), p.getZ(), dim);
        }
        wasAlive = alive;
    }
}
