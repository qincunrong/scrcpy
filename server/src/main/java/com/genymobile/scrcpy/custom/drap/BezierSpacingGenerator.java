package com.genymobile.scrcpy.custom.drap;

public class BezierSpacingGenerator {
    
    // 使用三阶Bezier曲线控制分布
    public static double[] generateBezierSequence(
            int numCount,
            double controlPoint1,  // 控制点1 (0-1)
            double controlPoint2) { // 控制点2 (0-1)
        
        double[] sequence = new double[numCount];
        
        for (int i = 0; i < numCount; i++) {
            double t = (double) i / (numCount - 1);
            sequence[i] = cubicBezier(t, 0, controlPoint1, controlPoint2, 1);
        }
        
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 三阶Bezier曲线
    private static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        double p = uuu * p0;               // (1-t)^3 * p0
        p += 3 * uu * t * p1;              // 3(1-t)^2 * t * p1
        p += 3 * u * tt * p2;              // 3(1-t) * t^2 * p2
        p += ttt * p3;                     // t^3 * p3
        
        return p;
    }
    
    // 自动生成控制点以创建中间稀疏的效果
    public static double[] generateAutoBezierSequence(
            int numCount,
            double intensity) { // 0-1，控制稀疏程度
        
        // 控制点计算：使曲线在中间更平缓（点更稀疏）
        double control1 = 0.1 + intensity * 0.4;   // 0.1-0.5
        double control2 = 0.5 + intensity * 0.4;   // 0.5-0.9
        
        return generateBezierSequence(numCount, control1, control2);
    }
}