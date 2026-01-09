package com.genymobile.scrcpy.custom.controll;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ParabolicDragSimulator1 {
    private final int screenWidth;
    private final int screenHeight;
    private final boolean debugMode;
    private int mDisplayId;
    private long mDownTime;
    public static final String TAG = ScrcpyConfig.getLogGroup() + "DragSimulator";

    public ParabolicDragSimulator1(int width, int height, boolean debug, int displayId) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.debugMode = debug;
        this.mDisplayId = displayId;
    }
    
    /**
     * 增强版抛物线拖动模拟
     * @param startX 起始X
     * @param startY 起始Y
     * @param endX 结束X
     * @param endY 结束Y
     * @param totalDuration 总时间(ms) - 不包括额外延迟
     * @param startDelay DOWN后延迟(ms)
     * @param endDelay UP前延迟(ms)
     * @param peakRatio 抛物线高度比例(0.0-1.0)
     */
    public void simulateEnhancedParabolicDrag(int startX, int startY,
                                             int endX, int endY,
                                             int totalDuration,
                                             int startDelay,
                                             int endDelay,
                                             float peakRatio) {
        long overallStartTime = System.currentTimeMillis();
        totalDuration = totalDuration - startDelay - endDelay;
        if (debugMode) {
            logDebugInfo("开始增强抛物线拖动模拟");
            logDebugInfo(String.format("起始点: (%d, %d)", startX, startY));
            logDebugInfo(String.format("结束点: (%d, %d)", endX, endY));
            logDebugInfo(String.format("总时间: %dms, 开始延迟: %dms, 结束延迟: %dms", 
                                      totalDuration, startDelay, endDelay));
        }
        
        // 1. ACTION_DOWN (按下)
         mDownTime = overallStartTime;
        sendTouchEvent(MotionEvent.ACTION_DOWN, startX, startY, mDownTime,mDownTime);
        logDebugInfo(String.format("ACTION_DOWN 时间戳: %d", mDownTime));
        
        // 2. 开始延迟 (模拟用户按下后的确认/准备时间)
        logDebugInfo(String.format("开始延迟 %dms...", startDelay));
        sleep(startDelay);
        
        // 3. 计算安全的抛物线参数
        ParabolicParams params = calculateSafeParabolicParams(
            startX, startY, endX, endY, peakRatio);
        
        if (debugMode) {
            logDebugInfo(String.format("控制点: (%f, %f)",
                                      params.control.x, params.control.y));
            logDebugInfo(String.format("抛物线高度: %.1fpx", params.maxHeight));
        }
        
        // 4. 生成带加速减速的轨迹点
        long moveStartTime = overallStartTime + startDelay;
        List<DragPoint> movePoints = generateAccelDecelParabolicPoints(
            params, totalDuration, moveStartTime);
        
        // 5. 发送移动事件
        logDebugInfo(String.format("开始移动，共%d个点", movePoints.size()));
        sendMovementEvents(movePoints);
        
        // 6. UP前延迟 (模拟放下前的确认/悬停时间)
        if (endDelay > 0) {
            logDebugInfo(String.format("UP前延迟 %dms...", endDelay));
            
            // 在最后位置发送一个保持事件
//            DragPoint lastPoint = movePoints.get(movePoints.size() - 1);
//            long holdTime = lastPoint.timestamp;
//
//            // 在延迟期间保持位置不变
//            sendTouchEvent(MotionEvent.ACTION_MOVE, lastPoint.x, lastPoint.y,mDownTime, holdTime);
            sleep(endDelay);
        }
        
        // 7. ACTION_UP (抬起)
        long upTime = overallStartTime + startDelay + totalDuration + endDelay;
        sendTouchEvent(MotionEvent.ACTION_UP, endX, endY, mDownTime,upTime);
        logDebugInfo(String.format("ACTION_UP 时间戳: %d", upTime));
        
        if (debugMode) {
            long totalElapsed = System.currentTimeMillis() - overallStartTime;
            logDebugInfo(String.format("总耗时: %dms", totalElapsed));
            logDebugInfo("增强抛物线拖动模拟完成");
        }
    }
    
    /**
     * 计算安全的抛物线参数（确保不超出屏幕）
     */
    private ParabolicParams calculateSafeParabolicParams(int startX, int startY,
                                                        int endX, int endY,
                                                        float desiredPeakRatio) {
        ParabolicParams params = new ParabolicParams();
        params.start = new Point(startX, startY);
        params.end = new Point(endX, endY);
        
        // 计算中间点
        int midX = (startX + endX) / 2;
        int midY = (startY + endY) / 2;
        
        // 计算两点距离
        double distance = Math.sqrt(
            Math.pow(endX - startX, 2) + 
            Math.pow(endY - startY, 2)
        );
        
        // 计算基础抛物线高度
        int baseHeight = (int) (distance * desiredPeakRatio);
        
        // 根据方向确定抛物线朝向
        boolean isMovingDown = startY < endY;
        
        // 计算最大允许高度（确保不超出屏幕）
        int maxAllowedHeight;
        if (isMovingDown) {
            // 向下拖动：抛物线向上凸起
            // 控制点不能小于0，且留有一定边距
            maxAllowedHeight = Math.min(midY, screenHeight / 4);
        } else {
            // 向上拖动：抛物线向下凹陷
            // 控制点不能大于屏幕高度，且留有一定边距
            maxAllowedHeight = Math.min(screenHeight - midY, screenHeight / 4);
        }
        
        // 应用限制
        int actualHeight = Math.min(baseHeight, maxAllowedHeight);
        
        // 计算控制点
        float controlX = midX;
        float controlY;
        
        if (isMovingDown) {
            controlY = midY - actualHeight;
        } else {
            controlY = midY + actualHeight;
        }
        
        // 确保控制点在屏幕内（额外安全边界）
        int margin = 10; // 像素边距
        controlX = clamp(controlX, margin, screenWidth - margin - 1);
        controlY = clamp(controlY, margin, screenHeight - margin - 1);
        
        // 验证控制点不会导致轨迹超出屏幕
        controlY = adjustControlPointForSafety(params.start, params.end, 
                                              new Point(controlX, controlY));
        
        params.control = new Point(controlX, controlY);
        params.maxHeight = actualHeight;
        
        return params;
    }
    
    /**
     * 调整控制点以确保整个轨迹在屏幕内
     */
    private float adjustControlPointForSafety(Point start, Point end, Point control) {
        // 采样轨迹上的点来检查是否超出屏幕
        int safetyMargin = 5;
        float maxY = control.y;
        
        for (float t = 0; t <= 1; t += 0.1) {
            Point point = calculateBezierPoint(t, start, control, end);
            
            // 如果超出边界，调整控制点
            if (point.y < safetyMargin) {
                // 需要降低抛物线高度（向上移动控制点）
                float neededAdjustment = safetyMargin - point.y;
                return control.y + neededAdjustment;
            } else if (point.y > screenHeight - safetyMargin) {
                // 需要降低抛物线高度（向下移动控制点）
                float neededAdjustment = point.y - (screenHeight - safetyMargin);
                return control.y - neededAdjustment;
            }
            
            // 记录最大Y值
            if (Math.abs(point.y - (start.y + end.y) / 2) > Math.abs(maxY - (start.y + end.y) / 2)) {
                maxY = point.y;
            }
        }
        
        return control.y;
    }
    
    /**
     * 生成先加速后减速的抛物线轨迹点
     */
    private List<DragPoint> generateAccelDecelParabolicPoints(
            ParabolicParams params, int duration, long startTime) {
        List<DragPoint> points = new ArrayList<>();
        
        // 自适应计算帧数
        int frameCount = calculateAdaptiveFrameCount(params, duration);
        
        if (debugMode) {
            logDebugInfo(String.format("生成 %d 个轨迹点", frameCount));
        }
        
        // 生成时间点（先加速后减速分布）
        float[] timeFactors = generateAccelDecelTimeFactors(frameCount);
        
        for (int i = 0; i < frameCount; i++) {
            DragPoint point = new DragPoint();
            
            // 应用先加速后减速的时间因子
            float timeFactor = timeFactors[i];
            
            // 计算抛物线轨迹上的位置
            float t = (float) i / (frameCount - 1);
            float easedT = applyAccelDecelEasing(t);
            
            Point position = calculateBezierPoint(easedT, 
                params.start, params.control, params.end);
            
            // 安全边界检查
            position.x = clamp(position.x, 0, screenWidth - 1);
            position.y = clamp(position.y, 0, screenHeight - 1);
            
            point.x = position.x;
            point.y = position.y;
            point.timestamp = startTime + (long)(duration * timeFactor);
            point.progress = t;
            point.velocityFactor = calculateVelocityFactor(i, frameCount);
            
            points.add(point);
        }
        
        return points;
    }
    
    /**
     * 生成先加速后减速的时间因子
     */
    private float[] generateAccelDecelTimeFactors(int frameCount) {
        float[] factors = new float[frameCount];
        
        // 使用正弦函数实现平滑的加速和减速
        for (int i = 0; i < frameCount; i++) {
            float t = (float) i / (frameCount - 1);
            
            // 分段函数：前40%加速，中间20%匀速，后40%减速
            if (t < 0.4f) {
                // 加速阶段：二次函数加速
                factors[i] = (t / 0.4f) * (t / 0.4f) * 0.4f;
            } else if (t < 0.6f) {
                // 匀速阶段
                factors[i] = 0.4f + (t - 0.4f) * 0.2f;
            } else {
                // 减速阶段：二次函数减速
                float decelT = (t - 0.6f) / 0.4f;
                factors[i] = 0.6f + (1 - (1 - decelT) * (1 - decelT)) * 0.4f;
            }
        }
        
        return factors;
    }
    
    /**
     * 应用先加速后减速的缓动函数
     */
    private float applyAccelDecelEasing(float t) {
        // 更平滑的加速减速曲线
        if (t < 0.5f) {
            // 加速阶段
            return 2 * t * t;
        } else {
            // 减速阶段
            return 1 - 2 * (1 - t) * (1 - t);
        }
    }
    
    /**
     * 计算自适应帧数
     */
    private int calculateAdaptiveFrameCount(ParabolicParams params, int duration) {
        // 计算拖动距离
        double distance = Math.sqrt(
            Math.pow(params.end.x - params.start.x, 2) + 
            Math.pow(params.end.y - params.start.y, 2)
        );
        
        // 基础帧数（基于距离）
        int baseFrames = (int) (distance / 15); // 每15像素一帧
        
        // 基于持续时间的调整
        int timeBasedFrames = Math.max(10, duration / 20); // 每20ms一帧
        
        // 基于抛物线高度的调整（更高抛物线需要更多帧）
        int heightBonus = (int) (params.maxHeight / 20);
        
        // 合并计算
        int frames = Math.max(baseFrames, timeBasedFrames) + heightBonus;
        
        // 限制范围
        return (int)clamp(frames, 12, 120);
    }
    
    /**
     * 计算速度因子（用于调试）
     */
    private float calculateVelocityFactor(int currentFrame, int totalFrames) {
        float progress = (float) currentFrame / (totalFrames - 1);
        
        if (progress < 0.4f) {
            // 加速阶段：速度从0.5线性增加到1.5
            return 0.5f + progress / 0.4f;
        } else if (progress < 0.6f) {
            // 匀速阶段：保持1.5
            return 1.5f;
        } else {
            // 减速阶段：速度从1.5线性减少到0.5
            return 1.5f - (progress - 0.6f) / 0.4f;
        }
    }
    
    /**
     * 发送移动事件
     */
    private void sendMovementEvents(List<DragPoint> points) {
        long lastTimestamp = 0;
        
        for (int i = 0; i < points.size(); i++) {
            DragPoint point = points.get(i);
            
            // 计算与上一个事件的时间间隔
            if (i > 0 && lastTimestamp > 0) {
                long interval = point.timestamp - lastTimestamp;
                if (interval > 0) {
                    sleep((int) interval);
                }
            }
            
            // 发送移动事件
            sendTouchEvent(MotionEvent.ACTION_MOVE, point.x, point.y, mDownTime,point.timestamp);
            
            if (debugMode && i % 5 == 0) {
                logDebugInfo(String.format("移动点[%d]: (%f, %f) 时间戳: %d 速度因子: %.2f",
                                          i, point.x, point.y, 
                                          point.timestamp, point.velocityFactor));
            }
            
            lastTimestamp = point.timestamp;
        }
    }
    
    /**
     * 计算二次贝塞尔曲线上的点
     */
    private Point calculateBezierPoint(float t, Point p0, Point p1, Point p2) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        
        float x = uu * p0.x + 2 * u * t * p1.x + tt * p2.x;
        float y = uu * p0.y + 2 * u * t * p1.y + tt * p2.y;
        
        return new Point((int) x, (int) y);
    }
    
    /**
     * 边界限制函数
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 睡眠函数
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logDebugInfo("睡眠被中断");
        }
    }
    
    /**
     * 发送触摸事件（模拟scrcpy server接口）
     */
    private void sendTouchEvent(int action, float x, float y, long downTime, long eventTime) {
        // 这里调用实际的scrcpy server接口
        // 例如：ControlMessage.createInjectTouchEvent(...)
        if (debugMode) {
            String actionName = getActionName(action);
            logDebugInfo(String.format("发送事件: %s (%f, %f) 时间戳: %d",
                                      actionName, x,y, eventTime-downTime));
        }

        try {
//            Logger.i(TAG,"injectMotionEvent, x:%f, y:%f, displayId:%d",x,y,mDisplayId);
            MotionEvent event = MotionEvent.obtain(
                    downTime, eventTime, action, x, y, 0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            Device.injectEvent(event, mDisplayId, Device.INJECT_MODE_ASYNC);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "injectMotionEvent， exception:"+e.getMessage());
        }
        // 实际发送代码
        // server.sendTouchEvent(action, x, y, timestamp);
    }


    
    /**
     * 获取动作名称（用于调试）
     */
    private String getActionName(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: return "ACTION_DOWN";
            case MotionEvent.ACTION_MOVE: return "ACTION_MOVE";
            case MotionEvent.ACTION_UP: return "ACTION_UP";
            default: return "ACTION_UNKNOWN";
        }
    }
    
    /**
     * 调试日志
     */
    private void logDebugInfo(String message) {
        if (debugMode) {
            try {
                Log.d(TAG, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // ==================== 内部类定义 ====================
    
    private static class ParabolicParams {
        Point start;
        Point end;
        Point control;
        float maxHeight;
    }
    
    private static class DragPoint {
        float x;
        float y;
        long timestamp;
        float progress;      // 进度 [0, 1]
        float velocityFactor; // 速度因子
    }
    
    private static class Point {
        float x;
        float y;
        
        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // ==================== 使用示例 ====================
    
    /**
     * 示例：在主函数中调用
     */
    public static void main(String[] args) {
        // 假设屏幕尺寸为1080x2400
        int screenWidth = 1080;
        int screenHeight = 2400;
        
        // 创建模拟器（开启调试模式）
        ParabolicDragSimulator1 simulator =
            new ParabolicDragSimulator1(screenWidth, screenHeight, true,0);
        
        // 拖动参数
        int startX = 200;
        int startY = 500;
        int endX = 800;
        int endY = 1800;
        
        // 时间参数
        int totalDuration = 800;  // 移动总时间800ms
        int startDelay = 300;     // DOWN后延迟300ms
        int endDelay = 200;       // UP前延迟200ms
        float peakRatio = 0.25f;  // 抛物线高度比例
        
        // 执行抛物线拖动
        simulator.simulateEnhancedParabolicDrag(
            startX, startY, endX, endY,
            totalDuration, startDelay, endDelay, peakRatio
        );
    }
    
    /**
     * 智能自适应拖动
     * 根据距离自动调整参数
     */
    public void smartParabolicDrag(int startX, int startY, int endX, int endY) {
        // 计算距离
        double distance = Math.sqrt(
            Math.pow(endX - startX, 2) + 
            Math.pow(endY - startY, 2)
        );
        
        // 根据距离自适应参数
        int duration;
        int startDelay;
        int endDelay;
        float peakRatio;
        
        if (distance < 300) {
            // 短距离：快速操作
            duration = 500;
            startDelay = 200;    // 较短延迟
            endDelay = 150;      // 较短结束延迟
            peakRatio = 0.15f;   // 低抛物线
        } else if (distance < 800) {
            // 中距离：标准操作
            duration = 700;
            startDelay = 300;    // 标准延迟
            endDelay = 200;      // 标准结束延迟
            peakRatio = 0.25f;   // 中等抛物线
        } else {
            // 长距离：缓慢操作
            duration = 1000;
            startDelay = 400;    // 较长延迟
            endDelay = 300;      // 较长结束延迟
            peakRatio = 0.3f;    // 较高抛物线
        }
        
        // 执行拖动
        simulateEnhancedParabolicDrag(
            startX, startY, endX, endY,
            duration, startDelay, endDelay, peakRatio
        );
    }
}