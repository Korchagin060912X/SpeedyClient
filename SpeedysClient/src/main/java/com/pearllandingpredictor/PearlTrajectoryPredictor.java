/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_3966
 */
package com.pearllandingpredictor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;

final class PearlTrajectoryPredictor {
    static final double DRAG_DEFAULT = 0.99;
    static final double GRAVITY_PEARL = 0.03;
    static final double GRAVITY_ARROW = 0.05;
    static final double GRAVITY_TRIDENT = 0.05;

    PearlTrajectoryPredictor() {
    }

    static Prediction predict(class_1937 world, class_1297 pearlEntity, int maxSteps) {
        return PearlTrajectoryPredictor.simulate(world, pearlEntity, pearlEntity.method_19538(), pearlEntity.method_18798(), maxSteps, 0.03, 0.99);
    }

    static Prediction predict(class_1937 world, class_1297 collisionSource, class_243 startPos, class_243 startVelocity, int maxSteps) {
        return PearlTrajectoryPredictor.simulate(world, collisionSource, startPos, startVelocity, maxSteps, 0.03, 0.99);
    }

    static Prediction predict(class_1937 world, class_1297 collisionSource, class_243 startPos, class_243 startVelocity, int maxSteps, double gravity, double drag) {
        return PearlTrajectoryPredictor.simulate(world, collisionSource, startPos, startVelocity, maxSteps, gravity, drag);
    }

    private static Prediction simulate(class_1937 world, class_1297 collisionSource, class_243 startPos, class_243 startVelocity, int maxSteps, double gravity, double drag) {
        class_243 pos = startPos;
        class_243 vel = startVelocity;
        ObjectArrayList points = new ObjectArrayList(Math.max(4, maxSteps + 1));
        points.add((Object)pos);
        class_243 landing = null;
        for (int i = 0; i < maxSteps; ++i) {
            class_243 nextVel = vel.method_1021(drag).method_1031(0.0, -gravity, 0.0);
            class_243 nextPos = pos.method_1019(nextVel);
            class_3965 hit = world.method_17742(new class_3959(pos, nextPos, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, collisionSource));
            if (hit.method_17783() != class_239.class_240.field_1333) {
                landing = hit.method_17784();
                points.add((Object)landing);
                break;
            }
            class_3966 entityHit = PearlTrajectoryPredictor.raycastEntity(world, collisionSource, pos, nextPos);
            if (entityHit != null) {
                landing = entityHit.method_17784();
                points.add((Object)landing);
                break;
            }
            points.add((Object)nextPos);
            pos = nextPos;
            vel = nextVel;
            if (pos.field_1351 < (double)(world.method_31607() - 64)) break;
        }
        return new Prediction((List<class_243>)points, landing);
    }

    private static class_3966 raycastEntity(class_1937 world, class_1297 source, class_243 from, class_243 to) {
        class_238 checkBox = new class_238(from, to).method_1014(0.35);
        class_3966 bestHit = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (class_1297 entity : world.method_8333(source, checkBox, e -> e.method_5805() && !e.method_31481() && e.method_5863())) {
            class_243 hitPos;
            double distSq;
            class_238 targetBox = entity.method_5829().method_1014(0.25);
            Optional hit = targetBox.method_992(from, to);
            if (hit.isEmpty() || !((distSq = from.method_1025(hitPos = (class_243)hit.get())) < bestDistanceSq)) continue;
            bestDistanceSq = distSq;
            bestHit = new class_3966(entity, hitPos);
        }
        return bestHit;
    }

    static final class Prediction {
        final List<class_243> points;
        final class_243 landingPos;

        Prediction(List<class_243> points, class_243 landingPos) {
            this.points = points;
            this.landingPos = landingPos;
        }
    }
}

