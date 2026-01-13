package com.genymobile.scrcpy.custom.drap;

public class PhysicsBasedGenerator {
    
    // 模拟弹簧系统：中间稀疏像拉伸的弹簧
    public static double[] generateSpringSequence(
            int numCount,
            double springConstant,  // 弹簧常数 (0.1-2.0)，越大中间越稀疏
            double damping) {       // 阻尼 (0-0.5)，控制振荡
        
        double[] sequence = new double[numCount];
        
        // 模拟弹簧质点系统
        double[] positions = new double[numCount];
        double[] velocities = new double[numCount];
        
        // 初始均匀分布
        for (int i = 0; i < numCount; i++) {
            positions[i] = (double) i / (numCount - 1);
        }
        
        // 模拟弹簧连接
        int iterations = 1000;
        double dt = 0.01;
        
        for (int iter = 0; iter < iterations; iter++) {
            // 计算每个质点上的力
            double[] forces = new double[numCount];
            
            // 边界固定
            positions[0] = 0;
            positions[numCount-1] = 1;
            
            // 弹簧力：相邻质点之间的力
            for (int i = 1; i < numCount - 1; i++) {
                // 左侧弹簧
                double leftForce = springConstant * (positions[i-1] - positions[i]);
                // 右侧弹簧
                double rightForce = springConstant * (positions[i+1] - positions[i]);
                
                // 添加一个中间拉伸力，使中间更稀疏
                double middlePull = 0;
                if (i == numCount / 2) {
                    middlePull = springConstant * 0.5; // 中间质点受到额外拉伸
                }
                
                forces[i] = leftForce + rightForce + middlePull;
            }
            
            // 更新速度和位置
            for (int i = 1; i < numCount - 1; i++) {
                // 阻尼力
                double dampingForce = -damping * velocities[i];
                
                // 总力
                double totalForce = forces[i] + dampingForce;
                
                // 更新速度（假设质量为1）
                velocities[i] += totalForce * dt;
                
                // 更新位置
                positions[i] += velocities[i] * dt;
            }
            
            // 确保边界
            positions[0] = 0;
            positions[numCount-1] = 1;
        }
        
        // 复制结果
        System.arraycopy(positions, 0, sequence, 0, numCount);
        
        // 确保精确的0和1
        sequence[0] = 0;
        sequence[numCount-1] = 1;
        
        return sequence;
    }
    
    // 简化的物理模拟：质量-弹簧系统
    public static double[] generateMassSpringSequence(
            int numCount,
            double middleStretch) { // 中间拉伸系数 (0-2.0)
        
        // 设置弹簧系统
        int masses = numCount;
        double[] x = new double[masses]; // 位置
        double[] v = new double[masses]; // 速度
        
        // 初始均匀分布
        for (int i = 0; i < masses; i++) {
            x[i] = (double) i / (masses - 1);
        }
        
        // 模拟参数
        double k = 10.0; // 弹簧常数
        double dt = 0.005;
        int steps = 2000;
        
        for (int step = 0; step < steps; step++) {
            double[] force = new double[masses];
            
            // 固定两端
            x[0] = 0;
            x[masses-1] = 1;
            
            // 计算弹簧力
            for (int i = 1; i < masses - 1; i++) {
                // 左右弹簧力
                force[i] = k * (x[i-1] - 2*x[i] + x[i+1]);
                
                // 添加中间拉伸力
                double distanceFromMiddle = Math.abs(i - (masses-1)/2.0) / (masses-1);
                double stretchForce = middleStretch * Math.exp(-distanceFromMiddle * 10);
                force[i] += stretchForce;
            }
            
            // 更新
            for (int i = 1; i < masses - 1; i++) {
                v[i] += force[i] * dt;
                x[i] += v[i] * dt;
                
                // 简单的阻尼
                v[i] *= 0.99;
            }
        }
        
        // 确保精确的0和1
        x[0] = 0;
        x[masses-1] = 1;
        
        return x;
    }
}