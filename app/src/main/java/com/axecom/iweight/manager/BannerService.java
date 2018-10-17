package com.axecom.iweight.manager;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

import com.axecom.iweight.ui.activity.BannerActivity;

/**
 * Created by Administrator on 2018/7/25.
 */

public class BannerService extends Service {
    public BannerActivity banner = null;

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayManager displayManager = (DisplayManager)   getSystemService(Context.DISPLAY_SERVICE);
        //获取屏幕数量
        Display[] presentationDisplays = displayManager.getDisplays();
        if (presentationDisplays.length > 1) {
            banner = new BannerActivity(this.getApplicationContext(), presentationDisplays[1]);
            banner.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            banner.show();
        }
        if (banner != null){
            banner.show();
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Intent intent = new Intent("com.axecom.destroy");
        sendBroadcast(intent);
        super.onDestroy();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
