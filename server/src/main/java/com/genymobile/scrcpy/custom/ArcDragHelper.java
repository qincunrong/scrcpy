// ParabolicDragService.java
package com.genymobile.scrcpy.custom;

import android.content.Intent;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import com.genymobile.scrcpy.wrappers.InputManager;

import java.util.ArrayList;
import java.util.List;

public class ArcDragHelper {
    
    private static final String TAG = OcrConfig.getLogGroup()+"ArcDragHelper";
    

    public void start(Intent intent) {
        if (intent != null) {
            int startX = intent.getIntExtra("startX", 0);
            int startY = intent.getIntExtra("startY", 0);
            int endX = intent.getIntExtra("endX", 0);
            int endY = intent.getIntExtra("endY", 0);
            long duration = intent.getLongExtra("duration", 500);
            
            new Thread(() -> {
                performParabolicDrag(startX, startY, endX, endY, duration);
            }).start();
        }
    }
    
    /**
     * 执行抛物线拖动
     */
    private void performParabolicDrag(int startX, int startY, 
                                     int endX, int endY, long duration) {
        try {
            // 1. 生成轨迹点
            List<Point> points = generateParabolicPoints(
                startX, startY, endX, endY, 15, 0.3f);
            
            if (points.isEmpty()) {
                return;
            }
            
            // 2. 获取事件时间
            long downTime = SystemClock.uptimeMillis();
            
            // 3. 按下事件
            Point first = points.get(0);
            injectMotionEvent(downTime, downTime, 
                MotionEvent.ACTION_DOWN, first.x, first.y);
            
            // 4. 移动事件
            long stepDelay = duration / (points.size() - 1);
            for (int i = 1; i < points.size(); i++) {
                SystemClock.sleep(stepDelay);
                
                Point point = points.get(i);
                long eventTime = downTime + (stepDelay * i);
                
                injectMotionEvent(downTime, eventTime,
                    MotionEvent.ACTION_MOVE, point.x, point.y);
            }
            
            // 5. 抬起事件
            Point last = points.get(points.size() - 1);
            long upTime = downTime + duration;
            injectMotionEvent(downTime, upTime,
                MotionEvent.ACTION_UP, last.x, last.y);
                
        } catch (Exception e) {
            Log.e(TAG, "Parabolic drag failed", e);
        }
    }
    
    /**
     * 生成抛物线轨迹点
     */
    private List<Point> generateParabolicPoints(int startX, int startY,
                                               int endX, int endY,
                                               int pointCount, float heightFactor) {
        List<Point> points = new ArrayList<>();
        
        // 使用三次贝塞尔曲线获得更自然的抛物线
        int cp1x = startX + (endX - startX) / 3;
        int cp1y = startY - (int)(Math.abs(endY - startY) * heightFactor);
        
        int cp2x = startX + (endX - startX) * 2 / 3;
        int cp2y = endY - (int)(Math.abs(endY - startY) * heightFactor);
        
        for (int i = 0; i < pointCount; i++) {
            float t = (float) i / (pointCount - 1);
            
            // 三次贝塞尔曲线公式
            float x = (float)(Math.pow(1 - t, 3) * startX +
                    3 * Math.pow(1 - t, 2) * t * cp1x +
                    3 * (1 - t) * Math.pow(t, 2) * cp2x +
                    Math.pow(t, 3) * endX);
            
            float y = (float)(Math.pow(1 - t, 3) * startY +
                    3 * Math.pow(1 - t, 2) * t * cp1y +
                    3 * (1 - t) * Math.pow(t, 2) * cp2y +
                    Math.pow(t, 3) * endY);
            
            points.add(new Point((int)x, (int)y));
        }
        
        return points;
    }
    
    /**
     * 注入MotionEvent
     */
    private void injectMotionEvent(long downTime, long eventTime,
                                  int action, float x, float y) {
        try {
            MotionEvent event = MotionEvent.obtain(
                downTime, eventTime, action, x, y, 0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            
            // 使用反射调用InputManager
            Class<?> inputManagerClass = Class.forName("android.hardware.input.InputManager");
            java.lang.reflect.Method getInstanceMethod = 
                inputManagerClass.getDeclaredMethod("getInstance");
            Object inputManager = getInstanceMethod.invoke(null);
            
            java.lang.reflect.Method injectInputEventMethod = 
                inputManagerClass.getMethod("injectInputEvent", 
                    InputEvent.class, int.class);
            injectInputEventMethod.invoke(inputManager, event, 
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
                
            event.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject event", e);
        }
    }

}