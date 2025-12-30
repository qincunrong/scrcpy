//// OcRService.java
//package com.genymobile.scrcpy.custom.ocr;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ImageFormat;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.YuvImage;
//import android.hardware.display.VirtualDisplay;
//import android.media.Image;
//import android.media.ImageReader;
//import android.media.projection.MediaProjection;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.DisplayMetrics;
//import android.util.Log;
//
//import com.googlecode.tesseract.android.TessBaseAPI;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class OcrService {
//    private static final String TAG = "OcrService";
//
//    private Context context;
//    private TessBaseAPI tessBaseAPI;
//    private boolean isInitialized = false;
//    private String language = "eng"; // 默认英文
//    private ExecutorService executorService;
//    private Handler mainHandler;
//
//    // OCR结果回调接口
//    public interface OcrCallback {
//        void onOcrResult(String text, List<OcrResultItem> items, long processTime);
//        void onOcrError(String error);
//    }
//
//    // OCR结果项
//    public static class OcrResultItem {
//        public String text;
//        public Rect boundingBox;
//        public float confidence;
//
//        public OcrResultItem(String text, Rect box, float confidence) {
//            this.text = text;
//            this.boundingBox = box;
//            this.confidence = confidence;
//        }
//    }
//
//    public OcrService(Context context) {
//        this.context = context;
//        this.executorService = Executors.newFixedThreadPool(2);
//        this.mainHandler = new Handler(Looper.getMainLooper());
//
//        // 初始化Tesseract
//        initTesseract();
//    }
//
//    private void initTesseract() {
//        executorService.execute(() -> {
//            try {
//                File tessDataDir = new File(context.getExternalFilesDir(null), "tessdata");
//                if (!tessDataDir.exists()) {
//                    tessDataDir.mkdirs();
//                }
//
//                // 检查语言文件是否存在
//                File engFile = new File(tessDataDir, "eng.traineddata");
//                if (!engFile.exists()) {
//                    // 从assets复制
//                    copyLanguageData(tessDataDir);
//                }
//
//                tessBaseAPI = new TessBaseAPI();
//                int result = tessBaseAPI.init(tessDataDir.getAbsolutePath(), language);
//
//                if (result == 0) {
//                    // 设置OCR参数
//                    tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
//                    tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,
//                        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!?;:-()[]{}<>'\"@#$%^&* ");
//
//                    isInitialized = true;
//                    Log.i(TAG, "Tesseract OCR initialized successfully");
//                } else {
//                    Log.e(TAG, "Failed to initialize Tesseract: " + result);
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error initializing OCR: " + e.getMessage());
//            }
//        });
//    }
//
//    private void copyLanguageData(File tessDataDir) throws IOException {
//        String[] languages = {"eng", "chi_sim", "chi_tra"};
//
//        for (String lang : languages) {
//            String fileName = lang + ".traineddata";
//            File outFile = new File(tessDataDir, fileName);
//
//            if (!outFile.exists()) {
//                try (InputStream in = context.getAssets().open("tessdata/" + fileName);
//                     OutputStream out = new FileOutputStream(outFile)) {
//                    byte[] buffer = new byte[1024];
//                    int read;
//                    while ((read = in.read(buffer)) != -1) {
//                        out.write(buffer, 0, read);
//                    }
//                    Log.i(TAG, "Copied language file: " + fileName);
//                } catch (IOException e) {
//                    Log.w(TAG, "Language file not found: " + fileName);
//                }
//            }
//        }
//    }
//
//    /**
//     * 从屏幕截图进行OCR
//     */
//    public void recognizeFromScreen(Image image, OcrCallback callback) {
//        if (!isInitialized) {
//            callback.onOcrError("OCR not initialized");
//            return;
//        }
//
//        executorService.execute(() -> {
//            long startTime = System.currentTimeMillis();
//
//            try {
//                // 转换Image为Bitmap
//                Bitmap bitmap = imageToBitmap(image);
//
//                // 预处理图像
//                Bitmap processed = preprocessImage(bitmap);
//
//                // 执行OCR
//                tessBaseAPI.setImage(processed);
//
//                // 获取文本
//                String text = tessBaseAPI.getUTF8Text();
//
//                // 获取详细的识别结果
//                List<OcrResultItem> items = getDetailedResults();
//
//                // 计算处理时间
//                long processTime = System.currentTimeMillis() - startTime;
//
//                mainHandler.post(() -> {
//                    callback.onOcrResult(text, items, processTime);
//                });
//
//                // 清理
//                tessBaseAPI.clear();
//                bitmap.recycle();
//                processed.recycle();
//
//            } catch (Exception e) {
//                mainHandler.post(() -> {
//                    callback.onOcrError("OCR error: " + e.getMessage());
//                });
//            }
//        });
//    }
//
//    /**
//     * 从Bitmap进行OCR
//     */
//    public void recognizeFromBitmap(Bitmap bitmap, Rect region, OcrCallback callback) {
//        if (!isInitialized) {
//            callback.onOcrError("OCR not initialized");
//            return;
//        }
//
//        executorService.execute(() -> {
//            long startTime = System.currentTimeMillis();
//
//            try {
//                // 裁剪指定区域
//                Bitmap cropped = Bitmap.createBitmap(bitmap,
//                    region.left, region.top, region.width(), region.height());
//
//                // 预处理图像
//                Bitmap processed = preprocessImage(cropped);
//
//                // 执行OCR
//                tessBaseAPI.setImage(processed);
//                String text = tessBaseAPI.getUTF8Text();
//
//                List<OcrResultItem> items = getDetailedResults();
//                long processTime = System.currentTimeMillis() - startTime;
//
//                mainHandler.post(() -> {
//                    callback.onOcrResult(text, items, processTime);
//                });
//
//                tessBaseAPI.clear();
//                processed.recycle();
//                cropped.recycle();
//
//            } catch (Exception e) {
//                mainHandler.post(() -> {
//                    callback.onOcrError("OCR error: " + e.getMessage());
//                });
//            }
//        });
//    }
//
//    /**
//     * 批量识别多个区域
//     */
//    public void recognizeMultipleRegions(Bitmap bitmap, List<Rect> regions,
//                                        OcrCallback callback) {
//        executorService.execute(() -> {
//            StringBuilder allText = new StringBuilder();
//            List<OcrResultItem> allItems = new ArrayList<>();
//            long totalTime = 0;
//
//            for (Rect region : regions) {
//                long startTime = System.currentTimeMillis();
//
//                try {
//                    Bitmap cropped = Bitmap.createBitmap(bitmap,
//                        region.left, region.top, region.width(), region.height());
//
//                    Bitmap processed = preprocessImage(cropped);
//                    tessBaseAPI.setImage(processed);
//
//                    String text = tessBaseAPI.getUTF8Text();
//                    List<OcrResultItem> items = getDetailedResults();
//
//                    // 调整边界框位置
//                    for (OcrResultItem item : items) {
//                        item.boundingBox.offset(region.left, region.top);
//                        allItems.add(item);
//                    }
//
//                    allText.append(text).append("\n");
//                    totalTime += System.currentTimeMillis() - startTime;
//
//                    tessBaseAPI.clear();
//                    processed.recycle();
//                    cropped.recycle();
//
//                } catch (Exception e) {
//                    Log.e(TAG, "Error in region OCR: " + e.getMessage());
//                }
//            }
//
//            final String finalText = allText.toString();
//            final long finalTime = totalTime;
//
//            mainHandler.post(() -> {
//                callback.onOcrResult(finalText, allItems, finalTime);
//            });
//        });
//    }
//
//    /**
//     * 图像预处理
//     */
//    private Bitmap preprocessImage(Bitmap bitmap) {
//        // 1. 转为灰度图
//        Bitmap grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
//            Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(grayBitmap);
//        Paint paint = new Paint();
//
//        // 灰度矩阵
//        float[] matrix = {
//            0.299f, 0.587f, 0.114f, 0, 0,
//            0.299f, 0.587f, 0.114f, 0, 0,
//            0.299f, 0.587f, 0.114f, 0, 0,
//            0, 0, 0, 1, 0
//        };
//
//        android.graphics.ColorMatrix colorMatrix = new android.graphics.ColorMatrix(matrix);
//        paint.setColorFilter(new android.graphics.ColorMatrixColorFilter(colorMatrix));
//        canvas.drawBitmap(bitmap, 0, 0, paint);
//
//        // 2. 二值化（可选）
//        Bitmap binaryBitmap = binarizeImage(grayBitmap);
//
//        // 3. 缩放（如果图像太大）
//        int maxSize = 2000;
//        if (binaryBitmap.getWidth() > maxSize || binaryBitmap.getHeight() > maxSize) {
//            float scale = Math.min((float)maxSize / binaryBitmap.getWidth(),
//                                 (float)maxSize / binaryBitmap.getHeight());
//            int newWidth = (int)(binaryBitmap.getWidth() * scale);
//            int newHeight = (int)(binaryBitmap.getHeight() * scale);
//
//            Bitmap scaledBitmap = Bitmap.createScaledBitmap(binaryBitmap,
//                newWidth, newHeight, true);
//            binaryBitmap.recycle();
//            binaryBitmap = scaledBitmap;
//        }
//
//        grayBitmap.recycle();
//        return binaryBitmap;
//    }
//
//    /**
//     * 图像二值化
//     */
//    private Bitmap binarizeImage(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        Bitmap binary = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//
//        int[] pixels = new int[width * height];
//        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//
//        // 计算自适应阈值
//        int threshold = calculateAdaptiveThreshold(pixels);
//
//        for (int i = 0; i < pixels.length; i++) {
//            int color = pixels[i];
//            int r = Color.red(color);
//            int g = Color.green(color);
//            int b = Color.blue(color);
//            int gray = (r + g + b) / 3;
//
//            if (gray > threshold) {
//                pixels[i] = Color.WHITE;
//            } else {
//                pixels[i] = Color.BLACK;
//            }
//        }
//
//        binary.setPixels(pixels, 0, width, 0, 0, width, height);
//        return binary;
//    }
//
//    /**
//     * 计算自适应阈值（大津法）
//     */
//    private int calculateAdaptiveThreshold(int[] pixels) {
//        int[] histogram = new int[256];
//
//        for (int pixel : pixels) {
//            int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
//            histogram[gray]++;
//        }
//
//        int total = pixels.length;
//        float sum = 0;
//        for (int i = 0; i < 256; i++) {
//            sum += i * histogram[i];
//        }
//
//        float sumB = 0;
//        int wB = 0;
//        int wF = 0;
//        float varMax = 0;
//        int threshold = 0;
//
//        for (int i = 0; i < 256; i++) {
//            wB += histogram[i];
//            if (wB == 0) continue;
//
//            wF = total - wB;
//            if (wF == 0) break;
//
//            sumB += i * histogram[i];
//
//            float mB = sumB / wB;
//            float mF = (sum - sumB) / wF;
//
//            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);
//
//            if (varBetween > varMax) {
//                varMax = varBetween;
//                threshold = i;
//            }
//        }
//
//        return threshold;
//    }
//
//    /**
//     * 获取详细的识别结果（包括位置和置信度）
//     */
//    private List<OcrResultItem> getDetailedResults() {
//        List<OcrResultItem> items = new ArrayList<>();
//
//        try {
//            // 获取字词级别的结果
//            tessBaseAPI.getRegions();
//            tessBaseAPI.getTextlines();
//
//            // 获取每个识别结果
//            String[] words = tessBaseAPI.getWords();
//            Rect[] wordRects = tessBaseAPI.getWordRects();
//            Float[] confidences = tessBaseAPI.getConfidences();
//
//            if (words != null && wordRects != null && confidences != null) {
//                for (int i = 0; i < words.length; i++) {
//                    if (confidences[i] > 60) { // 置信度阈值
//                        items.add(new OcrResultItem(words[i], wordRects[i], confidences[i]));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error getting detailed results: " + e.getMessage());
//        }
//
//        return items;
//    }
//
//    /**
//     * 转换Image为Bitmap
//     */
//    private Bitmap imageToBitmap(Image image) {
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer buffer = planes[0].getBuffer();
//        int pixelStride = planes[0].getPixelStride();
//        int rowStride = planes[0].getRowStride();
//        int rowPadding = rowStride - pixelStride * image.getWidth();
//
//        Bitmap bitmap = Bitmap.createBitmap(
//            image.getWidth() + rowPadding / pixelStride,
//            image.getHeight(),
//            Bitmap.Config.ARGB_8888
//        );
//        bitmap.copyPixelsFromBuffer(buffer);
//
//        // 裁剪掉填充部分
//        if (rowPadding > 0) {
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                image.getWidth(), image.getHeight());
//        }
//
//        return bitmap;
//    }
//
//    /**
//     * 设置OCR语言
//     */
//    public void setLanguage(String language) {
//        this.language = language;
//        if (isInitialized) {
//            tessBaseAPI.end();
//            initTesseract();
//        }
//    }
//
//    /**
//     * 释放资源
//     */
//    public void release() {
//        if (tessBaseAPI != null) {
//            tessBaseAPI.end();
//        }
//        if (executorService != null) {
//            executorService.shutdown();
//        }
//        isInitialized = false;
//    }
//}