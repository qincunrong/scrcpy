package com.genymobile.scrcpy.custom.controll;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.device.Device;
import com.genymobile.scrcpy.util.Logger;


public class MockDoubleClickImpl {
    
    private static final String TAG ="MockDoubleClickImpl";
    public void startDoubleClick(int displayId, int startX, int startY) {
        try {

            Logger.i(TAG,"startDoubleClick, x:%d, y:%d",startX,startY);
            long downTime = SystemClock.uptimeMillis();

            injectMotionEvent(displayId,downTime, downTime,
                MotionEvent.ACTION_DOWN, startX, startY);

            long upTime = SystemClock.uptimeMillis();
            injectMotionEvent(displayId,downTime, upTime,
                    MotionEvent.ACTION_UP, startX, startY);

            try {
                SystemClock.sleep(50);
            } catch (Exception e) {
            }

            long downTime2 = SystemClock.uptimeMillis();
            injectMotionEvent(displayId,downTime2, downTime2,
                    MotionEvent.ACTION_DOWN, startX, startY);

            long upTime2 = SystemClock.uptimeMillis();
            injectMotionEvent(displayId,downTime2, upTime2,
                    MotionEvent.ACTION_UP, startX, startY);

        } catch (Exception e) {
            Logger.e(TAG, "startDoubleClick exception", e);
        }
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
            Logger.e(TAG, "injectMotionEvent, exception:", e);
        }
    }

}