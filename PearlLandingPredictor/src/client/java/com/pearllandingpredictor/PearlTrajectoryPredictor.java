/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  net.minecraft.world.World
 */
package com.pearllandingpredictor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

final class PearlTrajectoryPredictor {
    static final double DRAG_DEFAULT = 0.99;
    static final double GRAVITY_PEARL = 0.03;
    static final double GRAVITY_ARROW = 0.05;
    static final double GRAVITY_TRIDENT = 0.05;

    PearlTrajectoryPredictor() {
    }

    static Prediction predict(World world, Entity pearlEntity, int maxSteps) {
        return PearlTrajectoryPredictor.simulate(world, pearlEntity, pearlEntity.getPos(), pearlEntity.getVelocity(), maxSteps, 0.03, 0.99);
    }

    static Prediction predict(World world, Entity collisionSource, Vec3d startPos, Vec3d startVelocity, int maxSteps) {
        return PearlTrajectoryPredictor.simulate(world, collisionSource, startPos, startVelocity, maxSteps, 0.03, 0.99);
    }

    static Prediction predict(World world, Entity collisionSource, Vec3d startPos, Vec3d startVelocity, int maxSteps, double gravity, double drag) {
        return PearlTrajectoryPredictor.simulate(world, collisionSource, startPos, startVelocity, maxSteps, gravity, drag);
    }

    private static Prediction simulate(World world, Entity collisionSource, Vec3d startPos, Vec3d startVelocity, int maxSteps, double gravity, double drag) {
        Vec3d pos = startPos;
        Vec3d vel = startVelocity;
        ObjectArrayList points = new ObjectArrayList(Math.max(4, maxSteps + 1));
        points.add((Object)pos);
        Vec3d landing = null;
        for (int i = 0; i < maxSteps; ++i) {
            Vec3d nextVel = vel.multiply(drag).add(0.0, -gravity, 0.0);
            Vec3d nextPos = pos.add(nextVel);
            BlockHitResult hit = world.raycast(new RaycastContext(pos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, collisionSource));
            if (hit.getType() != HitResult.Type.MISS) {
                landing = hit.getPos();
                points.add((Object)landing);
                break;
            }
            EntityHitResult entityHit = PearlTrajectoryPredictor.raycastEntity(world, collisionSource, pos, nextPos);
            if (entityHit != null) {
                landing = entityHit.getPos();
                points.add((Object)landing);
                break;
            }
            points.add((Object)nextPos);
            pos = nextPos;
            vel = nextVel;
            if (pos.y < (double)(world.getBottomY() - 64)) break;
        }
        return new Prediction((List<Vec3d>)points, landing);
    }

    private static EntityHitResult raycastEntity(World world, Entity source, Vec3d from, Vec3d to) {
        Box checkBox = new Box(from, to).expand(0.35);
        EntityHitResult bestHit = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (Entity entity : world.getOtherEntities(source, checkBox, e -> e.isAlive() && !e.isRemoved() && e.canHit())) {
            Vec3d hitPos;
            double distSq;
            Box targetBox = entity.getBoundingBox().expand(0.25);
            Optional hit = targetBox.raycast(from, to);
            if (hit.isEmpty() || !((distSq = from.squaredDistanceTo(hitPos = (Vec3d)hit.get())) < bestDistanceSq)) continue;
            bestDistanceSq = distSq;
            bestHit = new EntityHitResult(entity, hitPos);
        }
        return bestHit;
    }

    static final class Prediction {
        final List<Vec3d> points;
        final Vec3d landingPos;

        Prediction(List<Vec3d> points, Vec3d landingPos) {
            this.points = points;
            this.landingPos = landingPos;
        }
    }
}

