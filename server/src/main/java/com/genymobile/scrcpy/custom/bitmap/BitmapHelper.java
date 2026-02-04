//package com.genymobile.scrcpy.custom.bitmap;
//
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//
//import org.opencv.android.Utils;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;
//
//public class BitmapHelper {
//    public static void init() {
//        // 在静态代码块中初始化
//        OpenCVUtils.init();
//    }
//
//    public Bitmap preprocessImage(Bitmap original) {
//        Bitmap processed = original.copy(Bitmap.Config.ARGB_8888, true);
//        // 1. 调整尺寸（建议DPI 300+）
//        if (original.getWidth() < 600) {
//            processed = scaleImage(processed, 2.0f); // 放大2倍
//        }
//        // 2. 转为灰度图
//        processed = convertToGrayScale(processed);
//        // 3. 二值化（多种方法尝试）
//        processed = adaptiveThreshold(processed);
//        // 4. 降噪处理
//        processed = removeNoise(processed);
//        // 5. 边缘增强
//        processed = enhanceEdges(processed);
//        // 6. 纠正倾斜
//        processed = deskewImage(processed);
//
//        return processed;
//    }
//
//    // 自适应阈值二值化（比全局阈值效果好）
//    private Bitmap adaptiveThreshold(Bitmap grayBitmap) {
//        Mat src = new Mat();
//        Utils.bitmapToMat(grayBitmap, src);
//
//        Mat dst = new Mat();
//        // 使用自适应高斯阈值
//        Imgproc.adaptiveThreshold(
//                src, dst, 255,
//                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
//                Imgproc.THRESH_BINARY, 11, 2
//        );
//
//        Bitmap result = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(dst, result);
//        return result;
//    }
//
//    // 降噪处理
//    private Bitmap removeNoise(Bitmap bitmap) {
//        Mat src = new Mat();
//        Utils.bitmapToMat(bitmap, src);
//
//        Mat dst = new Mat();
//        // 中值滤波去除椒盐噪声
//        Imgproc.medianBlur(src, dst, 3);
//
//        // 形态学操作去除小噪声点
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
//        Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, kernel);
//
//        Bitmap result = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(dst, result);
//        return result;
//    }
//
//    // 边缘增强
//    private Bitmap enhanceEdges(Bitmap bitmap) {
//        Mat src = new Mat();
//        Utils.bitmapToMat(bitmap, src);
//
//        Mat dst = new Mat();
//        // 使用拉普拉斯算子增强边缘
//        Imgproc.Laplacian(src, dst, CvType.CV_8U, 3, 1, 0);
//
//        // 与原始图像叠加
//        Core.addWeighted(src, 1.5, dst, -0.5, 0, dst);
//
//        Bitmap result = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(dst, result);
//        return result;
//    }
//    private Bitmap scaleImage(Bitmap original, float scaleFactor) {
//        if (scaleFactor == 1.0f) return original.copy(original.getConfig(), true);
//
//        int newWidth = Math.max(1, Math.round(original.getWidth() * scaleFactor));
//        int newHeight = Math.max(1, Math.round(original.getHeight() * scaleFactor));
//
//        // 使用高质量缩放
//        Bitmap scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
//
//        // 计算新的DPI（建议300 DPI以上）
//        if (scaleFactor > 1.5f) {
//            // 对于放大图像，进行锐化处理
//            scaled = sharpenImage(scaled);
//        }
//
//        return scaled;
//    }
//
//    // 可选：锐化处理
//    private Bitmap sharpenImage(Bitmap bitmap) {
//        float[] sharpenMatrix = {
//                0, -1, 0,
//                -1, 5, -1,
//                0, -1, 0
//        };
//
//        Bitmap sharpened = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//        Canvas canvas = new Canvas(sharpened);
//        Paint paint = new Paint();
//
//        ColorMatrix colorMatrix = new ColorMatrix();
//        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
//        paint.setColorFilter(filter);
//
//        // 使用卷积矩阵进行锐化
//        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
//        convMatrix.setAll(0);
//        convMatrix.matrix[0][0] = -1;
//        convMatrix.matrix[1][0] = -1;
//        convMatrix.matrix[2][0] = -1;
//        convMatrix.matrix[0][1] = -1;
//        convMatrix.matrix[1][1] = 9;
//        convMatrix.matrix[2][1] = -1;
//        convMatrix.matrix[0][2] = -1;
//        convMatrix.matrix[1][2] = -1;
//        convMatrix.matrix[2][2] = -1;
//
//        return convMatrix.computeConvolution(bitmap, convMatrix);
//    }
//    private Bitmap convertToGrayScale(Bitmap original) {
//        // 方法1：使用Android内置方法（简单快速）
//        Bitmap grayBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(grayBitmap);
//        Paint paint = new Paint();
//
//        ColorMatrix colorMatrix = new ColorMatrix();
//        colorMatrix.setSaturation(0); // 0表示完全灰度
//
//        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
//        paint.setColorFilter(filter);
//
//        canvas.drawBitmap(original, 0, 0, paint);
//
//        // 方法2：使用像素级处理（更精确控制）
//        // grayBitmap = convertToGrayManual(original);
//
//        return grayBitmap;
//    }
//
//    // 方法2：手动灰度转换（YUV公式）
//    private Bitmap convertToGrayManual(Bitmap original) {
//        Bitmap grayBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
//
//        int width = original.getWidth();
//        int height = original.getHeight();
//        int[] pixels = new int[width * height];
//
//        original.getPixels(pixels, 0, width, 0, 0, width, height);
//
//        for (int i = 0; i < pixels.length; i++) {
//            int pixel = pixels[i];
//            int alpha = (pixel >> 24) & 0xFF;
//            int red = (pixel >> 16) & 0xFF;
//            int green = (pixel >> 8) & 0xFF;
//            int blue = pixel & 0xFF;
//
//            // YUV公式计算亮度（推荐）
//            int gray = (int)(0.299 * red + 0.587 * green + 0.114 * blue);
//
//            // 或者使用简单平均值
//            // int gray = (red + green + blue) / 3;
//
//            pixels[i] = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
//        }
//
//        grayBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        return grayBitmap;
//    }
//    /**
//     * 图像去倾斜 - 自动检测并纠正倾斜角度
//     */
//    private Bitmap deskewImage(Bitmap bitmap) {
//        try {
//            // 方法1：使用OpenCV（推荐）
//            boolean hasOpenCV=true;
//            if (hasOpenCV) {
//                return deskewWithOpenCV(bitmap);
//            }
//
//            // 方法2：纯Java实现（检测直线并计算角度）
//            return deskewWithJava(bitmap);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return bitmap; // 如果失败，返回原图
//        }
//    }
//
//    // 方法1：使用OpenCV进行去倾斜
//    private Bitmap deskewWithOpenCV(Bitmap bitmap) {
//        Mat src = new Mat();
//        Utils.bitmapToMat(bitmap, src);
//
//        // 转为灰度图
//        Mat gray = new Mat();
//        if (src.channels() == 3) {
//            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
//        } else {
//            gray = src.clone();
//        }
//
//        // 二值化
//        Mat binary = new Mat();
//        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
//
//        // 形态学操作，连接文本区域
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_CLOSE, kernel);
//
//        // 使用霍夫变换检测直线
//        Mat lines = new Mat();
//        Imgproc.HoughLinesP(binary, lines, 1, Math.PI/180, 100, 100, 10);
//
//        // 计算平均角度
//        double totalAngle = 0;
//        int lineCount = 0;
//
//        for (int i = 0; i < lines.rows(); i++) {
//            double[] val = lines.get(i, 0);
//            double x1 = val[0], y1 = val[1], x2 = val[2], y2 = val[3];
//
//            // 计算线段角度（排除接近垂直的线）
//            double angle = Math.atan2(y2 - y1, x2 - x1) * 180 / Math.PI;
//
//            // 只考虑接近水平的线（-45°到45°）
//            if (Math.abs(angle) < 45) {
//                totalAngle += angle;
//                lineCount++;
//            }
//        }
//
//        // 计算平均角度
//        double avgAngle = 0;
//        if (lineCount > 0) {
//            avgAngle = totalAngle / lineCount;
//        }
//
//        // 如果角度太小，不需要旋转
//        if (Math.abs(avgAngle) < 0.5) {
//            return bitmap;
//        }
//
//        // 旋转图像
//        Point center = new Point(src.cols() / 2.0, src.rows() / 2.0);
//        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, avgAngle, 1.0);
//
//        // 计算旋转后的图像尺寸
//        double radians = Math.toRadians(avgAngle);
//        double sin = Math.abs(Math.sin(radians));
//        double cos = Math.abs(Math.cos(radians));
//
//        int newWidth = (int) Math.floor(src.cols() * cos + src.rows() * sin);
//        int newHeight = (int) Math.floor(src.cols() * sin + src.rows() * cos);
//
//        // 调整旋转矩阵的平移参数
//        rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + (newWidth / 2.0 - center.x));
//        rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + (newHeight / 2.0 - center.y));
//
//        // 执行旋转
//        Mat rotated = new Mat();
//        Imgproc.warpAffine(src, rotated, rotationMatrix, new Size(newWidth, newHeight),
//                Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
//
//        // 转回Bitmap
//        Bitmap result = Bitmap.createBitmap(rotated.cols(), rotated.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(rotated, result);
//
//        return result;
//    }
//
//    // 方法2：纯Java实现（简化版）
//    private Bitmap deskewWithJava(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        // 简化的边缘检测
//        int[] edgePixels = detectEdges(bitmap);
//
//        // 分析边缘点，计算可能的倾斜角度
//        double angle = estimateSkewAngle(edgePixels, width, height);
//
//        // 如果角度太小，不旋转
//        if (Math.abs(angle) < 0.5) {
//            return bitmap;
//        }
//
//        // 旋转图像
//        return rotateBitmap(bitmap, angle);
//    }
//
//    // 简化的边缘检测
//    private int[] detectEdges(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int[] pixels = new int[width * height];
//
//        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//        int[] edgePixels = new int[pixels.length];
//
//        // Sobel算子边缘检测
//        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
//        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
//
//        for (int y = 1; y < height - 1; y++) {
//            for (int x = 1; x < width - 1; x++) {
//                int index = y * width + x;
//
//                int gx = 0, gy = 0;
//                for (int i = -1; i <= 1; i++) {
//                    for (int j = -1; j <= 1; j++) {
//                        int pixelIndex = (y + i) * width + (x + j);
//                        int gray = getGrayScale(pixels[pixelIndex]);
//
//                        gx += gray * sobelX[i + 1][j + 1];
//                        gy += gray * sobelY[i + 1][j + 1];
//                    }
//                }
//
//                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
//                edgePixels[index] = magnitude > 128 ? 1 : 0;
//            }
//        }
//
//        return edgePixels;
//    }
//
//    // 获取像素的灰度值
//    private int getGrayScale(int pixel) {
//        int r = (pixel >> 16) & 0xFF;
//        int g = (pixel >> 8) & 0xFF;
//        int b = pixel & 0xFF;
//        return (r + g + b) / 3;
//    }
//
//    // 估计倾斜角度（简化版）
//    private double estimateSkewAngle(int[] edgePixels, int width, int height) {
//        // 投影法：计算每行的边缘点数量
//        int[] horizontalProjection = new int[height];
//        int[] verticalProjection = new int[width];
//
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int index = y * width + x;
//                if (edgePixels[index] == 1) {
//                    horizontalProjection[y]++;
//                    verticalProjection[x]++;
//                }
//            }
//        }
//
//        // 寻找文本区域
//        int textStartY = -1, textEndY = -1;
//        for (int y = 0; y < height; y++) {
//            if (horizontalProjection[y] > width * 0.05) { // 5%的阈值
//                if (textStartY == -1) textStartY = y;
//                textEndY = y;
//            }
//        }
//
//        // 如果没有找到足够的文本，返回0
//        if (textStartY == -1 || textEndY - textStartY < 10) {
//            return 0;
//        }
//
//        // 计算可能的倾斜角度（简化）
//        double angle = 0;
//        int count = 0;
//
//        for (int y = textStartY; y <= textEndY; y++) {
//            // 寻找每行的左右边界
//            int left = -1, right = -1;
//            for (int x = 0; x < width; x++) {
//                int index = y * width + x;
//                if (edgePixels[index] == 1) {
//                    if (left == -1) left = x;
//                    right = x;
//                }
//            }
//
//            if (left != -1 && right != -1) {
//                // 简化的角度计算
//                for (int x = left; x <= right; x++) {
//                    int index = y * width + x;
//                    if (edgePixels[index] == 1) {
//                        // 检查相邻行的对应位置
//                        for (int dy = -2; dy <= 2; dy++) {
//                            if (dy == 0) continue;
//                            int ny = y + dy;
//                            if (ny >= 0 && ny < height) {
//                                int nIndex = ny * width + x;
//                                if (edgePixels[nIndex] == 1) {
//                                    double lineAngle = Math.atan2(dy, 0) * 180 / Math.PI;
//                                    angle += lineAngle;
//                                    count++;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return count > 0 ? angle / count : 0;
//    }
//
//    // 旋转Bitmap
//    private Bitmap rotateBitmap(Bitmap bitmap, double angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate((float) angle);
//
//        // 计算新的边界
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        float[] pts = {0, 0, width, 0, 0, height, width, height};
//        Matrix m = new Matrix();
//        m.setRotate((float) angle);
//        m.mapPoints(pts);
//
//        float minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
//        float maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
//        float minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
//        float maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
//
//        int newWidth = Math.round(maxX - minX);
//        int newHeight = Math.round(maxY - minY);
//
//        // 创建新的Bitmap
//        Bitmap rotated = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
//        Canvas canvas = new Canvas(rotated);
//
//        // 设置背景为白色
//        canvas.drawColor(Color.WHITE);
//
//        // 平移画布，使图像居中
//        canvas.translate(-minX, -minY);
//
//        // 旋转并绘制
//        canvas.rotate((float) angle, width / 2f, height / 2f);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//
//        return rotated;
//    }
//}
