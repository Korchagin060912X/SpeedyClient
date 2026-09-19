/*
 * Decompiled with CFR 0.152.
 */
package com.shampoon.pvputils.features;

import com.shampoon.pvputils.PVPUtils;

public final class FullBright {
    public static final int GAMMA_USER_MIN = 100;
    public static final int GAMMA_USER_MAX = 2000;

    public static double getAppliedGammaDouble() {
        int user = PVPUtils.CONFIG.fullBrightEnabled ? PVPUtils.CONFIG.fullBrightGammaOn : PVPUtils.CONFIG.fullBrightGammaOff;
        return FullBright.toMcGamma(user);
    }

    public static double toMcGamma(int userScale) {
        int clamped = Math.max(100, Math.min(2000, userScale));
        return (double)clamped / 100.0;
    }
}

