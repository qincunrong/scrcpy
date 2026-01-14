package com.genymobile.scrcpy.custom.drap;

public class VariableSpacingGenerator {
    
    // 核心方法：生成从0到1的非均匀序列
    public static double[] generateVariableSpacingSequence(
            int numCount, 
            double accelerationFactor,  // 加速度因子（0-1），越大中间越稀疏
            boolean symmetric) {         // 是否对称
        
        double[] sequence = new double[numCount];
        if (numCount <= 2) {
            sequence[0] = 0;
            if (numCount > 1) sequence[numCount-1] = 1;
            return sequence;
        }
        
        // 生成参数化的序列
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            sequence[i] = calculatePosition(t, accelerationFactor, symmetric);
        }
        
        // 确保精确的0和1
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 计算每个点的位置
    private static double calculatePosition(double t, double factor, boolean symmetric) {
        if (symmetric) {
            // 对称分布：中间最稀疏
            return symmetricAcceleration(t, factor);
        } else {
            // 不对称分布：可以控制最稀疏点的位置
            return asymmetricAcceleration(t, factor);
        }
    }
    
    // 对称加速减速（中间最稀疏）
    private static double symmetricAcceleration(double t, double factor) {
        // 使用两个二次函数拼接
        if (t <= 0.5) {
            // 前半段：加速，变化越来越快
            double normalized = t * 2; // 映射到0-1
            return 0.5 * Math.pow(normalized, 0.5 + factor * 2.5);
        } else {
            // 后半段：减速，变化越来越慢
            double normalized = (t - 0.5) * 2; // 映射到0-1
            return 0.5 + 0.5 * (1 - Math.pow(1 - normalized, 0.5 + factor * 2.5));
        }
    }


    
    // 不对称加速减速（最稀疏点位置可调）
    private static double asymmetricAcceleration(double t, double factor) {
        // factor控制最稀疏点的位置：0=开始，0.5=中间，1=结束
        double sparsePoint = 0.2 + factor * 0.6; // 最稀疏点在0.2-0.8之间
        
        if (t <= sparsePoint) {
            // 从开始到最稀疏点：加速
            double normalized = t / sparsePoint;
            double power = 0.3 + factor * 0.7; // 0.3-1.0
            return sparsePoint * Math.pow(normalized, power);
        } else {
            // 从最稀疏点到结束：减速
            double normalized = (t - sparsePoint) / (1 - sparsePoint);
            double power = 1.3 + factor * 2.7; // 1.3-4.0
            return sparsePoint + (1 - sparsePoint) * (1 - Math.pow(1 - normalized, power));
        }
    }
    
    // 高级版本：使用多个控制参数
    public static double[] generateAdvancedSequence(
            int numCount,
            double startDensity,    // 起点密度 (0-1, 越大越密集)
            double middleSparsity,  // 中间稀疏度 (0-1, 越大越稀疏)
            double endDensity) {    // 终点密度 (0-1, 越大越密集)
        
        double[] sequence = new double[numCount];
        sequence[0] = 0;
        
        // 计算总权重（用于归一化）
        double totalWeight = 0;
        double[] weights = new double[numCount];
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 计算权重函数：中间权重小（稀疏），两端权重大（密集）
            double distanceToMiddle = Math.abs(t - 0.5) * 2; // 0在中间，1在两端
            
            // 使用三个参数控制不同位置的密度
            double weight;
            if (t < 0.33) {
                // 起点区域
                weight = 1.0 + (1 - startDensity) * 2.0;
            } else if (t < 0.67) {
                // 中间区域
                weight = 0.5 + (1 - middleSparsity) * 0.5;
            } else {
                // 终点区域
                weight = 1.0 + (1 - endDensity) * 2.0;
            }
            
            weights[i] = weight;
            totalWeight += weight;
        }
        
        // 累积计算位置
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
    
    // 使用正弦函数调整（更平滑）
    public static double[] generateSinusoidalSequence(
            int numCount,
            double intensity) { // 0-1，控制变化强度
        
        double[] sequence = new double[numCount];
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 使用正弦函数，但通过指数调整变化率
            double adjusted;
            if (t < 0.5) {
                double normalized = t * 2;
                // 使用二次函数+正弦组合
                adjusted = 0.5 * (Math.pow(normalized, 0.3 + intensity * 0.7) * 
                                 Math.sin(Math.PI * normalized / 2));
            } else {
                double normalized = (t - 0.5) * 2;
                adjusted = 0.5 + 0.5 * (1 - Math.pow(1 - normalized, 0.3 + intensity * 0.7) * 
                                       Math.cos(Math.PI * normalized / 2));
            }
            
            sequence[i] = adjusted;
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 直接生成间距序列（更容易控制）
    public static double[] generateSpacingSequence(
            int numCount,
            double minSpacing,  // 最小间距
            double maxSpacing,  // 最大间距
            double peakPosition) { // 最大间距的位置 (0-1)
        
        double[] spacings = new double[numCount - 1];
        double totalLength = 0;
        
        // 生成间距序列：中间大，两端小
        for (int i = 0; i < numCount - 1; i++) {
            double position = (double) i / (numCount - 2);
            double distanceToPeak = Math.abs(position - peakPosition);
            
            // 使用高斯函数计算权重
            double sigma = 0.3; // 控制峰值宽度
            double weight = Math.exp(-distanceToPeak * distanceToPeak / (2 * sigma * sigma));
            
            // 间距从maxSpacing到minSpacing线性变化
            double spacing = minSpacing + (maxSpacing - minSpacing) * (1 - weight);
            spacings[i] = spacing;
            totalLength += spacing;
        }
        
        // 归一化到总长度为1
        for (int i = 0; i < spacings.length; i++) {
            spacings[i] /= totalLength;
        }
        
        // 转换为位置序列
        double[] sequence = new double[numCount];
        sequence[0] = 0;
        for (int i = 1; i < numCount; i++) {
            sequence[i] = sequence[i-1] + spacings[i-1];
        }
        
        // 确保精确的0和1
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
}