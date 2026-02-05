package com.genymobile.scrcpy.custom;

public class ScrcpyConfig {
    public static String getBaseDir() {
        return "/data/local/tmp/scrcpy";
    }

    public static String getBaseAssetsDir() {
        return "/data/local/tmp/scrcpy/assets";
    }

    public static String getLogGroup() {
        return "SCR-";
    }

    public static String getLogDir() {
        return "/data/local/tmp/scrcpy/log";
    }

    public static String getTempDir() {
        return "/data/local/tmp/scrcpy/temp";
    }
}
