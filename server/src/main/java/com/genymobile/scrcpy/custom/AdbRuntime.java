package com.genymobile.scrcpy.custom;

import java.io.IOException;

public class AdbRuntime {

    public static void sendNetworkVpnBroadcast(String host,String port,String excludeHost) {
        try {
            String cmd = "adb shell am broadcast -a com.yzp.action.custom_boot -p com.ysten.ystcloudscreen";
            Runtime.getRuntime().exec("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
