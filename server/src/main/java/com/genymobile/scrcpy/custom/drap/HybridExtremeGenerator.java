package com.genymobile.scrcpy.custom.drap;

public class HybridExtremeGenerator {
    
    // 分段混合函数：线性+指数爆炸
    public static double[] generateHybridExtremeSequence(
            int numCount,
            double extremeLevel,  // 极端程度 (0-1)
            double transition) {  // 过渡区域宽度 (0-0.3)
        
        double[] sequence = new double[numCount];
        
        // 定义关键点
        double transitionStart = 0.5 - transition;
        double transitionEnd = 0.5 + transition;
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            sequence[i] = hybridFunction(t, extremeLevel, transitionStart, transitionEnd);
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    private static double hybridFunction(double t, double extremeLevel, 
                                         double transStart, double transEnd) {
        if (t < transStart) {
            // 第一段：起点到过渡开始，使用高密度（非常密集）
            double normalized = t / transStart;
            
            // 使用超高指数，让点非常密集
            double power = 0.1 + extremeLevel * 0.1; // 0.1-0.2
            return Math.pow(normalized, power) * transStart;
            
        } else if (t <= transEnd) {
            // 第二段：过渡区域，使用近似线性（但可调整）
            double normalized = (t - transStart) / (transEnd - transStart);
            
            // 在过渡区域内可以加入轻微的非线性
            double offset = Math.sin(normalized * Math.PI) * 0.1 * extremeLevel;
            return transStart + (transEnd - transStart) * (normalized + offset);
            
        } else {
            // 第三段：过渡结束到终点，使用高密度（非常密集）
            double normalized = (t - transEnd) / (1 - transEnd);
            
            // 使用超高指数，让点非常密集
            double power = 0.1 + extremeLevel * 0.1; // 0.1-0.2
            return transEnd + (1 - transEnd) * Math.pow(normalized, power);
        }
    }
    
    // 反向指数爆炸：中间稀疏到极致
    public static double[] generateReverseExplosionSequence(
            int numCount,
            double explosionIntensity) { // 爆炸强度 (0-1)
        
        double[] sequence = new double[numCount];
        
        // 先均匀生成，然后通过权重重新分配
        double[] weights = new double[numCount];
        double totalWeight = 0;
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 权重函数：中间权重极小，两端权重大
            double distanceToMiddle = Math.abs(t - 0.5) * 2;
            
            // 使用指数衰减创造极端权重差异
            double weight = Math.exp(-explosionIntensity * 10 * distanceToMiddle * distanceToMiddle);
            
            // 确保最小值不为0
            weight = Math.max(weight, 0.001);
            
            weights[i] = weight;
            totalWeight += weight;
        }
        
        // 累积生成序列
        double cumulative = 0;
        for (int i = 0; i < numCount; i++) {
            cumulative += weights[i];
            sequence[i] = cumulative / totalWeight;
        }
        
        // 确保精确的0和1
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
}