package com.genymobile.scrcpy.custom;

import android.annotation.SuppressLint;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class VpnServiceCaller {
    public static void setNetworkVpn(String host, String port, String excludeHost) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("host", host);
            jsonObject.put("port", port);
            jsonObject.put("excludeHost", excludeHost);
            //获得ServiceManager类
            @SuppressLint("PrivateApi") Class ServiceManager = Class
                    .forName("android.os.ServiceManager");
            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, "vpn_service");
            //IVpnManager.Stub类
            @SuppressLint("PrivateApi") Class cStub = Class
                    .forName("android.os.IVpnManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IVpnManager对象
            Object oIVpnManager = asInterface.invoke(null, oRemoteService);
            //获得setVpn方法
            Method shutdown = oIVpnManager.getClass().getMethod("setVpn", String.class);
            //调用shutdown()方法
            shutdown.invoke(oIVpnManager, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
