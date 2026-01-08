package com.genymobile.scrcpy.custom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import com.genymobile.scrcpy.custom.bitmap.BitmapHelper;
import com.genymobile.scrcpy.custom.ocr.OptimizedOcrManager;

import java.io.File;

public class CustomTester {
    public static final String TAG = ScrcpyConfig.getLogGroup()+"CustomTester";
    public static void test() {
        try {
//            Log.i(TAG, "loadTessLibraries  start" );
//            SoExtracterAndLoader.loadTessLibraries(OcrConfig.getBaseDir());
//            Log.i(TAG, "loadTessLibraries  end" );
//
//            Log.i(TAG, "extractAssets start" );
//            AssetsExtractor.extractAssets();
//            Log.i(TAG, "extractAssets end" );
//
//            OptimizedOcrManager.getInstance();//tessOcrApi初始化
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    testOcr();
//                }
//            }, 5000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testOcr() {
        try {
            Log.i(TAG, "recognizeAsync start " );
            File tempDir = new File(ScrcpyConfig.getBaseAssetsDir(), "tessdata/test1.jpg");
            Log.i(TAG, "imageFile:"+tempDir );
            Bitmap bitmap = BitmapFactory.decodeFile(tempDir.getAbsolutePath());
            Log.i(TAG, "imageBitmap src:"+bitmap );
            BitmapHelper.init();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ocrBitmap(bitmap);
                }
            }, 20000);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void ocrBitmap(Bitmap bitmap) {
        Bitmap processBitmap=new BitmapHelper().preprocessImage(bitmap);
        Log.i(TAG, "imageBitmap processBitmap:"+processBitmap );
        Rect rect = new Rect(0, 200, 1080, 1200);
        OptimizedOcrManager.getInstance().recognizeAsync(processBitmap, rect, new OptimizedOcrManager.OcrCallback() {
            @Override
            public void onSuccess(String text, OptimizedOcrManager.OcrResultMetadata metadata) {
                Log.i(TAG, "recognizeAsync onSuccess:" + text);

            }

            @Override
            public void onError(String error) {
                Log.i(TAG, "recognizeAsync onError:" + error);
            }

            @Override
            public void onProgress(int progress) {
                Log.i(TAG, "recognizeAsync onProgress:" + progress);
            }
        });
    }
}
