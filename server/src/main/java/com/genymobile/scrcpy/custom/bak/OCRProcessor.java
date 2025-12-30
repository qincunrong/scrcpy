//package com.genymobile.scrcpy.custom.ocr;
//
//import android.app.Application;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.util.Log;
//
//import com.googlecode.tesseract.android.TessBaseAPI;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//
//public class OCRProcessor {
//    private static final String TAG = "OCRProcessor";
//    private TessBaseAPI tessBaseAPI;
//    private String dataPath;
//    private boolean initialized = false;
//
//    public OCRProcessor() {
//        initTesseract();
//    }
//
//    private void initTesseract() {
//        try {
//            ActivityThread activityThread = ActivityThread.currentActivityThread();
//
//            // 获取 Application
//            Application application = activityThread.getApplication();
//
//            // 获取 Context
//            Context context = application.getApplicationContext();
//            dataPath = context.getFilesDir() + "/tesseract/";
//
//            // 初始化Tesseract
//            tessBaseAPI = new TessBaseAPI();
//
//            // 检查训练数据
//            checkTrainingData(context, "eng");
//            checkTrainingData(context, "chi_sim");
//
//            // 初始化引擎
//            if (tessBaseAPI.init(dataPath, "eng+chi_sim")) {
//                initialized = true;
//                Log.i(TAG, "Tesseract initialized successfully");
//
//                // 设置配置
//                tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
//                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
//                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!?@#$%&*()-_=+[]{};:'\"\\|<>/`~ ");
//                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "");
//
//            } else {
//                Log.e(TAG, "Tesseract initialization failed");
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to initialize OCR", e);
//        }
//    }
//
//    private void checkTrainingData(Context context, String language) {
//        try {
//            File dir = new File(dataPath + "tessdata/");
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String trainedDataFile = language + ".traineddata";
//            File file = new File(dataPath + "tessdata/" + trainedDataFile);
//
//            if (!file.exists()) {
//                Log.i(TAG, "Copying " + trainedDataFile + " to " + file.getAbsolutePath());
//
//                // 从assets复制
//                InputStream in = context.getAssets().open("tessdata/" + trainedDataFile);
//                FileOutputStream out = new FileOutputStream(file);
//
//                byte[] buffer = new byte[1024];
//                int read;
//                while ((read = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, read);
//                }
//                out.flush();
//                out.close();
//                in.close();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to copy training data for " + language, e);
//        }
//    }
//
//    public String recognize(Bitmap bitmap) {
//        if (!initialized || tessBaseAPI == null) {
//            return null;
//        }
//
//        try {
//            // 预处理图像
//            Bitmap processed = preprocessBitmap(bitmap);
//
//            // 设置图像
//            tessBaseAPI.setImage(processed);
//
//            // 获取文本
//            String text = tessBaseAPI.getUTF8Text();
//
//            // 清理
//            tessBaseAPI.clear();
//
//            // 后处理文本
//            return postProcessText(text);
//
//        } catch (Exception e) {
//            Log.e(TAG, "OCR recognition failed", e);
//            return null;
//        }
//    }
//
//    private Bitmap preprocessBitmap(Bitmap original) {
//        // 可以在这里添加图像预处理逻辑
//        // 如缩放、二值化、去噪等
//        return original;
//    }
//
//    private String postProcessText(String text) {
//        if (text == null) {
//            return "";
//        }
//
//        // 清理文本
//        text = text.trim();
//        text = text.replaceAll("\\s+", " ");
//
//        return text;
//    }
//
//    public void destroy() {
//        if (tessBaseAPI != null) {
//            tessBaseAPI.end();
//        }
//    }
//}