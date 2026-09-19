/*
 * Decompiled with CFR 0.152.
 */
package io.github.musicintegration.platform;

public final class WindowsSupport {
    private WindowsSupport() {
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase().contains("win");
    }

    public static boolean isGsmtcSupportedOs() {
        return WindowsSupport.isWindows();
    }
}

