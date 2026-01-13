package com.genymobile.scrcpy.custom.drap;

public class ExtremeAccelerationGenerator {
    
    // 核心方法：指数爆炸式变化
    public static double[] generateExplosiveSequence(
            int numCount,
            double explosionFactor,  // 爆炸因子 (0.5-0.9)，越大越极端
            double smoothness) {     // 平滑度 (0-1)，控制过渡是否平滑
        
        double[] sequence = new double[numCount];
        
        // 使用指数爆炸函数
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            sequence[i] = explosiveFunction(t, explosionFactor, smoothness);
        }
        
        // 确保精确的0和1
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    private static double explosiveFunction(double t, double factor, double smoothness) {
        if (t <= 0.5) {
            // 前半段：指数爆炸式加速
            double normalized = t * 2; // 映射到0-1
            
            // 使用超低指数，创造爆炸效果
            double power = 0.05 + (1 - factor) * 0.1; // 0.05-0.15
            
            // 添加平滑项
            double base = Math.pow(normalized, power);
            double smooth = smoothness * Math.sin(Math.PI * normalized / 2);
            
            return 0.5 * (base + smooth) / (1 + smoothness);
        } else {
            // 后半段：指数爆炸式减速
            double normalized = (t - 0.5) * 2; // 映射到0-1
            
            // 使用超高指数，创造爆炸效果
            double power = 5.0 + factor * 10.0; // 5-15
            
            // 添加平滑项
            double base = 1 - Math.pow(1 - normalized, power);
            double smooth = smoothness * (1 - Math.cos(Math.PI * normalized / 2));
            
            return 0.5 + 0.5 * (base + smooth) / (1 + smoothness);
        }
    }
    
    // 使用双曲正切函数（tanh）的变体 - 极端变化
    public static double[] generateHyperbolicSequence(
            int numCount,
            double steepness) { // 陡峭度 (0.5-5.0)，越大越极端
        
        double[] sequence = new double[numCount];
        
        // 缩放参数，控制变化的中心位置
        double scale = 3.0 + steepness * 7.0; // 3-10
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 使用缩放和偏移的双曲正切函数
            double x = (t - 0.5) * scale * 2; // 缩放到[-scale, scale]
            
            // 双曲正切函数：从-1到1，中间变化最快
            double tanhValue = Math.tanh(x);
            
            // 映射到0-1
            sequence[i] = (tanhValue + 1) / 2;
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 使用误差函数（erf）的变体 - 统计学上的极端分布
    public static double[] generateErrorFunctionSequence(
            int numCount,
            double sigma) { // 标准差 (0.1-0.5)，越小越极端
        
        double[] sequence = new double[numCount];
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 使用误差函数（erf）的累积分布
            double x = (t - 0.5) / (sigma * Math.sqrt(2));
            
            // 误差函数近似计算
            double erfValue = erf(x);
            
            // 映射到0-1
            sequence[i] = (erfValue + 1) / 2;
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 误差函数近似计算
    private static double erf(double x) {
        // 使用Abramowitz and Stegun近似公式
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;
        
        int sign = (x < 0) ? -1 : 1;
        x = Math.abs(x);
        
        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        
        return sign * y;
    }
}