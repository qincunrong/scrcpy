// OptimizedOcrManager.java
package com.genymobile.scrcpy.custom.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;

import com.genymobile.scrcpy.custom.OcrConfig;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OptimizedOcrManager {
    private static final String TAG = OcrConfig.getLogGroup()+"OptimizedOcr";
    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    private static final long OCR_TIMEOUT_MS = 5000;
    
    private static OptimizedOcrManager instance;
    private ExecutorService executorService;
    private TessBaseAPI tessApi;
    private  volatile boolean isInitialized = false;
    private String currentLanguage = "chi_sim+eng";
    
    // 缓存优化
    private LruCache<String, String> textCache;
    private Map<String, Long> cacheTimestamps;
    
    // 性能监控
    private long totalProcessingTime = 0;
    private int totalRequests = 0;
    
    public static synchronized OptimizedOcrManager getInstance() {
        if (instance == null) {
            instance = new OptimizedOcrManager();
        }
        return instance;
    }
    
    private OptimizedOcrManager() {
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        
        // 初始化缓存（10MB）
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8; // 使用1/8的堆内存作为缓存
        
        textCache = new LruCache<String, String>(cacheSize) {
            @Override
            protected int sizeOf(String key, String value) {
                return value.getBytes().length / 1024;
            }
        };
        
        cacheTimestamps = new HashMap<>();
        // 异步初始化Tesseract
        initializeTesseractAsync();
    }
    
    /**
     * 异步初始化Tesseract
     */
    private void initializeTesseractAsync() {
        executorService.execute(() -> {
            try {
                long startTime = System.currentTimeMillis();
                // 准备Tesseract数据目录
                File tessDir = prepareTessData();
                if (tessDir == null) {
                    Log.e(TAG, "Failed to prepare tess data directory");
                    return;
                }
                
                // 初始化Tesseract
                tessApi = new TessBaseAPI();
                // 设置优化参数
                boolean initResult = tessApi.init(tessDir.getAbsolutePath(), currentLanguage);
                Log.i(TAG, "tessApi init result:" + initResult);
                if (initResult) {
                    // 配置OCR参数以优化性能
                    configureTesseractForPerformance();
                    isInitialized = true;
                    
                    long initTime = System.currentTimeMillis() - startTime;
                    Log.i(TAG, String.format("Tesseract initialized in %d ms", initTime));
                } else {
                    Log.e(TAG, "Tesseract initialization failed with code: " + initResult);
                    tessApi.end();
                    tessApi = null;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Tesseract", e);
            }
        });
    }
    
    /**
     * 准备TessData目录和文件
     */
    private File prepareTessData() throws IOException {
        // 数据目录：/data/local/tmp/scrcpy/assets/
        File tessDataDir = new File(OcrConfig.getBaseAssetsDir(), "tessdata");
        if (!tessDataDir.exists()) {
            if (!tessDataDir.mkdirs()) {
                Log.e(TAG, "prepareTessData, failed to create assets directory");
                return null;
            }
        }
        
        // 检查并复制语言文件
        String[] languages = {"eng", "chi_sim"};
//        for (String lang : languages) {
//            File langFile = new File(tessDataDir, lang + ".traineddata");
//            if (!langFile.exists()) {
//                copyLanguageDataFromAssets(lang, langFile);
//            }
//        }
        
        // 创建父目录（tessdata的上一级）
        return tessDataDir.getParentFile();
    }

    /**
     * 配置Tesseract性能参数
     */
    private void configureTesseractForPerformance() {
        if (tessApi == null) return;
        
        try {
            // 设置页面分割模式
            tessApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
            // 设置白名单（减少误识别）
            if (currentLanguage.startsWith("eng")) {
                tessApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, 
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
            }

            // 设置黑名单（可选）
            // tessApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+");

            // 设置识别引擎模式
            tessApi.setVariable("tessedit_ocr_engine_mode", "2"); // LSTM only
            // 优化识别速度
            tessApi.setVariable("tessedit_pageseg_mode", "6"); // 假设为统一文本块
            tessApi.setVariable("tessedit_char_whitelist", "");
            tessApi.setVariable("classify_bln_numeric_mode", "0");
            tessApi.setVariable("textord_debug_tabfind", "0");
            // 设置置信度阈值
            tessApi.setVariable("tessedit_minimal_confidence", "70");

        } catch (Exception e) {
            Log.w(TAG, "Failed to configure Tesseract parameters", e);
        }
    }
    
    /**
     * OCR识别接口
     */
    public interface OcrCallback {
        void onSuccess(String text, OcrResultMetadata metadata);
        void onError(String error);
        void onProgress(int progress); // 进度回调（可选）
    }
    
    /**
     * OCR结果元数据
     */
    public static class OcrResultMetadata {
        public long processingTime;
        public String language;
        public int imageWidth;
        public int imageHeight;
        public float confidence;
        public String cacheStatus;
        
        @Override
        public String toString() {
            return String.format("OCR[lang=%s, time=%dms, conf=%.1f, cache=%s]", 
                language, processingTime, confidence, cacheStatus);
        }
    }
    
    /**
     * 执行OCR识别（主入口）
     */
    public Future<OcrResult> recognizeAsync(final Image image, final Rect region, 
                                           final OcrCallback callback) {

        
        Callable<OcrResult> task = () -> {

            if (!isInitialized) {
                callback.onError("OCR engine not initialized");
                return null;
            }
            long startTime = System.currentTimeMillis();
            OcrResultMetadata metadata = new OcrResultMetadata();
            
            try {
                // 检查缓存
                String cacheKey = generateCacheKey( region);
                String cachedResult = textCache.get(cacheKey);
                
                if (cachedResult != null && 
                    System.currentTimeMillis() - cacheTimestamps.get(cacheKey) < 30000) {
                    // 30秒内缓存有效
                    metadata.cacheStatus = "HIT";
                    metadata.processingTime = System.currentTimeMillis() - startTime;
                    metadata.language = currentLanguage;
                    
                    callback.onSuccess(cachedResult, metadata);
                    return new OcrResult(cachedResult, metadata);
                }
                
                metadata.cacheStatus = "MISS";
                
                // 转换和预处理图像
                Bitmap bitmap = preprocessImageForOcr(image, region);
                if (bitmap == null) {
                    throw new IOException("Failed to process image");
                }
                
                // 执行OCR
                tessApi.setImage(bitmap);
                String result = tessApi.getUTF8Text();
                float meanConfidence = tessApi.meanConfidence() / 100f;
                
                // 后处理文本
                result = postProcessText(result);
                
                // 更新元数据
                metadata.processingTime = System.currentTimeMillis() - startTime;
                metadata.language = currentLanguage;
                metadata.imageWidth = bitmap.getWidth();
                metadata.imageHeight = bitmap.getHeight();
                metadata.confidence = meanConfidence;
                
                // 更新缓存
                if (meanConfidence > 0.7f && !result.trim().isEmpty()) {
                    textCache.put(cacheKey, result);
                    cacheTimestamps.put(cacheKey, System.currentTimeMillis());
                }
                
                // 更新性能统计
                synchronized (this) {
                    totalProcessingTime += metadata.processingTime;
                    totalRequests++;
                }
                
                // 回调
                callback.onSuccess(result, metadata);
                
                // 清理
                bitmap.recycle();
                tessApi.clear();
                
                return new OcrResult(result, metadata);
                
            } catch (Exception e) {
                Log.e(TAG, "OCR recognition failed", e);
                callback.onError(e.getMessage());
                return null;
            }
        };
        
        return executorService.submit(task);
    }

    public Future<OcrResult> recognizeAsync(final Bitmap bitmap, final Rect region,
                                            final OcrCallback callback) {

        Callable<OcrResult> task = () -> {
            if (!isInitialized) {
                callback.onError("OCR engine not initialized");
                return null;
            }
            long startTime = System.currentTimeMillis();
            OcrResultMetadata metadata = new OcrResultMetadata();
            try {
                // 检查缓存
                String cacheKey = generateCacheKey( region);
                String cachedResult = textCache.get(cacheKey);

                if (cachedResult != null &&
                        System.currentTimeMillis() - cacheTimestamps.get(cacheKey) < 30000) {
                    // 30秒内缓存有效
                    metadata.cacheStatus = "HIT";
                    metadata.processingTime = System.currentTimeMillis() - startTime;
                    metadata.language = currentLanguage;

                    callback.onSuccess(cachedResult, metadata);
                    return new OcrResult(cachedResult, metadata);
                }

                metadata.cacheStatus = "MISS";
                if (bitmap == null) {
                    throw new IOException("Failed to process image");
                }

                // 执行OCR
                tessApi.setImage(bitmap);
                String result = tessApi.getUTF8Text();
                float meanConfidence = tessApi.meanConfidence() / 100f;

                // 后处理文本
                result = postProcessText(result);

                // 更新元数据
                metadata.processingTime = System.currentTimeMillis() - startTime;
                metadata.language = currentLanguage;
                metadata.imageWidth = bitmap.getWidth();
                metadata.imageHeight = bitmap.getHeight();
                metadata.confidence = meanConfidence;

                // 更新缓存
                if (meanConfidence > 0.7f && !result.trim().isEmpty()) {
                    textCache.put(cacheKey, result);
                    cacheTimestamps.put(cacheKey, System.currentTimeMillis());
                }

                // 更新性能统计
                synchronized (this) {
                    totalProcessingTime += metadata.processingTime;
                    totalRequests++;
                }

                // 回调
                callback.onSuccess(result, metadata);

                // 清理
                bitmap.recycle();
                tessApi.clear();

                return new OcrResult(result, metadata);

            } catch (Exception e) {
                Log.e(TAG, "OCR recognition failed", e);
                callback.onError(e.getMessage());
                return null;
            }
        };

        return executorService.submit(task);
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey( Rect region) {
        // 基于图像特征生成简单的哈希键
        String key = String.format("%d_%d_%d_%d_%d", 
            region.left, region.top, region.width(), region.height(),
            System.currentTimeMillis() / 10000); // 每10秒变化
        
        return Integer.toHexString(key.hashCode());
    }

    
    /**
     * 图像预处理优化
     */
    private Bitmap preprocessImageForOcr(Image image, Rect region) {
        try {
            // 1. 转换为Bitmap
            Bitmap bitmap = convertYUV420ToBitmap(image);
            if (bitmap == null) return null;
            
            // 2. 裁剪指定区域
            if (region != null && !region.isEmpty()) {
                bitmap = Bitmap.createBitmap(bitmap, 
                    Math.max(0, region.left),
                    Math.max(0, region.top),
                    Math.min(region.width(), bitmap.getWidth() - region.left),
                    Math.min(region.height(), bitmap.getHeight() - region.top));
            }
            
            // 3. 调整大小（如果太大）
            int maxDimension = 1200;
            if (bitmap.getWidth() > maxDimension || bitmap.getHeight() > maxDimension) {
                float scale = Math.min(
                    (float) maxDimension / bitmap.getWidth(),
                    (float) maxDimension / bitmap.getHeight()
                );
                
                int newWidth = (int) (bitmap.getWidth() * scale);
                int newHeight = (int) (bitmap.getHeight() * scale);
                
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                bitmap.recycle();
                bitmap = scaled;
            }
            
            // 4. 灰度化
            bitmap = convertToGrayscale(bitmap);
            
            // 5. 二值化（自适应阈值）
            bitmap = adaptiveBinarization(bitmap);
            
            return bitmap;
            
        } catch (Exception e) {
            Log.e(TAG, "Image preprocessing failed", e);
            return null;
        }
    }
    
    /**
     * YUV420转Bitmap（优化版本）
     */
    private Bitmap convertYUV420ToBitmap(Image image) {
        if (image == null) return null;
        
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        
        byte[] nv21 = new byte[ySize + uSize + vSize];
        
        // Y channel
        yBuffer.get(nv21, 0, ySize);
        
        // U and V channels
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        
        try {
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, 
                image.getWidth(), image.getHeight(), null);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 
                80, out);
            
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            
        } catch (Exception e) {
            Log.e(TAG, "YUV conversion failed", e);
            return null;
        }
    }
    
    /**
     * 灰度化
     */
    private Bitmap convertToGrayscale(Bitmap bitmap) {
        Bitmap grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
            Bitmap.Config.ARGB_8888);
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = Color.red(pixel);
            int g = Color.green(pixel);
            int b = Color.blue(pixel);
            
            // 使用加权平均法
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        
        grayBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return grayBitmap;
    }
    
    /**
     * 自适应二值化
     */
    private Bitmap adaptiveBinarization(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        Bitmap binary = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 使用局部自适应阈值
        int blockSize = 31; // 块大小，必须是奇数
        int constant = 10;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 计算局部平均值
                int sum = 0;
                int count = 0;
                
                for (int dy = -blockSize/2; dy <= blockSize/2; dy++) {
                    for (int dx = -blockSize/2; dx <= blockSize/2; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            int pixel = pixels[ny * width + nx];
                            sum += Color.red(pixel); // 灰度图，取红色通道即可
                            count++;
                        }
                    }
                }
                
                int average = sum / count;
                int currentPixel = pixels[y * width + x];
                int gray = Color.red(currentPixel);
                
                // 二值化
                if (gray > average - constant) {
                    pixels[y * width + x] = Color.WHITE;
                } else {
                    pixels[y * width + x] = Color.BLACK;
                }
            }
        }
        
        binary.setPixels(pixels, 0, width, 0, 0, width, height);
        return binary;
    }
    
    /**
     * 文本后处理
     */
    private String postProcessText(String text) {
        if (text == null) return "";
        
        // 1. 去除多余空格和换行
        text = text.trim();
        text = text.replaceAll("\\s+", " ");
        
        // 2. 修正常见OCR错误
        Map<String, String> corrections = new HashMap<>();
        corrections.put("O", "0");
        corrections.put("l", "1");
        corrections.put("I", "1");
        corrections.put("Z", "2");
        corrections.put("S", "5");
        corrections.put("B", "8");
        
        for (Map.Entry<String, String> entry : corrections.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        
        // 3. 按行处理
        String[] lines = text.split("\n");
        List<String> processedLines = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                processedLines.add(line);
            }
        }
        
        return String.join("\n", processedLines);
    }
    
    /**
     * 切换语言
     */
    public void setLanguage(String language, final LanguageCallback callback) {
        if (tessApi == null || language.equals(currentLanguage)) {
            callback.onSuccess();
            return;
        }
        
        executorService.execute(() -> {
            try {
                tessApi.end();
                tessApi = new TessBaseAPI();
                
                File tessDir = new File(OcrConfig.getBaseAssetsDir(), "tessdata").getParentFile();
                boolean result = tessApi.init(tessDir.getAbsolutePath(), language);
                
                if (result ) {
                    currentLanguage = language;
                    configureTesseractForPerformance();
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to switch language: " + result);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    public interface LanguageCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * 获取性能统计
     */
    public PerformanceStats getPerformanceStats() {
        synchronized (this) {
            PerformanceStats stats = new PerformanceStats();
            stats.totalRequests = totalRequests;
            stats.totalProcessingTime = totalProcessingTime;
            stats.averageProcessingTime = totalRequests > 0 ? 
                totalProcessingTime / totalRequests : 0;
            stats.cacheHitRate = textCache.hitCount() / 
                (float)(textCache.hitCount() + textCache.missCount());
            stats.cacheSize = textCache.size();
            stats.isInitialized = isInitialized;
            return stats;
        }
    }
    
    public static class PerformanceStats {
        public int totalRequests;
        public long totalProcessingTime;
        public long averageProcessingTime;
        public float cacheHitRate;
        public int cacheSize;
        public boolean isInitialized;
        
        @Override
        public String toString() {
            return String.format(
                "Performance[reqs=%d, avg_time=%dms, cache_hit=%.1f%%, cache_size=%dKB]",
                totalRequests, averageProcessingTime, cacheHitRate * 100, cacheSize);
        }
    }
    
    /**
     * 清理资源
     */
    public void release() {
        if (tessApi != null) {
            tessApi.end();
            tessApi = null;
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        textCache.evictAll();
        cacheTimestamps.clear();
        instance = null;
    }
    
    /**
     * OCR结果包装类
     */
    public static class OcrResult {
        public final String text;
        public final OcrResultMetadata metadata;
        
        public OcrResult(String text, OcrResultMetadata metadata) {
            this.text = text;
            this.metadata = metadata;
        }
    }
}