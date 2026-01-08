package com.genymobile.scrcpy.custom.controll;

import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;

import com.genymobile.scrcpy.custom.OcrConfig;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.wrappers.ServiceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenShotImpl {
    private static String TAG = OcrConfig.getLogGroup() + "ScreenShotImpl";
    private static volatile ScreenShotImpl singleton = null;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private ScreenShotImpl() {}

    public static ScreenShotImpl getInstance() {
        if (singleton == null) {
            synchronized (ScreenShotImpl.class) {
                if (singleton == null) {
                    singleton = new ScreenShotImpl();
                }
            }
        }
        return singleton;
    }
    
    public void startScreenshot(int screenWidth,int screenHeight) {
        Logger.i(TAG, "startScreenshot, screenWidth:%d, screenHeight:%d", screenWidth, screenHeight);
        if (screenHeight <= 0 || screenHeight <= 0) {
            return;
        }
        try {
            // 创建ImageReader来获取帧
            ImageReader imageReader = ImageReader.newInstance(
                    screenWidth, screenHeight,
                    PixelFormat.RGBA_8888,  // 或ImageFormat.PNG
                    1  // maxImages
            );
            ServiceManager.getDisplayManager()
                    .createVirtualDisplay("scrcpy",screenWidth, screenHeight, -1, imageReader.getSurface());
            if (mHandlerThread == null) {
                mHandlerThread = new HandlerThread("ImageProcessing");
                mHandlerThread.start();
                mHandler = new Handler(mHandlerThread.getLooper());
            }
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    try {
                        Logger.i(TAG, "onImageAvailable");
                        // 获取最新的一帧
                        Image image = imageReader.acquireLatestImage();
                        if (image != null) {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                            String fileName = "screenshot_" + timeStamp;
                            ScreenShotImageConverter capture = new ScreenShotImageConverter(OcrConfig.getBaseDir());
                            String imageFile = capture.saveImageToPng(image, fileName);
                            boolean isUploadSuccess = uploadImage(imageFile);
                            Logger.i(TAG, "uploadImage result:%b", isUploadSuccess);
                            if (isUploadSuccess) {
                                deleteFile(imageFile);
                            }
                        }else {
                            Logger.i(TAG, "onImageAvailable, image is null");
                        }
                        imageReader.close();
                    } catch (Exception e) {
                        Logger.i(TAG, "onImageAvailable, exception:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }, mHandler);
        } catch (Exception e) {
            Logger.i(TAG, "startScreenshot, exception:"+e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean uploadImage(String imageFile) {
        //TODO:上传图片
        return false;
    }

    private void deleteFile(String imageFile) {
        //TODO:删除截图图片

    }


}