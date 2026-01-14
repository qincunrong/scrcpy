package com.genymobile.scrcpy.custom.controll;

import android.os.Handler;
import android.os.HandlerThread;

public class SleepImpl {

    private static volatile SleepImpl singleton = null;
    private long mSleepStartTime;
    private long mSleepEndTime;

    private OnEventListener mListener;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private SleepImpl() {}

    public static SleepImpl getInstance() {
        if (singleton == null) {
            synchronized (SleepImpl.class) {
                if (singleton == null) {
                    singleton = new SleepImpl();
                }
            }
        }
        return singleton;
    }
    public void start(int durationInMills) {
        if (durationInMills <= 0) {
            return;
        }
        mSleepStartTime = System.currentTimeMillis();
        mSleepEndTime = mSleepStartTime + durationInMills;
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("TimerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onSleepFinished();
            }
        }, durationInMills);
    }

    private void onSleepFinished() {
        try {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                mHandlerThread = null;
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mListener != null) {
            mListener.onSleepFinished();
            mListener = null;
        }

    }

    public OnEventListener getListener() {
        return mListener;
    }

    public void setListener(OnEventListener mListener) {
        this.mListener = mListener;
    }

    public boolean isSleep() {
        if (System.currentTimeMillis() < mSleepEndTime) {
            return true;
        }
        return false;
    }

    public interface OnEventListener {

        void onSleepStart();
        void onSleepFinished();

    }
}
