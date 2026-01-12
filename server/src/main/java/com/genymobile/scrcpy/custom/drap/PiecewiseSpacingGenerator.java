package com.genymobile.scrcpy.custom.drap;

public class PiecewiseSpacingGenerator {
    
    // 分段函数生成序列
    public static double[] generatePiecewiseSequence(
            int numCount,
            double[] breakPoints,    // 断点位置 (0-1之间)
            double[] densities) {    // 每个段的密度 (0-1，越大越密集)
        
        // 参数校验
        if (breakPoints.length != densities.length) {
            throw new IllegalArgumentException("断点数量和密度数量必须相同");
        }
        
        // 添加起点和终点
        double[] allPoints = new double[breakPoints.length + 2];
        allPoints[0] = 0;
        System.arraycopy(breakPoints, 0, allPoints, 1, breakPoints.length);
        allPoints[allPoints.length - 1] = 1;
        
        // 计算每个段的长度
        double[] segmentLengths = new double[allPoints.length - 1];
        for (int i = 0; i < segmentLengths.length; i++) {
            segmentLengths[i] = allPoints[i+1] - allPoints[i];
        }
        
        // 根据密度分配点数
        int[] pointsPerSegment = new int[segmentLengths.length];
        int remainingPoints = numCount - 1; // 去掉起点
        
        // 计算总权重
        double totalWeight = 0;
        for (int i = 0; i < segmentLengths.length; i++) {
            double weight = segmentLengths[i] * densities[i];
            totalWeight += weight;
        }
        
        // 分配点数
        int allocated = 0;
        for (int i = 0; i < segmentLengths.length - 1; i++) {
            double weight = segmentLengths[i] * densities[i];
            pointsPerSegment[i] = (int) Math.max(1, 
                Math.round(weight / totalWeight * remainingPoints));
            allocated += pointsPerSegment[i];
        }
        
        // 最后一个段分配剩余点数
        pointsPerSegment[segmentLengths.length - 1] = remainingPoints - allocated;
        
        // 生成序列
        double[] sequence = new double[numCount];
        sequence[0] = 0;
        int index = 1;
        
        for (int seg = 0; seg < segmentLengths.length; seg++) {
            double start = allPoints[seg];
            double end = allPoints[seg + 1];
            int pointsInSegment = pointsPerSegment[seg];
            
            for (int i = 0; i < pointsInSegment; i++) {
                double t = (double) i / pointsInSegment;
                double value;
                
                // 根据密度调整分布
                if (densities[seg] < 0.3) {
                    // 低密度：线性分布
                    value = start + (end - start) * t;
                } else if (densities[seg] < 0.7) {
                    // 中密度：平方分布
                    value = start + (end - start) * t * t;
                } else {
                    // 高密度：四次方分布（更密集）
                    value = start + (end - start) * Math.pow(t, 4);
                }
                
                if (index < numCount) {
                    sequence[index++] = value;
                }
            }
        }
        
        // 确保最后一个点是1
        sequence[numCount - 1] = 1;
        
        return sequence;
    }
}