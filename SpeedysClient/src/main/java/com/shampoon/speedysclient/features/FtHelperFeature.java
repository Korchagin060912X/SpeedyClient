package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class FtHelperFeature {
    /** Как у снежка / снаряда в ванилле (направление × скорость). */
    private static final double SNOWBALL_THROW_SPEED = 1.5;
    private static final double SNOWBALL_DRAG = 0.99;
    private static final double SNOWBALL_GRAVITY = 0.03;
    /** Радиус круга на земле ≈ зона 5×5 (диаметр ~5 блоков). */
    private static final double SNOWBALL_LAND_RADIUS = 2.5;

    private FtHelperFeature() {
    }

    public static State collect(MinecraftClient client) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.ftHelperEnabled || client.player == null || client.world == null) {
            return State.NONE;
        }
        Item held = FtHelperFeature.activeItem(client.player.getMainHandStack(), client.player.getOffHandStack());
        if (held == null) {
            return State.NONE;
        }
        if (held == Items.SUGAR || held == Items.ENDER_EYE || held == Items.FIRE_CHARGE) {
            Vec3d center = client.player.getPos();
            double radius = 10.0;
            Box area = new Box(center.x - radius, center.y - 1.0, center.z - radius, center.x + radius, center.y + 2.0, center.z + radius);
            return new State(ShapeType.CIRCLE, radius, 0.0, FtHelperFeature.hasAnyTarget(client, area, radius * radius, center.x, center.z), center);
        }
        if (held == Items.SNOWBALL) {
            Vec3d landing = FtHelperFeature.predictSnowballBlockLanding(client);
            if (landing == null) {
                return State.NONE;
            }
            double groundY = MathHelper.floor(landing.y);
            Vec3d center = new Vec3d(landing.x, groundY + 0.02, landing.z);
            double r = SNOWBALL_LAND_RADIUS;
            double rSq = r * r;
            Box area = new Box(center.x - r, center.y - 1.0, center.z - r, center.x + r, center.y + 2.0, center.z + r);
            return new State(ShapeType.CIRCLE, r, 0.0, FtHelperFeature.hasAnyTarget(client, area, rSq, center.x, center.z), center);
        }
        if (held == Items.NETHERITE_SCRAP) {
            if (PVPUtils.CONFIG.trapTimerEnabled && TrapTimerFeature.isCountdownActive()) {
                return State.NONE;
            }
            boolean dragonTrap = PVPUtils.CONFIG.trapTimerEnabled
                    ? PVPUtils.CONFIG.trapTimerDragonMode
                    : PVPUtils.CONFIG.ftHelperDragonTrapMode;
            double size = dragonTrap ? 7.0 : 5.0;
            double half = size / 2.0;
            Box area = new Box(
                    client.player.getX() - half,
                    client.player.getY() - 1.0,
                    client.player.getZ() - half,
                    client.player.getX() + half,
                    client.player.getY() + 2.0,
                    client.player.getZ() + half);
            return new State(
                    ShapeType.SQUARE,
                    0.0,
                    half,
                    FtHelperFeature.hasAnyTarget(client, area, -1.0, client.player.getX(), client.player.getZ()),
                    client.player.getPos());
        }
        return State.NONE;
    }

    /**
     * Только столкновение с блоками — снежок не «прилипает» к мобам/игрокам по траектории.
     */
    private static Vec3d predictSnowballBlockLanding(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }
        Vec3d pos = player.getEyePos();
        Vec3d vel = player.getRotationVec(1.0f).multiply(SNOWBALL_THROW_SPEED).add(player.getVelocity());
        for (int step = 0; step < 256; step++) {
            Vec3d nextVel = vel.multiply(SNOWBALL_DRAG).add(0.0, -SNOWBALL_GRAVITY, 0.0);
            Vec3d nextPos = pos.add(nextVel);
            HitResult blockHit = client.world.raycast(new RaycastContext(
                    pos,
                    nextPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player));
            if (blockHit.getType() != HitResult.Type.MISS) {
                return blockHit.getPos();
            }
            pos = nextPos;
            vel = nextVel;
            if (pos.y < client.world.getBottomY()) {
                break;
            }
        }
        return null;
    }

    private static Item activeItem(ItemStack main, ItemStack off) {
        if (FtHelperFeature.isFtItem(main.getItem())) {
            return main.getItem();
        }
        if (FtHelperFeature.isFtItem(off.getItem())) {
            return off.getItem();
        }
        return null;
    }

    private static boolean isFtItem(Item item) {
        return item == Items.SUGAR
                || item == Items.ENDER_EYE
                || item == Items.FIRE_CHARGE
                || item == Items.NETHERITE_SCRAP
                || item == Items.SNOWBALL;
    }

    public static boolean hasAnyLivingInBox(MinecraftClient client, Box area) {
        return FtHelperFeature.hasAnyTarget(client, area, -1.0, 0.0, 0.0);
    }

    private static boolean hasAnyTarget(MinecraftClient client, Box area, double circleSq, double cx, double cz) {
        for (Entity e : client.world.getOtherEntities(client.player, area)) {
            if (!(e instanceof LivingEntity) || e.isRemoved() || !e.isAlive()) {
                continue;
            }
            if (circleSq > 0.0) {
                double dx = e.getX() - cx;
                double dz = e.getZ() - cz;
                if (dx * dx + dz * dz > circleSq) {
                    continue;
                }
            }
            return true;
        }
        return false;
    }

    public record State(ShapeType shapeType, double radius, double halfSquare, boolean hitTarget, Vec3d center) {
        public static final State NONE = new State(ShapeType.NONE, 0.0, 0.0, false, Vec3d.ZERO);
    }

    public enum ShapeType {
        NONE,
        CIRCLE,
        SQUARE
    }
}
