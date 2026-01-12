package com.genymobile.scrcpy.custom.controll;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ArcCircleDragImpl {
    private final int screenWidth;
    private final int screenHeight;
    private final boolean debugMode;
    private long mDownTime;
    private int mDisplayId;
    public static final String TAG = ScrcpyConfig.getLogGroup() + "ParabolicDrag";

    public ArcCircleDragImpl(int width, int height, boolean debug,int displayId) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.debugMode = debug;
        this.mDisplayId = displayId;
    }



    /**
     * 模拟抛物线拖动（核心方法）
     */
    private void simulateArcDrag(DragConfig config) {
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
        ArcTrajectory trajectory = calculateArcTrajectory(config);
        logDebug("圆弧信息:"+trajectory);

        List<DragPoint> movePoints = generateAccelDecelPoints(trajectory, config);

        // 5. 发送 MOVE 事件
        long moveStartTime = overallStartTime + config.startDelay;
        sendMoveEvents(movePoints, moveStartTime, config);

        // 6. UP前延迟 200ms
        if (config.endDelay > 0) {
            // 在最后位置发送一个保持事件
//            DragPoint lastPoint = movePoints.get(movePoints.size() - 1);
//            long holdTime = lastPoint.timestamp;
//            sendTouchEvent(MotionEvent.ACTION_MOVE, (int) lastPoint.x, (int) lastPoint.y, holdTime);
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
    private ArcTrajectory calculateArcTrajectory(DragConfig config) {
        ArcTrajectory trajectory = new ArcTrajectory();
        trajectory.start = new Point(config.startX, config.startY);
        trajectory.end = new Point(config.endX, config.endY);

        // 计算距离
        double distance = Math.sqrt(
                Math.pow(config.endX - config.startX, 2) +
                        Math.pow(config.endY - config.startY, 2)
        );
        double arcHeight = distance * config.parabolaHeightRatio;
        Point pointerC = ArcCalculator.calculatePointC(trajectory.start, trajectory.end, arcHeight);
        trajectory.control = pointerC;
        trajectory.arcHeight = (int) arcHeight;
        return trajectory;
    }



    /**
     * 生成先加速后减速的轨迹点（间隔16-100ms）
     */
    private List<DragPoint> generateAccelDecelPoints(
            ArcTrajectory trajectory, DragConfig config) {
        List<DragPoint> points = new ArrayList<>();

        int steps = calculateOptimalSteps(config.totalDuration, config.minInterval, config.maxInterval);

        logDebug("生成 " + steps + " 个轨迹点，总时间: " + config.totalDuration + "ms");
//        List<Point> pointList= ArcCalculator.calculateArcPoints(trajectory.start, trajectory.end, trajectory.arcHeight, steps);
        List<Point> pointList= ArcCalculator.calculateArcPointsAcceDec(trajectory.start, trajectory.end, trajectory.arcHeight, steps);
        for (int i = 0; i < pointList.size(); i++) {
            DragPoint dragPoint = new DragPoint();
            Point pointItem = pointList.get(i);

            // 进度（0到1）
            float progress = (float) i / steps;

            // 应用先加速后减速的时间曲线
//            float easedProgress = applyAccelDecelEasing(progress);
//            float easedProgress = progress;

            // 计算抛物线上的点
//            Point position = calculateBezierPoint(easedProgress,
//                    trajectory.start, trajectory.control, trajectory.end);

            // 确保在屏幕内
            double x = clamp(pointItem.x, 0, screenWidth - 1);
            double y = clamp(pointItem.y, 0, screenHeight - 1);

            dragPoint.x = x;
            dragPoint.y = y;
            dragPoint.progress = progress;
            points.add(dragPoint);
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

//                if (interval < config.minInterval) {
//                    // 如果间隔太小，调整目标时间
//                    targetTime = lastEventTime + config.minInterval;
//                    logDebug("调整间隔: " + interval + "ms -> " + config.minInterval + "ms");
//                } else if (interval > config.maxInterval) {
//                    // 如果间隔太大，调整目标时间
//                    targetTime = lastEventTime + config.maxInterval;
//                    logDebug("调整间隔: " + interval + "ms -> " + config.maxInterval + "ms");
//                }
            }

            // 等待到目标时间
            long waitTime = targetTime - SystemClock.uptimeMillis();
            if (waitTime > 0) {
                sleep((int) waitTime);
            }

            // 发送 MOVE 事件
            sendTouchEvent(MotionEvent.ACTION_MOVE, (int) point.x, (int) point.y, targetTime);
            lastEventTime = targetTime;
        }
    }

    /**
     * 发送触摸事件（使用 scrcpy 的 Device 接口）
     */
    long mLastEventTime;
    private void sendTouchEvent(int action, int x, int y, long timestamp) {
        try {
            if (debugMode) {
                String actionName = getActionName(action);
                logDebug(String.format("发送事件: %s (%d, %d) , time:%d, offsetDown:%d, offsetLast:%d",
                        actionName, x,y, timestamp,(timestamp-mDownTime),timestamp-mLastEventTime));
            }
            mLastEventTime = timestamp;
            MotionEvent event = createMotionEvent(action, x, y, timestamp);
            Device.injectEvent(event, mDisplayId, Device.INJECT_MODE_ASYNC);
            event.recycle();
        } catch (Exception e) {
            logError("发送触摸事件失败: " + e.getMessage());
        }
    }
    private String getActionName(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: return "ACTION_DOWN";
            case MotionEvent.ACTION_MOVE: return "ACTION_MOVE";
            case MotionEvent.ACTION_UP: return "ACTION_UP";
            default: return "ACTION_UNKNOWN";
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
    private double clamp(double value, double min, double max) {
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
            try {
                Logger.i(TAG,  message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private static class ArcTrajectory {
        Point start;
        Point end;
        Point control;
        int arcHeight;

        @Override
        public String toString() {
            return "{" +
                    "start=" + start +
                    ", end=" + end +
                    ", control=" + control +
                    ", arcHeight=" + arcHeight +
                    '}';
        }
    }

    private static class DragPoint {
        double x;
        double y;
        float progress;  // 0 到 1
        long timestamp;  // 事件时间戳
    }

    /**
     * 高级功能：带超时保护的拖动
     */
    public void startArcDrag(DragConfig config) {
        try {
            // 执行拖动
            simulateArcDrag(config);
        } finally {
        }
    }
}