// TessSoLoader.java - 手动加载so库
package com.genymobile.scrcpy.custom;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsExtractor {
    private static final String TAG = OcrConfig.getLogGroup()+"AssetsFileExtractor";
    private static boolean isLoaded = false;
    private static String mBaseDir=OcrConfig.getBaseAssetsDir();
    private static final String ASSETS_SO_DIR = "/assets";
    private static final String[] ASSETS_FILES = {
            "chi_sim.traineddata",
            "eng.traineddata",
            "test1.jpg",
    };

    public static synchronized void extractAssets() {
        if (isLoaded) {
            return;
        }
        try {
            // 2. 提取 so 库到临时目录
            File extractAssets = null;
            try {
                extractAssets = extractAssetsLibraries();
                Log.i(TAG, "extractAssets:" + extractAssets.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "extractAssets exception:" + e.getMessage());
            }
            isLoaded = true;
            Log.i(TAG, "extractAssets load all success");
        } catch (Exception e) {
            Log.e(TAG, "extractAssets, failed", e);
            throw new RuntimeException("nativeLoadingLib, failed", e);
        }
    }
    private static File extractAssetsLibraries() throws IOException {
        // 创建临时目录
        File tempDir = new File(mBaseDir, "tessdata");
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                throw new IOException("Failed to create temp directory");
            }
        }
        
        // 清理旧文件
        File[] oldFiles = tempDir.listFiles();
        if (oldFiles != null) {
            for (File file : oldFiles) {
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete old file: " + file.getAbsolutePath());
                }
            }
        }
        
        // 从 assets 复制 so 文件
        for (String libName : ASSETS_FILES) {
            String assetFile = ASSETS_SO_DIR + "/"  + libName;
            try {
                Log.i(TAG, "assetFile:" + assetFile);
//                InputStream in = context.getAssets().open(assetPath);
                InputStream in = AssetsExtractor.class.getResourceAsStream(assetFile);
                Log.i(TAG, "assetFile in:" + in);
                OutputStream out = new FileOutputStream(new File(tempDir, libName));
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                Log.d(TAG, "assetFile copy success: " + libName);
            } catch (IOException e) {
                // 尝试其他可能的路径
                e.printStackTrace();
            }
        }
        return tempDir;
    }

}