package com.shampoon.speedysclient.features;

import com.shampoon.pvputils.PVPUtils;

public final class CustomRatioFeature {
    private static final float MIN_RATIO = 4.0f / 3.0f;
    private static final float MAX_RATIO = 16.0f / 9.0f;

    private CustomRatioFeature() {
    }

    public static int applyToWidth(int originalWidth, int sourceHeight) {
        if (PVPUtils.CONFIG == null || !PVPUtils.CONFIG.customRatioEnabled) {
            return originalWidth;
        }
        float ratio = parseRatio(PVPUtils.CONFIG.customRatioValue);
        if (ratio <= 0.0f || sourceHeight <= 0) {
            return originalWidth;
        }
        int adjusted = Math.max(1, Math.round(sourceHeight * ratio));
        return adjusted;
    }

    private static float parseRatio(String raw) {
        if (raw == null) {
            return -1.0f;
        }
        String value = raw.trim();
        int sep = value.indexOf(':');
        if (sep <= 0 || sep >= value.length() - 1) {
            return -1.0f;
        }
        try {
            float w = Float.parseFloat(value.substring(0, sep).trim());
            float h = Float.parseFloat(value.substring(sep + 1).trim());
            if (w <= 0.0f || h <= 0.0f) {
                return -1.0f;
            }
            float ratio = w / h;
            if (ratio < MIN_RATIO || ratio > MAX_RATIO) {
                return -1.0f;
            }
            return ratio;
        } catch (NumberFormatException ignored) {
            return -1.0f;
        }
    }
}
