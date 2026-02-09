package com.genymobile.scrcpy.custom.controll;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenShotImageConverter {
    private static final String TAG = "ScreenShotConverter";
    private String mImagePath;
    public ScreenShotImageConverter(String imagePath) {
        mImagePath = imagePath;
    }

    /**
     * 从 ImageReader 的 Image 保存为 PNG 文件
     */
    public String saveImageToPng(Image image, String fileName) {
        if (image == null) {
            Logger.e(TAG, "saveImageToPng, image is null");
            return null;
        }
        Bitmap bitmap = null;
        FileOutputStream fos = null;
        String filePath = null;
        
        try {
            // 1. 将 Image 转换为 Bitmap
            bitmap = imageToBitmap(image);
            if (bitmap == null) {
                Logger.e(TAG, "saveImageToPng, failed imageToBitmap");
                return null;
            }
            // 3. 创建文件
            File file = new File(mImagePath, fileName + ".png");
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            filePath = file.getAbsolutePath();
//            fos = new FileOutputStream(file);
//            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            // 4. 保存为 PNG
            boolean success=compressQuality(bitmap, file.getAbsolutePath(), 1 * 1024 * 1024);
            if (success) {
                Logger.i(TAG, "saveImageToPng, success:" + filePath);
            } else {
                Logger.e(TAG, "saveImageToPng, failed compress");
                filePath = null;
            }
            
        } catch (Exception e) {
            Logger.e(TAG, "saveImageToPng, exception:"+e.getMessage());
            e.printStackTrace();
            filePath = null;
        } finally {
            // 6. 清理资源
            try {
                closeQuietly(fos);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
        
        return filePath;
    }
    
    /**
     * 将 Image 转换为 Bitmap（支持多种格式）
     */
    private Bitmap imageToBitmap(Image image) {
        int format = image.getFormat();
        int width = image.getWidth();
        int height = image.getHeight();
        
        switch (format) {
            case PixelFormat.RGBA_8888:
                return rgba8888ToBitmap(image, width, height);
                
            case PixelFormat.RGB_565:
                return rgb565ToBitmap(image, width, height);
                
            case ImageFormat.YUV_420_888:
                return yuv420888ToBitmap(image, width, height);
                
            default:
                Logger.w(TAG, "Unsupported image format: " + format);
                return null;
        }
    }
    
    /**
     * 处理 RGBA_8888 格式（MediaProjection 常用格式）
     */
    private Bitmap rgba8888ToBitmap(Image image, int width, int height) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        
        // 计算行步长和像素步长
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        
        // 创建 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888);
        
        bitmap.copyPixelsFromBuffer(buffer);
        
        // 如果需要裁剪掉填充部分
        if (rowPadding > 0) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        
        return bitmap;
    }
    
    /**
     * 处理 RGB_565 格式
     */
    private Bitmap rgb565ToBitmap(Image image, int width, int height) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(buffer);
        
        return bitmap;
    }
    
    /**
     * 处理 YUV_420_888 格式（相机常用格式）
     */
    private Bitmap yuv420888ToBitmap(Image image, int width, int height) {
        Image.Plane[] planes = image.getPlanes();
        
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        
        byte[] nv21 = new byte[ySize + uSize + vSize];
        
        // Y 平面
        yBuffer.get(nv21, 0, ySize);
        
        // U 平面
        vBuffer.get(nv21, ySize, vSize);
        
        // V 平面
        uBuffer.get(nv21, ySize + vSize, uSize);
        
        // 使用 YuvImage 转换为 JPEG，然后解码为 Bitmap
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outputStream);
        byte[] jpegData = outputStream.toByteArray();
        
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
    }

    /**
     * 安静地关闭流
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Logger.e(TAG, "Error closing stream", e);
            }
        }
    }

    private static boolean compressQuality(Bitmap bm,String imageFile,long targetSize) {
        ByteArrayOutputStream bos=null;
        FileOutputStream fos = null;
        try {
            bos = new ByteArrayOutputStream();
            int quality = 100;
            // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            bm.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            long length=bos.toByteArray().length;
            Logger.i(TAG, "compressQuality targetSize:%s , currentSize:%s" , FileUtils.formatFileSize(targetSize), FileUtils.formatFileSize(length));
            while (length > targetSize) {
                bos.reset();// 重置bos即清空bos
                quality -= 5;
                bm.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                length = bos.toByteArray().length;
                Logger.i(TAG, "compressQuality quality:%d , size:%s" , quality, FileUtils.formatFileSize(length));
                if (quality < 50) {
                    break;
                }
            }
            fos = new FileOutputStream(imageFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            Logger.i(TAG, "compressQuality exception:" + e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}