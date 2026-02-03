package com.genymobile.scrcpy.util;

import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static long getTimeStamp() {
        return System.currentTimeMillis();
    }
    public static String formatTime(long time) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_FORMAT);
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(new Date(time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public static String formatTime(String format,long time) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(new Date(time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
