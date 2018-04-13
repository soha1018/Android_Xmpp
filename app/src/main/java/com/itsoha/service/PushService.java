package com.itsoha.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.itsoha.R;
import com.itsoha.utils.ThreadUtils;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * 监听服务器发送的消息
 */
public class PushService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        IMService.xmppConnection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message msg = (Message) packet;
                final String body = msg.getBody();

       /*         WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                layoutParams.format = PixelFormat.TRANSLUCENT;
                layoutParams.setTitle("Toast");
                layoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                layoutParams.gravity = Gravity.CENTER;
                TextView view = new TextView(getApplicationContext());
                view.setText(body);
                wm.addView(view, layoutParams);*/

                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        builder.setPositiveButton("关闭", null);
                        AlertDialog dialog = builder.create();
                        dialog.setIcon(R.drawable.ic_launcher_foreground);
                        dialog.setTitle("系统消息！");
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                        dialog.setMessage(body);
                        dialog.show();
                    }
                });


            }
        }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
