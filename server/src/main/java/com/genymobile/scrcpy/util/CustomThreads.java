package com.genymobile.scrcpy.util;

import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CustomThreads {
    public static final String TAG ="CustomThreads";
    private ExecutorService mExecutors = Executors.newCachedThreadPool();
    private static volatile CustomThreads sInstance;

    public static ExecutorService createSingleExecutors(String name) {
      return   Executors.newSingleThreadExecutor();
    }

    public static ExecutorService getExecutors() {
        return getInstance().mExecutors;
    }

    public static CustomThreads getInstance() {
        if (sInstance == null) {
            synchronized (CustomThreads.class) {
                if (sInstance == null) {
                    sInstance = new CustomThreads();
                }
            }
        }
        return sInstance;
    }



    public static void submit(Runnable runnable) {
         getInstance().toSubmit(runnable);
    }


    public static <T> Future<T> call(Callable<T> callable) {
        return getInstance().getExecutors().submit(callable);
    }

    public static boolean isMainThread() {
        try {
            return Looper.getMainLooper() == Looper.myLooper();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void toSubmit(Runnable runnable) {
         mExecutors.submit(runnable);
    }
//    public <T> FutureTask<T> create(Callable<T> callable) {
//       final FutureTask<T> futureTask=  new FutureTask<T>(callable){
//            @Override
//            protected void done() {
//                try {
//                    T result = futureTask.get();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                } catch (CancellationException e) {
//
//                }
//            }
//        };
//        mExecutors.execute(futureTask);
//
//    }



}
