package com.shampoon.speedysclient.features;

/**
 * Плавный цикл по стопам: 2 цвета — туда-обратно; 3 — 1→2→3→1. Без анимации или одна стопа — цвет 1.
 */
public final class VisualRgbGradient {
    public static final long PERIOD_MS = 6000L;

    private VisualRgbGradient() {
    }

    public static void sampleRgb(
            long nowMs,
            int r1,
            int g1,
            int b1,
            int r2,
            int g2,
            int b2,
            int r3,
            int g3,
            int b3,
            int colorStops,
            boolean animate,
            int[] outRgb) {
        int stops = Math.max(1, Math.min(3, colorStops));
        if (!animate || stops <= 1) {
            outRgb[0] = r1;
            outRgb[1] = g1;
            outRgb[2] = b1;
            return;
        }
        float u = (nowMs % PERIOD_MS) / (float) PERIOD_MS;
        if (stops == 2) {
            float seg = u * 2.0f;
            if (seg < 1.0f) {
                lerpRgb(r1, g1, b1, r2, g2, b2, seg, outRgb);
            } else {
                lerpRgb(r2, g2, b2, r1, g1, b1, seg - 1.0f, outRgb);
            }
            return;
        }
        float seg = u * 3.0f;
        int k = (int) seg;
        float t = seg - k;
        if (k <= 0) {
            lerpRgb(r1, g1, b1, r2, g2, b2, t, outRgb);
        } else if (k == 1) {
            lerpRgb(r2, g2, b2, r3, g3, b3, t, outRgb);
        } else {
            lerpRgb(r3, g3, b3, r1, g1, b1, t, outRgb);
        }
    }

    private static void lerpRgb(int r1, int g1, int b1, int r2, int g2, int b2, float t, int[] out) {
        out[0] = Math.round(r1 + (r2 - r1) * t);
        out[1] = Math.round(g1 + (g2 - g1) * t);
        out[2] = Math.round(b1 + (b2 - b1) * t);
        out[0] = clamp255(out[0]);
        out[1] = clamp255(out[1]);
        out[2] = clamp255(out[2]);
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
