package com.genymobile.scrcpy.util;

import android.util.Log;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.log.LogManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


public class Logger {


    private static final int LOG_VERBOSE = 1;
    private static final int LOG_DEBUG = 2;
    private static final int LOG_INFO = 3;
    private static final int LOG_WARN = 4;
    private static final int LOG_ERROR = 5;
    private static int sLogLevel = LOG_VERBOSE;
    private static boolean sIsSaveLog = true;

    public static void setLogLevel(int level) {
        sLogLevel = level;
    }


    public static void v(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_DEBUG){
                tag=appendTag(tag);
                Log.v(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void d(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_DEBUG){
                tag=appendTag(tag);
                Log.d(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void i(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                Log.i(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void debug(String tag, String msg,Object... params) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                String fullMsg = String.format(msg, params);
                Log.v(tag, fullMsg);
                saveLog(tag, fullMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void debug(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                Log.v(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void i(Object obj, String msg) {
        String tag = obj.getClass().getSimpleName();
        i(tag,msg,"");

    }
    public static void i(Object obj, String msg,Object... params) {
        String tag = obj.getClass().getSimpleName();
        i(tag, msg, params);

    }
    public static void i(String tag, String msg,Object... params) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                String fullMsg = String.format(msg, params);
                Log.i(tag, fullMsg);
                saveLog(tag, fullMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void w(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_WARN){
                tag=appendTag(tag);
                Log.w(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void w(String tag, String msg,Object... params) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                String fullMsg = String.format(msg, params);
                Log.w(tag, fullMsg);
                saveLog(tag, fullMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void e(String tag, String msg) {
        try {
            if (sLogLevel <= LOG_ERROR){
                tag=appendTag(tag);
                Log.e(tag, msg);
                saveLog(tag, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void e(String tag, String msg,Object... params) {
        try {
            if (sLogLevel <= LOG_INFO){
                tag=appendTag(tag);
                String fullMsg = String.format(msg, params);
                Log.e(tag, fullMsg);
                saveLog(tag, fullMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String appendTag(String tag) {
        return ScrcpyConfig.getLogGroup() + tag;
    }

    private static void saveLog(String tag, String msg) {
        try {
            if (sIsSaveLog) {
                LogManager.getLogManager().cacheLog(tag, msg);
            }
        } catch (Exception e) {

        }
    }
    public static String printThrowable(String tag, Throwable ex) {
        return printThrowable(tag, "", ex);
    }

    public static String printThrowable(String tag, String msg,Throwable ex) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("exception:"+msg);
            sb.append(", msg:" + ex.getMessage());
            sb.append(", stack==" + ex.getMessage());
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            i(tag, sb.toString());
        } catch (Exception e) {
        }
        return null;
    }
}
