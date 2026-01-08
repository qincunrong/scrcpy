package com.genymobile.scrcpy.custom.controll;

import android.graphics.PixelFormat;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.wrappers.ServiceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenShotImpl {
    private static String TAG = ScrcpyConfig.getLogGroup() + "ScreenShotImpl";
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;

    public ScreenShotImpl() {}


    
    public void startScreenshot(int screenWidth,int screenHeight) {
        Logger.i(TAG, "startScreenshot, screenWidth:%d, screenHeight:%d", screenWidth, screenHeight);
        if (screenHeight <= 0 || screenHeight <= 0) {
            return;
        }
        try {
            // 创建ImageReader来获取帧
            mImageReader = ImageReader.newInstance(
                    screenWidth, screenHeight,
                    PixelFormat.RGBA_8888,  // 或ImageFormat.PNG
                    1  // maxImages
            );
           mVirtualDisplay= ServiceManager.getDisplayManager()
                    .createVirtualDisplay("scrcpy",screenWidth, screenHeight, -1, mImageReader.getSurface());
            if (mHandlerThread == null) {
                mHandlerThread = new HandlerThread("ImageProcessing");
                mHandlerThread.start();
                mHandler = new Handler(mHandlerThread.getLooper());
            }
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    try {
                        Logger.i(TAG, "onImageAvailable");
                        // 获取最新的一帧
                        Image image = imageReader.acquireLatestImage();
                        if (image != null) {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                            String fileName = "screenshot_" + timeStamp;
                            ScreenShotImageConverter capture = new ScreenShotImageConverter(ScrcpyConfig.getBaseDir());
                            String imageFile = capture.saveImageToPng(image, fileName);
                            boolean isUploadSuccess = uploadImage(imageFile);
                            Logger.i(TAG, "uploadImage result:%b", isUploadSuccess);
                            if (isUploadSuccess) {
                                deleteFile(imageFile);
                            }
                            releaseScreenShot();
                        }else {
                            Logger.i(TAG, "onImageAvailable, image is null");
                        }

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

    private void releaseScreenShot() {
        Logger.i(TAG, "releaseScreenShot");
        try {
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                mHandlerThread = null;
                mHandler = null;
            }
        } catch (Exception e) {
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