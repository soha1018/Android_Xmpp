package com.itsoha.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    /**
     * 可以在子线程中弹出
     */
    public static void showToastSafe(final Context context, final String text) {
        ThreadUtils.runInUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
