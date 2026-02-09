// ParabolicDragService.java
package com.genymobile.scrcpy.custom.controll;

import android.graphics.Point;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成抛物线轨迹点
 * 说明：在x1,y1点按下并保持，以弧线方式在timeSpan时间内(单位：毫秒）移动到x2,y2点，并释放。
 * 要注意的是，弧线方式移动的过程中，要有一开始加速移动，到达目标点时，要减速移动。
 *
 * 轨迹是抛物线 抛物先轨迹在屏幕内
 * down事件+第一次延迟(长模拟拖动过程) + 移动事件（先加速后减速） + 最后一次延迟（模拟拖动放下）+抬起事件
 *
 */
public class MockDragImpl {
    
    private static final String TAG = "MockDragImpl";
    public void startDrag(int displayId, int startX, int startY,
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
            injectMotionEvent(displayId,downTime, downTime,
                MotionEvent.ACTION_DOWN, first.x, first.y);
            
            // 4. 移动事件
            long stepDelay = duration / (points.size() - 1);
            for (int i = 1; i < points.size(); i++) {
                long stepDelayNew = stepDelay;
                if (i == 1) {
                    if (stepDelayNew < 500) {
                        stepDelayNew = 500;
                    }
                    Logger.i(TAG, "firstDelay:%d, stepDelay:%d", stepDelayNew, stepDelay);
                } else if (i == points.size() - 1) {
                    if (stepDelayNew < 300) {
                        stepDelayNew = 300;
                    }
                    Logger.i(TAG, "lastDelay:%d", stepDelayNew);
                }
                SystemClock.sleep(stepDelayNew);

                Point point = points.get(i);
                long eventTime = downTime + (stepDelayNew * i);

                injectMotionEvent(displayId, downTime, eventTime,
                        MotionEvent.ACTION_MOVE, point.x, point.y);
            }
            
            // 5. 抬起事件
            Point last = points.get(points.size() - 1);
            long upTime = downTime + duration;
            injectMotionEvent(displayId,downTime, upTime,
                MotionEvent.ACTION_UP, last.x, last.y);
                
        } catch (Exception e) {
            Logger.e(TAG, "startDrag, exception:", e);
        }
    }
    

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
    
    private void injectMotionEvent(int diaplayId,long downTime, long eventTime,
                                  int action, float x, float y) {
        try {
            Logger.i(TAG,"injectMotionEvent, x:%f, y:%f",x,y);
            MotionEvent event = MotionEvent.obtain(
                downTime, eventTime, action, x, y, 0);
            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            Device.injectEvent(event, diaplayId, Device.INJECT_MODE_ASYNC);
        } catch (Exception e) {
            Logger.e(TAG, "injectMotionEvent， exception:", e);
        }
    }

}