//package com.genymobile.scrcpy.custom.controll;
//
//import com.genymobile.scrcpy.control.Controller;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ArcDragSimulator {
//    private final int screenWidth;
//    private final int screenHeight;
//    private final long minInterval = 16; // ms
//    private final long maxInterval = 100; // ms
//
//    public ArcDragSimulator(int screenWidth, int screenHeight) {
//        this.screenWidth = screenWidth;
//        this.screenHeight = screenHeight;
//    }
//
//    public static class Point {
//        public float x;
//        public float y;
//
//        public Point(float x, float y) {
//            this.x = x;
//            this.y = y;
//        }
//    }
//
//    /**
//     * 生成圆弧拖动轨迹点
//     * @param startX 起点X坐标
//     * @param startY 起点Y坐标
//     * @param endX 终点X坐标
//     * @param endY 终点Y坐标
//     * @return 轨迹点列表（包含起点和终点）
//     */
//    public List<Point> generateArcDragPoints(float startX, float startY, float endX, float endY) {
//        List<Point> points = new ArrayList<>();
//
//        // 添加起点
//        points.add(new Point(startX, startY));
//
//        // 计算距离和弧高
//        float dx = endX - startX;
//        float dy = endY - startY;
//        float distance = (float) Math.sqrt(dx * dx + dy * dy);
//
//        // 弧高为距离的20%，但不超过屏幕的1/3
//        float arcHeight = distance * 0.2f;
//        float maxArcHeight = Math.min(screenWidth, screenHeight) * 0.3f;
//        arcHeight = Math.min(arcHeight, maxArcHeight);
//
//        // 确保圆弧不会超出屏幕
//        float controlY = (startY + endY) / 2 - arcHeight;
//        if (controlY < 0) {
//            arcHeight = (startY + endY) / 2;
//        } else if (controlY > screenHeight) {
//            arcHeight = (startY + endY) / 2 - screenHeight;
//        }
//        arcHeight = Math.max(arcHeight, distance * 0.1f); // 最小为距离的10%
//
//        // 控制点（贝塞尔曲线的控制点）
//        float controlX = (startX + endX) / 2;
//        float controlYFinal = (startY + endY) / 2 - arcHeight;
//
//        // 确保控制点在屏幕内
//        controlX = Math.max(0, Math.min(controlX, screenWidth));
//        controlYFinal = Math.max(0, Math.min(controlYFinal, screenHeight));
//
//        // 生成轨迹点数量，基于距离动态调整
//        int numPoints = Math.max(10, (int) (distance / 5));
//        numPoints = Math.min(numPoints, 100); // 限制最大点数
//
//        // 生成贝塞尔曲线点
//        for (int i = 1; i < numPoints - 1; i++) {
//            float t = (float) i / (numPoints - 1);
//
//            // 应用缓动函数（先加速后减速）
//            float easedT = easeInOut(t);
//
//            // 二次贝塞尔曲线公式：B(t) = (1-t)^2 * P0 + 2*t*(1-t) * P1 + t^2 * P2
//            float x = (1 - easedT) * (1 - easedT) * startX +
//                      2 * easedT * (1 - easedT) * controlX +
//                      easedT * easedT * endX;
//            float y = (1 - easedT) * (1 - easedT) * startY +
//                      2 * easedT * (1 - easedT) * controlYFinal +
//                      easedT * easedT * endY;
//
//            // 确保点不超出屏幕
//            x = Math.max(0, Math.min(x, screenWidth - 1));
//            y = Math.max(0, Math.min(y, screenHeight - 1));
//
//            points.add(new Point(x, y));
//        }
//
//        // 添加终点
//        points.add(new Point(endX, endY));
//
//        return points;
//    }
//
//    /**
//     * 缓动函数：先加速后减速
//     */
//    private float easeInOut(float t) {
//        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
//    }
//
//    /**
//     * 执行圆弧拖动
//     */
//    public void performArcDrag(Controller controller, float startX, float startY,
//                               float endX, float endY, long pointerId) {
//        List<Point> points = generateArcDragPoints(startX, startY, endX, endY);
//
//        try {
//            // 发送down事件
//            controller.injectTouchEvent(pointerId, MotionEvent.ACTION_DOWN,
//                                        startX, startY, 1.0f, 0);
//
//            // down事件后延迟300ms
//            Thread.sleep(300);
//
//            // 发送move事件
//            long lastMoveTime = System.currentTimeMillis();
//            for (Point point : points) {
//                long currentTime = System.currentTimeMillis();
//                long elapsed = currentTime - lastMoveTime;
//
//                // 确保move事件间隔在16ms到100ms之间
//                if (elapsed < minInterval) {
//                    Thread.sleep(minInterval - elapsed);
//                } else if (elapsed > maxInterval) {
//                    // 如果间隔过大，直接发送
//                }
//
//                controller.injectTouchEvent(pointerId, MotionEvent.ACTION_MOVE,
//                                            point.x, point.y, 1.0f, 0);
//
//                lastMoveTime = System.currentTimeMillis();
//
//                // 最后一个move事件前的延迟
//                if (point == points.get(points.size() - 2)) { // 倒数第二个点
//                    Thread.sleep(200);
//                }
//            }
//
//            // 发送up事件
//            controller.injectTouchEvent(pointerId, MotionEvent.ACTION_UP,
//                                        endX, endY, 1.0f, 0);
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    /**
//     * 异步执行圆弧拖动
//     */
//    public void performArcDragAsync(Controller controller, float startX, float startY,
//                                    float endX, float endY, long pointerId) {
//        new Thread(() -> {
//            performArcDrag(controller, startX, startY, endX, endY, pointerId);
//        }).start();
//    }
//}
//
//
//// 如果需要更精确的时间控制，可以添加时间戳版本
//public class TimedArcDragSimulator extends ArcDragSimulator {
//    public TimedArcDragSimulator(int screenWidth, int screenHeight) {
//        super(screenWidth, screenHeight);
//    }
//
//    public List<TimedPoint> generateTimedArcDragPoints(float startX, float startY,
//                                                       float endX, float endY) {
//        List<Point> points = super.generateArcDragPoints(startX, startY, endX, endY);
//        List<TimedPoint> timedPoints = new ArrayList<>();
//
//        long totalDuration = 1000; // 总时长1000ms
//        long downDelay = 300;
//        long upDelay = 200;
//
//        // 计算每个点的时间（先加速后减速的时间分布）
//        for (int i = 0; i < points.size(); i++) {
//            Point p = points.get(i);
//            float t = (float) i / (points.size() - 1);
//
//            // 使用缓动函数计算时间比例
//            float timeRatio = easeInOut(t);
//
//            // 计算时间戳（从down事件后开始计算）
//            long timestamp = downDelay + (long)(timeRatio * (totalDuration - downDelay - upDelay));
//
//            timedPoints.add(new TimedPoint(p.x, p.y, timestamp));
//        }
//
//        return timedPoints;
//    }
//
//    public static class TimedPoint extends Point {
//        public long timestamp;
//
//        public TimedPoint(float x, float y, long timestamp) {
//            super(x, y);
//            this.timestamp = timestamp;
//        }
//    }
//}