package com.genymobile.scrcpy.log;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.TimeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jackyli
 */
public class LogManager {

    private static final LogManager logManager = new LogManager();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ExecutorService singleService = Executors.newSingleThreadExecutor();

    /**
     * 缓存log信息
     * 默认存储七天日志，
     * 默认最早时间的顶替
     *
     * @param log 日志信息
     */
    public static String logDir = ScrcpyConfig.getLogDir();
    public static int pid = Process.myPid();
    public volatile boolean mIsUploading = false;
    /**
     * 当前的天
     */
    private int day;


    private LogManager() {

    }

    public static LogManager getLogManager() {
        return logManager;
    }
    public void uploadLogFile(String date, String hour) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                LogReqBean reqBean = new LogReqBean(date, hour);
                new LogUpload().startUploadLog(reqBean);
            }
        });
    }


    public void cacheLog(String tag,String log) {
        try {
            Thread thread = Thread.currentThread();
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(getTimeToStr());
            logBuilder.append("-");
            logBuilder.append(pid);
            logBuilder.append("-");
            logBuilder.append(thread.getId());
            logBuilder.append("-");
            logBuilder.append(tag);
            logBuilder.append(": ");
            logBuilder.append(log);
            log = logBuilder.toString();
            writeLog(log);
        } catch (Exception e) {
            Log.i("LogManager", "cacheLog exception:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeLog(String log) {
        try {
            String logFolder = logDir + File.separator + getTimeToLogDir();
            FileUtils.createDirs(logFolder);
            String logFileName = getTimeToLogName() + ".log";
            String filePath = logFolder + File.separator + logFileName;
            String finalLog = log;
            singleService.submit(() -> {
                try {
                    FileUtils.writeFile(filePath, finalLog);
                } catch (Exception e) {
                    Log.i("LogMananger", "writeException==" + e.getCause());
                }
            });
        } catch (Exception e) {
            Log.i("LogManager", "cacheLog exception:" + e.getCause());
        }
    }


    private String getMethodName(Thread thread,int stackLevel) {
        try {
            String methodName = thread.getStackTrace()[stackLevel].getMethodName();
            return methodName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    private int getFileLine(Thread thread,int stackLevel) {
        try {
            int line = thread.getStackTrace()[stackLevel].getLineNumber();
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getTimeToStr() {
        Date d = new Date(TimeUtils.getTimeStamp());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINA);
        return sdf.format(d);
    }
    public static String getTimeToLogDir() {
        Date d = new Date(TimeUtils.getTimeStamp());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        return sdf.format(d);
    }
    public static String getTimeToLogName() {
        Date d = new Date(TimeUtils.getTimeStamp());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH", Locale.CHINA);
        return sdf.format(d);
    }

}
