package com.genymobile.scrcpy.custom;

import android.graphics.Bitmap;

public class ConvolutionMatrix {
    public int size;
    public float[][] matrix;
    public float factor = 1.0f;
    public float offset = 0.0f;
    
    public ConvolutionMatrix(int size) {
        this.size = size;
        this.matrix = new float[size][size];
    }
    
    public void setAll(float value) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = value;
            }
        }
    }
    
    public void setMatrix(int row, int col, float value) {
        if (row >= 0 && row < size && col >= 0 && col < size) {
            matrix[row][col] = value;
        }
    }
    
    public Bitmap computeConvolution(Bitmap src, ConvolutionMatrix convMatrix) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int[] newPixels = new int[width * height];
        
        int halfSize = convMatrix.size / 2;
        
        for (int y = halfSize; y < height - halfSize; y++) {
            for (int x = halfSize; x < width - halfSize; x++) {
                float r = 0, g = 0, b = 0;
                
                for (int i = -halfSize; i <= halfSize; i++) {
                    for (int j = -halfSize; j <= halfSize; j++) {
                        int pixelIndex = (y + i) * width + (x + j);
                        int pixel = pixels[pixelIndex];
                        
                        float kernelValue = convMatrix.matrix[i + halfSize][j + halfSize];
                        
                        r += ((pixel >> 16) & 0xFF) * kernelValue;
                        g += ((pixel >> 8) & 0xFF) * kernelValue;
                        b += (pixel & 0xFF) * kernelValue;
                    }
                }
                
                // 应用因子和偏移
                r = r / convMatrix.factor + convMatrix.offset;
                g = g / convMatrix.factor + convMatrix.offset;
                b = b / convMatrix.factor + convMatrix.offset;
                
                // 限制在0-255范围内
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int alpha = (pixels[y * width + x] >> 24) & 0xFF;
                newPixels[y * width + x] = (alpha << 24) | 
                                          ((int)r << 16) | 
                                          ((int)g << 8) | 
                                          (int)b;
            }
        }
        
        // 处理边缘像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y < halfSize || y >= height - halfSize || 
                    x < halfSize || x >= width - halfSize) {
                    newPixels[y * width + x] = pixels[y * width + x];
                }
            }
        }
        
        result.setPixels(newPixels, 0, width, 0, 0, width, height);
        return result;
    }
}