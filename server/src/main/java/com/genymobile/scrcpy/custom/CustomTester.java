package com.genymobile.scrcpy.custom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import com.genymobile.scrcpy.custom.ocr.OptimizedOcrManager;

import java.io.File;

public class CustomTester {
    public static final String TAG = OcrConfig.getLogGroup()+"CustomTester";
    public static void test() {
        try {
            Log.i(TAG, "loadTessLibraries  start" );
            SoExtracterAndLoader.loadTessLibraries(OcrConfig.getBaseDir());
            Log.i(TAG, "loadTessLibraries  end" );

            Log.i(TAG, "extractAssets start" );
            AssetsExtractor.extractAssets();
            Log.i(TAG, "extractAssets end" );

            OptimizedOcrManager.getInstance();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    testOcr();
                }
            }, 5000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testOcr() {
        try {
            Log.i(TAG, "recognizeAsync start " );
            File tempDir = new File(OcrConfig.getBaseAssetsDir(), "tessdata/test1.jpg");
            Log.i(TAG, "imageFile:"+tempDir );
            Bitmap bitmap = BitmapFactory.decodeFile(tempDir.getAbsolutePath());
            Log.i(TAG, "imageBitmap:"+bitmap );
            Rect rect = new Rect(0, 200, 1080, 1200);
            OptimizedOcrManager.getInstance().recognizeAsync(bitmap, rect, new OptimizedOcrManager.OcrCallback() {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
