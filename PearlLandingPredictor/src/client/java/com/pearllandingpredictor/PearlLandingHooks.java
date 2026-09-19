/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
 */
package com.pearllandingpredictor;

import com.pearllandingpredictor.PearlTrajectoryRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class PearlLandingHooks {
    private PearlLandingHooks() {
    }

    public static void register() {
        WorldRenderEvents.LAST.register(PearlTrajectoryRenderer::render);
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> PearlTrajectoryRenderer.renderHud(drawContext));
    }

    public static void toggle() {
        PearlTrajectoryRenderer.toggleEnabled();
    }

    public static boolean isEnabled() {
        return PearlTrajectoryRenderer.isEnabled();
    }
}

