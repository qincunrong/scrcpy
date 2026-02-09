package com.genymobile.scrcpy.util;

import android.util.Log;

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
        if (sLogLevel <= LOG_DEBUG){
            Log.v(tag, msg);
            saveLog(tag, msg);
        }

    }

    public static void d(String tag, String msg) {
        if (sLogLevel <= LOG_DEBUG){
            Log.d(tag, msg);
            saveLog(tag, msg);
        }

    }

    public static void i(String tag, String msg) {
        if (sLogLevel <= LOG_INFO){
            Log.i(tag, msg);
            saveLog(tag, msg);
        }
    }
    public static void debug(String tag, String msg,Object... params) {
        if (sLogLevel <= LOG_INFO){
            String fullMsg = String.format(msg, params);
            Log.v(tag, fullMsg);
            saveLog(tag, fullMsg);
        }
    }
    public static void debug(String tag, String msg) {
        if (sLogLevel <= LOG_INFO){
            Log.v(tag, msg);
            saveLog(tag, msg);
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
        if (sLogLevel <= LOG_INFO){
            String fullMsg = String.format(msg, params);
            Log.i(tag, fullMsg);
            saveLog(tag, fullMsg);
        }

    }

    public static void w(String tag, String msg) {
        if (sLogLevel <= LOG_WARN){
            Log.w(tag, msg);
            saveLog(tag, msg);
        }

    }
    public static void w(String tag, String msg,Object... params) {
        if (sLogLevel <= LOG_INFO){
            String fullMsg = String.format(msg, params);
            Log.w(tag, fullMsg);
            saveLog(tag, fullMsg);
        }

    }

    public static void e(String tag, String msg) {
        if (sLogLevel <= LOG_ERROR){
            Log.e(tag, msg);
            saveLog(tag, msg);
        }

    }
    public static void e(String tag, String msg,Object... params) {
        if (sLogLevel <= LOG_INFO){
            String fullMsg = String.format(msg, params);
            Log.e(tag, fullMsg);
            saveLog(tag, fullMsg);
        }

    }

    private static void saveLog(String tag, String msg) {
        try {
            LogManager.getLogManager().cacheLog(tag, msg);
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
