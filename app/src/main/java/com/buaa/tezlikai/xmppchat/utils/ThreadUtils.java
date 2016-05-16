package com.buaa.tezlikai.xmppchat.utils;

import android.os.Handler;

/**
 * Created by Administrator on 2016/5/15.
 *
 */
public class ThreadUtils {
    /**
     * 子线程执行task
     */
    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    /**
     * 主线程里面的hander
     */

    public static Handler sHandler = new Handler();

    /**
     * UI线程执行task
     */
    public static void runInUIThread(Runnable task) {
        sHandler.post(task);
    }
}
