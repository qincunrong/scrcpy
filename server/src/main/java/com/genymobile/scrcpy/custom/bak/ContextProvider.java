//// ContextProvider.java - 通过反射获取 Context
//package com.genymobile.scrcpy.custom.bak;
//
//import android.app.Application;
//import android.content.Context;
//import android.content.ContextWrapper;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.io.File;
//import java.lang.reflect.Method;
//
//public class ContextProvider {
//    private static final String TAG = "ContextProvider";
//    private static Context applicationContext = null;
//
//    /**
//     * 获取 Application Context
//     */
//    public static synchronized Context getApplicationContext() {
//        if (applicationContext != null) {
//            return applicationContext;
//        }
//
//        try {
//            // 方法1：通过 ActivityThread 获取
//            applicationContext = getContextViaActivityThread();
//            if (applicationContext != null) {
//                Log.i(TAG, "Got context via ActivityThread");
//                return applicationContext;
//            }
//
//            // 方法2：通过 ServiceManager 获取
//            applicationContext = getContextViaServiceManager();
//            if (applicationContext != null) {
//                Log.i(TAG, "Got context via ServiceManager");
//                return applicationContext;
//            }
//
//            // 方法3：创建模拟 Context
////            applicationContext = createMockContext();
////            if (applicationContext != null) {
////                Log.i(TAG, "Created mock context");
////                return applicationContext;
////            }
//
//            Log.e(TAG, "All methods failed to get context");
//            return null;
//
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to get application context", e);
//            return null;
//        }
//    }
//
//    /**
//     * 方法1：通过 ActivityThread 获取 Context
//     */
//    private static Context getContextViaActivityThread() {
//        try {
//            // 获取当前应用的 ActivityThread
//            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
//            Method currentActivityThreadMethod =
//                activityThreadClass.getDeclaredMethod("currentActivityThread");
//            Object activityThread = currentActivityThreadMethod.invoke(null);
//
//            if (activityThread == null) {
//                Log.d(TAG, "ActivityThread is null, app may not be fully initialized");
//                return null;
//            }
//
//            // 获取 Application
//            Method getApplicationMethod = activityThreadClass.getMethod("getApplication");
//            Application app = (Application) getApplicationMethod.invoke(activityThread);
//
//            if (app != null) {
//                return app.getApplicationContext();
//            }
//
//            // 尝试通过 getSystemContext
//            Method getSystemContextMethod = activityThreadClass.getMethod("getSystemContext");
//            Context systemContext = (Context) getSystemContextMethod.invoke(activityThread);
//
//            if (systemContext != null) {
//                return systemContext;
//            }
//
//        } catch (Exception e) {
//            Log.w(TAG, "Failed to get context via ActivityThread: " + e.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * 方法2：通过 ServiceManager 获取系统 Context
//     */
//    private static Context getContextViaServiceManager() {
//        try {
//            // 获取 ServiceManager
//            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
//
//            // 获取 ActivityManagerService
//            Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
//            IBinder binder = (IBinder) getServiceMethod.invoke(null, "activity");
//
//            if (binder == null) {
//                return null;
//            }
//
//            // 获取 IActivityManager
//            Class<?> iActivityManagerStubClass =
//                Class.forName("android.app.IActivityManager$Stub");
//            Method asInterfaceMethod = iActivityManagerStubClass.getMethod("asInterface", IBinder.class);
//            Object activityManager = asInterfaceMethod.invoke(null, binder);
//
//            // 通过反射获取 Context
//            Class<?> activityManagerClass = activityManager.getClass();
//            Method getContextMethod = activityManagerClass.getMethod("getContext");
//            Context context = (Context) getContextMethod.invoke(activityManager);
//
//            return context;
//
//        } catch (Exception e) {
//            Log.w(TAG, "Failed to get context via ServiceManager: " + e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * 方法3：创建模拟 Context（有限功能）
//     */
////    private static Context createMockContext() {
////        try {
////            // 创建一个基本的 ContextWrapper
////            Context baseContext = getBaseContextFromClassLoader();
////            if (baseContext != null) {
////                return new ScrcpyContextWrapper(baseContext);
////            }
////
////            // 如果连基础 Context 都没有，创建一个空实现
////            return new ScrcpyContext();
////
////        } catch (Exception e) {
////            Log.w(TAG, "Failed to create mock context: " + e.getMessage());
////            return null;
////        }
////    }
//
//    /**
//     * 从 ClassLoader 获取基础 Context
//     */
//    private static Context getBaseContextFromClassLoader() {
//        try {
//            // 尝试获取系统 Context
//            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
//            Method getSystemContextMethod = contextImplClass.getMethod("getSystemContext");
//            return (Context) getSystemContextMethod.invoke(null);
//
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    /**
//     * 自定义 ContextWrapper，提供必要的功能
//     */
//    private static class ScrcpyContextWrapper extends ContextWrapper {
//        public ScrcpyContextWrapper(Context base) {
//            super(base);
//        }
//
//        // 可以重写方法以适应 scrcpy 的需求
//        @Override
//        public File getFilesDir() {
//            // scrcpy-server 通常运行在 /data/local/tmp/
//            File defaultDir = new File("/data/local/tmp/scrcpy");
//            if (!defaultDir.exists()) {
//                defaultDir.mkdirs();
//            }
//            return defaultDir;
//        }
//
//        @Override
//        public File getCacheDir() {
//            return getFilesDir();
//        }
//    }
//
//    /**
//     * 最小化的 Context 实现
//     */
//   /* private static class ScrcpyContext extends Context {
//        private final File filesDir;
//
//        public ScrcpyContext() {
//            this.filesDir = new File("/data/local/tmp/scrcpy");
//            if (!filesDir.exists()) {
//                filesDir.mkdirs();
//            }
//        }
//
//        @Override
//        public File getFilesDir() {
//            return filesDir;
//        }
//
//        @Override
//        public File getCacheDir() {
//            return filesDir;
//        }
//
//        @Override
//        public ContentResolver getContentResolver() {
//            return null;
//        }
//
//        @Override
//        public PackageManager getPackageManager() {
//            return null;
//        }
//
//        @Override
//        public Context getApplicationContext() {
//            return this;
//        }
//
//        @Override
//        public String getPackageName() {
//            return "com.genymobile.scrcpy";
//        }
//
//        @Override
//        public ApplicationInfo getApplicationInfo() {
//            return new ApplicationInfo();
//        }
//
//        @Override
//        public Resources getResources() {
//            return null;
//        }
//
//        @Override
//        public ClassLoader getClassLoader() {
//            return ContextProvider.class.getClassLoader();
//        }
//
//        @Override
//        public AssetManager getAssets() {
//            // 尝试获取 AssetManager
//            try {
//                Class<?> assetManagerClass = Class.forName("android.content.res.AssetManager");
//                return (AssetManager) assetManagerClass.newInstance();
//            } catch (Exception e) {
//                return null;
//            }
//        }
//    }
//    */
//    /**
//     * 清理资源
//     */
//    public static void release() {
//        applicationContext = null;
//    }
//}