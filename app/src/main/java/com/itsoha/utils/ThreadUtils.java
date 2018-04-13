package com.itsoha.utils;


import android.os.Handler;
import android.support.annotation.NonNull;

import com.itsoha.activity.LoginActivity;

import java.util.concurrent.ThreadFactory;

public class ThreadUtils {

    /**
     * 主线程的handler
     */
    public static Handler mHandler = new Handler();


    /**
     * 子线程运行的task
     */
    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    /**
     * Ui运行的task
     */
    public static void runInUiThread(Runnable task) {
        mHandler.post(task);
    }

}
