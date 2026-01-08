package com.genymobile.scrcpy.custom.controll;

public class SleepImpl {

    private static volatile SleepImpl singleton = null;
    private long mSleepStartTime;
    private long mSleepEndTime;

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
    }

    public boolean isSleep() {
        if (System.currentTimeMillis() < mSleepEndTime) {
            return true;
        }
        return false;
    }
}
