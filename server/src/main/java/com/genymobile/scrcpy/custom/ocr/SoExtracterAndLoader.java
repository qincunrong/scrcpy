// TessSoLoader.java - 手动加载so库
package com.genymobile.scrcpy.custom.ocr;

import android.content.Context;
import android.os.Build;
import android.system.Os;
import android.util.Log;

import com.genymobile.scrcpy.custom.OcrConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

public class SoExtracterAndLoader {
    private static final String TAG = OcrConfig.getLogGroup()+ "TessSoLoader";
    private static boolean isLoaded = false;
    
    // tess-two 依赖的 so 库列表（按依赖顺序）
    private static final String[] SO_LIBRARIES = {
        "libpngt.so",
        "libjpgt.so",
        "liblept.so",
        "libtess.so",
    };
    
    // 从 AAR 中提取的 so 库路径（假设我们将 so 文件放在 assets 中）
//    private static final String ASSETS_SO_DIR = "libs";
    private static final String JAR_SO_DIR = "/lib";

    /**
     * 手动加载所有依赖的 so 库
     */
    public static synchronized void loadTessLibraries(String baseDir) {
        if (isLoaded) {
            return;
        }
        
        try {
            // 1. 获取设备 ABI
            String abi = getDeviceAbi();
            Log.i(TAG, "nativeDeviceABI: " + abi);
            
            // 2. 提取 so 库到临时目录
            File nativeLibDir = null;
            try {
                nativeLibDir = extractSoLibraries(baseDir, abi);
                Log.i(TAG, "nativeLibDir:" + nativeLibDir.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "extractSoLibraries exception:" + e.getMessage());
            }

            // 3. 修改 LD_LIBRARY_PATH
            addLibraryPath(nativeLibDir.getAbsolutePath());
            
            // 4. 按顺序加载 so 库
            for (String libName : SO_LIBRARIES) {
                String libPath = new File(nativeLibDir, libName).getAbsolutePath();
                try {
                    Log.i(TAG, "nativeLoadingLib: " + libPath);
                    System.load(libPath);
                    Log.i(TAG, "nativeLoadingLib: " + libPath+", load success");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            isLoaded = true;
            Log.i(TAG, "nativeLoadingLib load all success");
        } catch (Exception e) {
            Log.e(TAG, "nativeLoadingLib, failed", e);
            throw new RuntimeException("nativeLoadingLib, failed", e);
        }
    }
    
    /**
     * 获取设备 ABI
     */
    private static String getDeviceAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] abis = Build.SUPPORTED_ABIS;
            // 优先级：arm64-v8a > armeabi-v7a > x86_64 > x86
            for (String abi : abis) {
                if (abi.equals("arm64-v8a") || abi.equals("armeabi-v7a") || 
                    abi.equals("x86_64") || abi.equals("x86")) {
                    return abi;
                }
            }
            return abis[0];
        } else {
            return Build.CPU_ABI;
        }
    }
    
    /**
     * 从 assets 提取 so 库到临时目录
     */
    private static File extractSoLibraries(String baseFileDir, String abi) throws IOException {
        // 创建临时目录
        File tempDir = new File(baseFileDir, "scrcpyNativeLibs");
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
        for (String libName : SO_LIBRARIES) {
            String srcSoPath = JAR_SO_DIR + "/" + abi + "/" + libName;
            try {
                Log.i(TAG, "srcSoPath:" + srcSoPath);
//                InputStream in = context.getAssets().open(assetPath);
                InputStream in = SoExtracterAndLoader.class.getResourceAsStream(srcSoPath);
                Log.i(TAG, "srcSoPath in:" + in);
                OutputStream out = new FileOutputStream(new File(tempDir, libName));
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                Log.d(TAG, "Extracted: " + libName);
            } catch (IOException e) {
                // 尝试其他可能的路径
                String[] possiblePaths = {
                        JAR_SO_DIR + "/" + abi + "/" + libName,
                    "lib/" + abi + "/" + libName,
                    abi + "/" + libName
                };
                
                boolean extracted = false;
                for (String path : possiblePaths) {
                    try  {
//                        InputStream in = context.getAssets().open(path);
                        InputStream in =  SoExtracterAndLoader.class.getResourceAsStream(srcSoPath);
                        FileOutputStream out = new FileOutputStream(new File(tempDir, libName));
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        out.close();
                        extracted = true;
                        Log.d(TAG, "Extracted from alternative path: " + path);
                        break;
                    } catch (IOException ignored) {
                        // 继续尝试下一个路径
                    }
                }
                
                if (!extracted) {
                    Log.w(TAG, "Could not find library in assets: " + libName);
                }
            }
        }
        
        // 设置执行权限
        for (File libFile : tempDir.listFiles()) {
            if (libFile.getName().endsWith(".so")) {
                libFile.setExecutable(true, false);
            }
        }
        
        return tempDir;
    }
    
    /**
     * 修改 LD_LIBRARY_PATH 环境变量
     * 注意：这种方法在高版本 Android 上可能受限
     */
    private static void addLibraryPath(String libPath) {
        try {
            // 方法1：直接设置环境变量（需要系统权限）
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Os.setenv("LD_LIBRARY_PATH", libPath + ":" + System.getenv("LD_LIBRARY_PATH"), true);
            }
            
            // 方法2：通过反射修改 ClassLoader 的 library path
            addLibraryPathToClassLoader(libPath);
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to modify library path: " + e.getMessage());
        }
    }
    
    /**
     * 通过反射修改 ClassLoader 的 native library path
     */
    private static void addLibraryPathToClassLoader(String libPath) throws Exception {
        // 获取当前 ClassLoader
        ClassLoader classLoader = SoExtracterAndLoader.class.getClassLoader();
        
        if (classLoader instanceof java.net.URLClassLoader) {
            // Android 7.0 之前
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            
            for (String path : paths) {
                if (path.equals(libPath)) {
                    return; // 已经存在
                }
            }
            
            String[] newPaths = new String[paths.length + 1];
            System.arraycopy(paths, 0, newPaths, 0, paths.length);
            newPaths[paths.length] = libPath;
            field.set(null, newPaths);
            
        } else {
            // Android 7.0+ 使用 PathClassLoader
            try {
                Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader")
                        .getDeclaredField("pathList");
                pathListField.setAccessible(true);
                Object pathList = pathListField.get(classLoader);
                
                Field nativeLibraryDirectoriesField = pathList.getClass()
                        .getDeclaredField("nativeLibraryDirectories");
                nativeLibraryDirectoriesField.setAccessible(true);
                
                @SuppressWarnings("unchecked")
                List<File> nativeLibraryDirectories = 
                    (List<File>) nativeLibraryDirectoriesField.get(pathList);
                
                nativeLibraryDirectories.add(0, new File(libPath));
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to modify ClassLoader library path", e);
            }
        }
    }
    
    /**
     * 检查 so 库是否已加载
     */
    public static boolean isTessLibrariesLoaded() {
        return isLoaded;
    }
    
    /**
     * 清理临时文件
     */
    public static void cleanup(Context context) {
        try {
            File tempDir = new File(context.getFilesDir(), "scrcpyNativeLibs");
            if (tempDir.exists()) {
                for (File file : tempDir.listFiles()) {
                    file.delete();
                }
                tempDir.delete();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to cleanup temp files", e);
        }
    }
}