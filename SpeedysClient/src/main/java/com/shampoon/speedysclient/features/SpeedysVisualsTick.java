/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.util.math.Vec3d
 */
package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import com.shampoon.speedysclient.features.TargetEspFeature;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class SpeedysVisualsTick {
    private static final int TRAIL_INTERVAL = 2;
    private static final int TRAIL_MAX_POINTS = 90;
    private static final int TRAIL_MAX_AGE = 20;
    private static int trailCooldown;
    private static boolean wasOnGround;
    private static final ArrayDeque<TrailPoint> trailPoints;
    public static final int JUMP_RING_MAX_AGE = 22;
    private static final List<JumpRing> jumpRings;

    private SpeedysVisualsTick() {
    }

    public static List<JumpRing> getJumpRings() {
        return jumpRings;
    }

    public static ArrayDeque<TrailPoint> getTrailPoints() {
        return trailPoints;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            trailPoints.clear();
            jumpRings.clear();
            wasOnGround = true;
            return;
        }
        ClientPlayerEntity p = client.player;
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.trailEnabled) {
            Vec3d vel = p.getVelocity();
            double h = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            boolean running = p.isSprinting() && h > 0.12;
            ArrayDeque<TrailPoint> aged = new ArrayDeque<TrailPoint>();
            for (TrailPoint pt : trailPoints) {
                int na = pt.age + 1;
                if (na > 20) continue;
                aged.addLast(new TrailPoint(pt.x, pt.y, pt.z, na));
            }
            trailPoints.clear();
            trailPoints.addAll(aged);
            if (running && --trailCooldown <= 0) {
                trailCooldown = 2;
                trailPoints.addLast(new TrailPoint(p.getX(), p.getY() + 0.05, p.getZ(), 0));
                while (trailPoints.size() > 90) {
                    trailPoints.removeFirst();
                }
            }
        } else {
            trailPoints.clear();
        }
        if (PVPUtils.CONFIG != null && PVPUtils.CONFIG.jumpCircleEnabled) {
            boolean onGround = p.isOnGround();
            if (!onGround && wasOnGround && p.getVelocity().y > 0.08) {
                jumpRings.add(new JumpRing(p.getX(), p.getY() + 0.02, p.getZ()));
            }
            wasOnGround = onGround;
        } else {
            jumpRings.clear();
            wasOnGround = p.isOnGround();
        }
        jumpRings.removeIf(ring -> {
            ++ring.ticks;
            return ring.ticks >= 22;
        });
        TargetEspFeature.tick();
        TargetEspFeature.tickParticles(client);
        CustomWorldCometsFeature.tick(client);
    }

    static {
        wasOnGround = true;
        trailPoints = new ArrayDeque();
        jumpRings = new ArrayList<JumpRing>();
    }

    public static final class TrailPoint {
        public final double x;
        public final double y;
        public final double z;
        public final int age;

        public TrailPoint(double x, double y, double z, int age) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.age = age;
        }
    }

    public static final class JumpRing {
        public final double x;
        public final double y;
        public final double z;
        public int ticks;

        public JumpRing(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

