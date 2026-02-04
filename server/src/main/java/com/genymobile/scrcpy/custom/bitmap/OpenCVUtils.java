//package com.genymobile.scrcpy.custom.bitmap;
//
//import android.util.Log;
//
//import com.genymobile.scrcpy.custom.ScrcpyConfig;
//
//import org.opencv.android.OpenCVLoader;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//
//public class OpenCVUtils {
//    private static boolean isInitialized = false;
//    public static final String TAG = ScrcpyConfig.getLogGroup()+"OpenCVUtils";
//
//    public static void init() {
//        if (!isInitialized) {
//            try {
//                Log.i(TAG, "init start");
////                System.loadLibrary("opencv_java4");
////                System.loadLibrary("opencv_imgproc");
//                boolean success = OpenCVLoader.initDebug();
//                if (success) {
//                    Log.i(TAG, "OpenCV loaded successfully");
//                } else {
//                    Log.e(TAG, "OpenCV loading failed");
//                }
//                Log.i(TAG, "init success");
//                isInitialized = true;
//                // 可选：测试一个简单的 OpenCV 操作
//                testOpenCV();
//            } catch (UnsatisfiedLinkError e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static void testOpenCV() {
//        Log.i(TAG, "testOpenCV start");
//        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
//        Log.i(TAG, "testOpenCV end:"+mat);
//    }
//
//    public static boolean isInitialized() {
//        return isInitialized;
//    }
//}