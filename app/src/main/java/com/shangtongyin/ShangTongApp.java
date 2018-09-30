package com.shangtongyin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.axecom.iweight.my.entity.LogBean;
import com.luofx.base.MyBaseApplication;
import com.luofx.entity.dao.BaseDao;
import com.luofx.utils.PreferenceUtils;
import com.shangtongyin.tools.serialport.Print;
import com.shangtongyin.tools.serialport.USB_Print;

/**
 * Fun：
 * Author：Linus_Xie
 * Date：2018/8/2 14:55
 */
public class ShangTongApp extends MyBaseApplication {

    /**
     * 0:默认值没有选着打印机    1"佳博2120TF", 2 "美团打印机", 3 "商通打印机", 4 "香山打印机
     */
    public String TYPE_ERROR = "error";
    public String TYPE_INFO = "info";
    private int printIndex;

    public int getPrintIndex() {
//        return printIndex;
        return 3;
    }

    public void setPrintIndex(int printIndex) {
        this.printIndex = printIndex;
    }

    private Print print;

    public Print getPrint() {
        return print;
    }


    public ShangTongApp() {
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    protected BaseDao<LogBean> baseDao;

    public BaseDao<LogBean> getBaseDao() {
        return baseDao;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        baseDao = new BaseDao<>(this, LogBean.class);
        print = new Print();
        print.open();
//        registerBroadcast();
        USB_Print.initPrinter(this);
        SharedPreferences sharedPreferences = PreferenceUtils.getSp(this);
        this.printIndex = sharedPreferences.getInt("printIndex", 0);
    }

    /**
     * 最终关闭串口
     */
    public void onDestory() {
        if (print != null) {
            print.closeSerialPort();
        }
    }
}
