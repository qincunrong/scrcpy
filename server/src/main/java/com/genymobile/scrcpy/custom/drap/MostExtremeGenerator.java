package com.genymobile.scrcpy.custom.drap;

public class MostExtremeGenerator {
    
    // 最极端的变化：中间几乎为常数，两端爆炸式变化
    public static double[] generateMostExtremeSequence(
            int numCount,
            double middleWidth,     // 中间平坦区域宽度 (0-0.8)
            double endExplosion) {  // 两端爆炸强度 (1-10)
        
        double[] sequence = new double[numCount];
        
        double middleStart = 0.5 - middleWidth/2;
        double middleEnd = 0.5 + middleWidth/2;
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            sequence[i] = mostExtremeFunction(t, middleStart, middleEnd, endExplosion);
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    private static double mostExtremeFunction(double t, double middleStart, 
                                             double middleEnd, double explosion) {
        if (t < middleStart) {
            // 左端：超指数变化
            double normalized = t / middleStart;
            
            // 使用极低的指数创造爆炸效果
            double power = 1.0 / (explosion * 5); // 0.02-0.1
            return middleStart * Math.pow(normalized, power);
            
        } else if (t <= middleEnd) {
            // 中间：几乎线性（平坦）
            double normalized = (t - middleStart) / (middleEnd - middleStart);
            
            // 可以添加轻微的弯曲
            double curve = Math.sin(normalized * Math.PI) * 0.05;
            return middleStart + (middleEnd - middleStart) * (normalized + curve);
            
        } else {
            // 右端：超指数变化
            double normalized = (t - middleEnd) / (1 - middleEnd);
            
            // 使用极高的指数创造爆炸效果
            double power = explosion * 5; // 5-50
            return middleEnd + (1 - middleEnd) * (1 - Math.pow(1 - normalized, power));
        }
    }
    
    // 使用Sigmoid函数的极端变体
    public static double[] generateSigmoidExtremeSequence(
            int numCount,
            double steepness) { // 陡峭度 (5-50)
        
        double[] sequence = new double[numCount];
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            
            // 极端Sigmoid：几乎像阶跃函数
            double x = (t - 0.5) * steepness * 2;
            double sigmoid = 1.0 / (1.0 + Math.exp(-x));
            
            sequence[i] = sigmoid;
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
}