package com.axecom.iweight.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.bean.Order;
import com.axecom.iweight.bean.SubOrderBean;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.ui.activity.MainActivity;
import com.axecom.iweight.ui.activity.UseCashActivity;
import com.axecom.iweight.utils.LogUtils;
import com.axecom.iweight.utils.SPUtils;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/7/19.
 */

public class NetWorkReceiver extends BroadcastReceiver {

    private static final String ACTION_NET_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_NET_CHANGE)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                EventBus.getDefault().post(new BusEvent(BusEvent.NET_WORK_AVAILABLE, true));
            } else {
            }
        }
    }

}
