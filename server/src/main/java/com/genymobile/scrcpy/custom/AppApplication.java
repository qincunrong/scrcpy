package com.genymobile.scrcpy.custom;

import com.genymobile.scrcpy.log.LogClear;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;

import java.io.File;

public class AppApplication {

    private static volatile AppApplication singleton = null;
    private String TAG = "AppApplication";
    private AppApplication() {}

    public static AppApplication getInstance() {
        if (singleton == null) {
            synchronized (AppApplication.class) {
                if (singleton == null) {
                    singleton = new AppApplication();
                }
            }
        }
        return singleton;
    }

    private void initApp() {
        //清除过期的日志
        LogClear.clearLogCache();
        //清除过期的截屏文件等临时文件
        FileUtils.deleteDir(new File(ScrcpyConfig.getTempDir()));
        initDir();
    }
    private void initDir() {
        try {
            String cacheDir = ScrcpyConfig.getTempDir();
            File cacheFile = new File(cacheDir);
            if (!cacheFile.exists()) {
                cacheFile.mkdirs();
            }
            Logger.i(TAG, "initDir success");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.i(TAG,"initDir exception:"+e.getMessage());
        }
    }
}
