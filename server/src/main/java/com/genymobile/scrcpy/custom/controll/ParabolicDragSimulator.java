package com.genymobile.scrcpy.custom.controll;

import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ParabolicDragSimulator {
    private final int screenWidth;
    private final int screenHeight;
    private final boolean debugMode;
    private long mDownTime;
    private int mDisplayId;
    public static final String TAG = ScrcpyConfig.getLogGroup() + "ParabolicDrag";

    public ParabolicDragSimulator(int width, int height, boolean debug,int displayId) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.debugMode = debug;
        this.mDisplayId = displayId;
    }

    /**
     * 执行抛物线拖动
     */
    public void executeParabolicDrag(int startX, int startY, int endX, int endY) {
        DragConfig config = new DragConfig(startX, startY, endX, endY);
        config.setStartDelay(300);        // DOWN后延迟300ms
        config.setEndDelay(200);          // UP前延迟200ms
        config.setMinInterval(16);        // 最小间隔16ms
        config.setMaxInterval(100);       // 最大间隔100ms
        config.setParabolaHeightRatio(0.8f); // 抛物线高度比例
        config.setTotalDuration(800);     // 总拖动时间800ms

        simulateParabolicDrag(config);
    }

    /**
     * 模拟抛物线拖动（核心方法）
     */
    private void simulateParabolicDrag(DragConfig config) {
        long overallStartTime = SystemClock.uptimeMillis();
        mDownTime = overallStartTime;
        // 1. ACTION_DOWN
        sendTouchEvent(MotionEvent.ACTION_DOWN, config.startX, config.startY, overallStartTime);
        logDebug("ACTION_DOWN at (" + config.startX + "," + config.startY + ")");

        // 2. 开始延迟 300ms
        if (config.startDelay > 0) {
            logDebug("开始延迟:"+config.startDelay);
            sleep(config.startDelay);
        }

        // 3. 计算安全的抛物线轨迹
        ParabolicTrajectory trajectory = calculateSafeParabolicTrajectory(config);
        logDebug("抛物线信息:"+trajectory);


        // 4. 生成带加速减速的轨迹点（间隔16-100ms）
        List<DragPoint> movePoints = generateAccelDecelTrajectoryPoints(trajectory, config);

        // 5. 发送 MOVE 事件
        long moveStartTime = overallStartTime + config.startDelay;
        sendMoveEvents(movePoints, moveStartTime, config);

        // 6. UP前延迟 200ms
        if (config.endDelay > 0) {
            // 在最后位置发送一个保持事件
            DragPoint lastPoint = movePoints.get(movePoints.size() - 1);
            long holdTime = lastPoint.timestamp;
            sendTouchEvent(MotionEvent.ACTION_MOVE, lastPoint.x, lastPoint.y, holdTime);
            sleep(config.endDelay);
        }
        // 7. ACTION_UP
        long upTime = overallStartTime + config.startDelay + config.totalDuration + config.endDelay;
        sendTouchEvent(MotionEvent.ACTION_UP, config.endX, config.endY, upTime);
        logDebug("ACTION_UP at (" + config.endX + "," + config.endY + ")");
    }

    /**
     * 计算安全的抛物线轨迹（确保不超出屏幕）
     */
    private ParabolicTrajectory calculateSafeParabolicTrajectory(DragConfig config) {
        ParabolicTrajectory trajectory = new ParabolicTrajectory();
        trajectory.start = new Point(config.startX, config.startY);
        trajectory.end = new Point(config.endX, config.endY);

        // 计算中间点
        int midX = (config.startX + config.endX) / 2;
        int midY = (config.startY + config.endY) / 2;

        // 计算距离
        double distance = Math.sqrt(
            Math.pow(config.endX - config.startX, 2) +
            Math.pow(config.endY - config.startY, 2)
        );


        // 计算抛物线高度
        int parabolaHeight = (int)(distance * config.parabolaHeightRatio);

        // 确定抛物线方向（根据拖动方向）
//        boolean isMovingDown = config.startY < config.endY;
        boolean isMovingDown = true;

        // 计算控制点（贝塞尔曲线控制点）
        int controlX = midX;
        int controlY;

        if (isMovingDown) {
            controlY = midY - parabolaHeight;  // 向下拖动：抛物线向上凸起
        } else {
            controlY = midY + parabolaHeight;  // 向上拖动：抛物线向下凹陷
        }

        // 确保控制点在屏幕内
        controlX = clamp(controlX, 10, screenWidth - 10);
        controlY = clamp(controlY, 10, screenHeight - 10);

        // 验证整个轨迹在屏幕内
        Point safeControl = ensureTrajectoryInScreen(trajectory.start, trajectory.end, new Point(controlX, controlY));
//        Point safeControl = new Point(controlX, controlY);
//
        trajectory.control = safeControl;
        trajectory.parabolaHeight = Math.abs(controlY - midY);

        return trajectory;
    }

    /**
     * 确保整个轨迹在屏幕内
     */
    private Point ensureTrajectoryInScreen(Point start, Point end, Point control) {
        // 采样轨迹上的点
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;

        for (float t = 0; t <= 1; t += 0.02f) {
            Point point = calculateBezierPoint(t, start, control, end);
            maxY = Math.max(maxY, point.y);
            minY = Math.min(minY, point.y);
        }

        // 如果超出边界，调整控制点
        int margin = 5;  // 安全边距

        if (minY < margin || maxY > screenHeight - margin) {
            int centerY = (start.y + end.y) / 2;
            int maxAllowedHeight = screenHeight / 4;  // 最大高度为屏幕高度的1/4

            // 减小抛物线高度
            int adjustedHeight = Math.min(
                Math.abs(control.y - centerY),
                maxAllowedHeight
            );

            if (control.y < centerY) {
                control.y = centerY - adjustedHeight;
            } else {
                control.y = centerY + adjustedHeight;
            }
        }

        return control;
    }

    /**
     * 生成先加速后减速的轨迹点（间隔16-100ms）
     */
    private List<DragPoint> generateAccelDecelTrajectoryPoints(
            ParabolicTrajectory trajectory, DragConfig config) {
        List<DragPoint> points = new ArrayList<>();

        // 自适应计算步数（确保间隔在16-100ms之间）
        int steps = calculateOptimalSteps(config.totalDuration, config.minInterval, config.maxInterval);

        logDebug("生成 " + steps + " 个轨迹点，总时间: " + config.totalDuration + "ms");

        for (int i = 0; i <= steps; i++) {
            DragPoint point = new DragPoint();

            // 进度（0到1）
            float progress = (float) i / steps;

            // 应用先加速后减速的时间曲线
            float easedProgress = applyAccelDecelEasing(progress);
//            float easedProgress = progress;

            // 计算抛物线上的点
            Point position = calculateBezierPoint(easedProgress,
                trajectory.start, trajectory.control, trajectory.end);

            // 确保在屏幕内
            position.x = clamp(position.x, 0, screenWidth - 1);
            position.y = clamp(position.y, 0, screenHeight - 1);

            point.x = position.x;
            point.y = position.y;
            point.progress = progress;

            points.add(point);
        }

        return points;
    }

    /**
     * 计算最优步数（确保间隔在16-100ms之间）
     */
    private int calculateOptimalSteps(int totalDuration, int minInterval, int maxInterval) {
        // 理论步数
        int theoreticalSteps = totalDuration / ((minInterval + maxInterval) / 2);

        // 确保步数在合理范围内
        int minSteps = (int) Math.ceil((float) totalDuration / maxInterval);
        int maxSteps = (int) Math.floor((float) totalDuration / minInterval);


        int steps = Math.min(Math.max(theoreticalSteps, minSteps), maxSteps);

        // 至少3个点
        return Math.max(steps, 3);
    }

    /**
     * 先加速后减速的缓动函数
     */
    private float applyAccelDecelEasing(float t) {
        // 使用sin函数实现平滑的加速和减速
        return (float) (0.5f - Math.cos(t * Math.PI) / 2);
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
     * 发送 MOVE 事件序列
     */
    private void sendMoveEvents(List<DragPoint> points, long startTime, DragConfig config) {
        int pointCount = points.size();
        long lastEventTime = startTime;

        for (int i = 0; i < pointCount; i++) {
            DragPoint point = points.get(i);

            // 计算这个点应该发生的时间
            long targetTime = startTime + (long)(config.totalDuration * point.progress);

            // 确保最小间隔
            if (i > 0) {
                long interval = targetTime - lastEventTime;

                if (interval < config.minInterval) {
                    // 如果间隔太小，调整目标时间
                    targetTime = lastEventTime + config.minInterval;
                    logDebug("调整间隔: " + interval + "ms -> " + config.minInterval + "ms");
                } else if (interval > config.maxInterval) {
                    // 如果间隔太大，调整目标时间
                    targetTime = lastEventTime + config.maxInterval;
                    logDebug("调整间隔: " + interval + "ms -> " + config.maxInterval + "ms");
                }
            }

            // 等待到目标时间
            long waitTime = targetTime - SystemClock.uptimeMillis();
            if (waitTime > 0) {
                sleep((int) waitTime);
            }

            // 发送 MOVE 事件
            sendTouchEvent(MotionEvent.ACTION_MOVE, point.x, point.y, targetTime);

            if (debugMode && i % 5 == 0) {
                logDebug(String.format("MOVE[%d/%d]: (%d, %d) 进度: %.2f",
                    i + 1, pointCount, point.x, point.y, point.progress));
            }

            lastEventTime = targetTime;
        }
    }

    /**
     * 发送触摸事件（使用 scrcpy 的 Device 接口）
     */
    private void sendTouchEvent(int action, int x, int y, long timestamp) {
        try {
            MotionEvent event = createMotionEvent(action, x, y, timestamp);
            Device.injectEvent(event, mDisplayId, Device.INJECT_MODE_ASYNC);
            event.recycle();
        } catch (Exception e) {
            logError("发送触摸事件失败: " + e.getMessage());
        }
    }

    /**
     * 创建 MotionEvent（兼容 scrcpy 的方式）
     */
    private MotionEvent createMotionEvent(int action, int x, int y, long timestamp) {
        long downTime = mDownTime;
        long eventTime = timestamp;

        // 使用 scrcpy 的方式创建 MotionEvent
        // 注意：这里简化了多点触控的情况
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];

        pointerProperties[0] = new MotionEvent.PointerProperties();
        pointerProperties[0].id = 0;
        pointerProperties[0].toolType = MotionEvent.TOOL_TYPE_FINGER;

        pointerCoords[0] = new MotionEvent.PointerCoords();
        pointerCoords[0].x = x;
        pointerCoords[0].y = y;
        pointerCoords[0].pressure = 1.0f;
        pointerCoords[0].size = 1.0f;

        return MotionEvent.obtain(
            downTime,                   // downTime
            eventTime,                  // eventTime
            action,                     // action
            1,                          // pointerCount
            pointerProperties,          // pointerProperties
            pointerCoords,              // pointerCoords
            0,                          // metaState
            0,                          // buttonState
            1.0f,                       // xPrecision
            1.0f,                       // yPrecision
            -1,                         // deviceId (scrcpy 使用 -1)
            0,                          // edgeFlags
            InputDevice.SOURCE_TOUCHSCREEN, // source
            0                           // flags
        );
    }

    /**
     * 边界限制
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 睡眠
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 调试日志
     */
    private void logDebug(String message) {
        if (debugMode) {
            Logger.i(TAG,  message);
        }
    }

    private void logError(String message) {
        Logger.e(TAG,"[ParabolicDrag ERROR] " + message);
    }

    // ==================== 内部类 ====================

    public static class DragConfig {
        int startX, startY;
        int endX, endY;
        int startDelay = 300;       // DOWN后延迟
        int endDelay = 200;         // UP前延迟
        int minInterval = 16;       // 最小间隔
        int maxInterval = 100;      // 最大间隔
        float parabolaHeightRatio = 0.2f; // 抛物线高度比例
        int totalDuration = 800;    // 总拖动时间

        public DragConfig(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public void setStartDelay(int delay) { this.startDelay = delay; }
        public void setEndDelay(int delay) { this.endDelay = delay; }
        public void setMinInterval(int interval) { this.minInterval = interval; }
        public  void setMaxInterval(int interval) { this.maxInterval = interval; }
        public void setParabolaHeightRatio(float ratio) { this.parabolaHeightRatio = ratio; }
        public void setTotalDuration(int duration) { this.totalDuration = duration; }
    }

    private static class ParabolicTrajectory {
        Point start;
        Point end;
        Point control;
        int parabolaHeight;

        @Override
        public String toString() {
            return "{" +
                    "start=" + start +
                    ", end=" + end +
                    ", control=" + control +
                    ", parabolaHeight=" + parabolaHeight +
                    '}';
        }
    }

    private static class DragPoint {
        int x;
        int y;
        float progress;  // 0 到 1
        long timestamp;  // 事件时间戳
    }

    private static class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "{ x:"+x+", y:"+y+" }";
        }
    }

    // ==================== 使用示例 ====================

    public static void main(String[] args) {
        // 模拟场景：从 (200, 500) 拖动到 (800, 1800)
        int screenWidth = 1080;
        int screenHeight = 2400;

        // 创建 Device 实例（这里需要实际的 Device 对象）
        Device device = null; // 需要从 scrcpy 上下文获取

        // 创建模拟器
//        ParabolicDragSimulator simulator = new ParabolicDragSimulator(
//            screenWidth, screenHeight, device, true);
//
//        // 执行抛物线拖动
//        simulator.executeParabolicDrag(200, 500, 800, 1800);
    }

    /**
     * 更智能的拖动方法（根据距离自动调整参数）
     */
    public void smartParabolicDrag(int startX, int startY, int endX, int endY) {
        // 计算距离
        double distance = Math.sqrt(
            Math.pow(endX - startX, 2) +
            Math.pow(endY - startY, 2)
        );

        // 根据距离自动调整参数
        int totalDuration;
        float parabolaHeightRatio;

        if (distance < 300) {
            // 短距离：快速完成，低抛物线
            totalDuration = 500;
            parabolaHeightRatio = 0.1f;
        } else if (distance < 800) {
            // 中距离：标准参数
            totalDuration = 800;
            parabolaHeightRatio = 0.2f;
        } else {
            // 长距离：需要更长时间，更高抛物线
            totalDuration = 1200;
            parabolaHeightRatio = 0.25f;
        }

        DragConfig config = new DragConfig(startX, startY, endX, endY);
        config.setStartDelay(300);
        config.setEndDelay(200);
        config.setMinInterval(16);
        config.setMaxInterval(100);
        config.setParabolaHeightRatio(parabolaHeightRatio);
        config.setTotalDuration(totalDuration);

        simulateParabolicDrag(config);
    }

    /**
     * 高级功能：带超时保护的拖动
     */
    public void safeParabolicDragWithTimeout(DragConfig config) {
        // 设置超时监控
        Thread timeoutThread = new Thread(() -> {
            try {
                // 总超时时间：配置时间 + 2秒缓冲
                int totalTimeout = config.startDelay + config.totalDuration +
                                 config.endDelay + 2000;
                Thread.sleep(totalTimeout);

                // 如果超时，记录警告
                logError("拖动操作可能已超时");

            } catch (InterruptedException e) {
                // 正常情况：操作已完成
            }
        });

        timeoutThread.start();

        try {
            // 执行拖动
            simulateParabolicDrag(config);
        } finally {
            // 中断超时监控线程
            timeoutThread.interrupt();
        }
    }
}