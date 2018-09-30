package com.axecom.iweight.net;


import android.text.TextUtils;

import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.LocalSettingsBean;
import com.axecom.iweight.conf.Constants;
import com.axecom.iweight.ui.activity.LocalSettingsActivity;
import com.axecom.iweight.utils.SPUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2017-11-29.
 */

public class RetrofitFactory {
    private static RetrofitFactory mRetrofitFactory;
    private RequestInterface requestInterface;

    private RetrofitFactory(){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(InterceptorUtil.HeaderInterceptor())
                .addInterceptor(InterceptorUtil.LogInterceptor())
                .build();

        LocalSettingsBean.Value.ServerPort serverPort = (LocalSettingsBean.Value.ServerPort) SPUtils.readObject(SysApplication.mContext, LocalSettingsActivity.KEY_SVERVER_PORT);
        LocalSettingsBean.Value.ServerIp serverIp = (LocalSettingsBean.Value.ServerIp) SPUtils.readObject(SysApplication.mContext, LocalSettingsActivity.KEY_SERVER_IP);
        String url = "";
        String port = "";
        if(serverIp != null){
            url = serverIp.val;
            if(serverPort!=null && !TextUtils.isEmpty(serverPort.val)){
                port = serverPort.val;
                url =  url + ":" + port;
            }
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(!TextUtils.isEmpty(url) ? "http://"+url+"/" : Constants.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        requestInterface = retrofit.create(RequestInterface.class);
    }

    public static RetrofitFactory getInstance(){
        if(mRetrofitFactory == null){
            synchronized (RetrofitFactory.class){
                mRetrofitFactory = new RetrofitFactory();
            }
        }
        return mRetrofitFactory;
    }

    public RequestInterface API(){
        return requestInterface;
    }
}
