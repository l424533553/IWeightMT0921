package com.axecom.iweight.manager;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.PayNoticeBean;
import com.axecom.iweight.bean.SubOrderBean;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.activity.BannerActivity;
import com.axecom.iweight.ui.activity.UseCashActivity;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.axecom.iweight.utils.LogUtils;
import com.axecom.iweight.utils.SPUtils;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/10/12.
 */

public class PayCheckManage {

    private final BannerActivity banner;
    private final ImageView qrCodeIv;
    private final SubOrderReqBean orderBean;
    private final String payId;
    BaseActivity content;
    private boolean cancelCheck;
    private int mRunCount = 20;

    public PayCheckManage(BaseActivity content, BannerActivity banner, ImageView qrCodeIv, SubOrderReqBean orderBean, String payId) {
        this.banner = banner;
        this.content = content;
        this.qrCodeIv = qrCodeIv;
        this.orderBean = orderBean;
        this.payId = payId;
    }

    private int requestCount;


    public void submitOrder() {
        RetrofitFactory.getInstance().API()
                .submitOrder(orderBean)
                .compose(this.<BaseEntity<SubOrderBean>>setThread())
                .subscribe(new Observer<BaseEntity<SubOrderBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        content.showLoading();
                    }

                    @Override
                    public void onNext(final BaseEntity<SubOrderBean> subOrderBeanBaseEntity) {
                        if (subOrderBeanBaseEntity.isSuccess()) {
                            SubOrderBean data = subOrderBeanBaseEntity.getData();
                            if (qrCodeIv != null) {
                                content.closeLoading();
                                Glide.with(content)
                                        .load(data.getCode_img_url())
                                        .into(qrCodeIv);
                                Glide.with(content)
                                        .load(data.getCode_img_url())
                                        .into(banner.bannerQRCode);
                            }
                            String payMethod = "";
                            switch (payId) {
                                case "1":
                                    payMethod = "支付方式：微信支付";
                                    break;
                                case "2":
                                    payMethod ="支付方式：支付宝支付";
                                    break;
                                case "4":
                                    payMethod ="支付方式：现金支付";
                                    break;
                            }
                            SPUtils.putString(SysApplication.getContext(), "print_bitmap", data.getPrint_code_img());
                            banner.showPayAmount(orderBean.getTotal_amount(),payMethod);
                            getPayNotice(data.getOrder_no(), data.getPrint_code_img(), true);
                        } else {
                            content.showLoading(subOrderBeanBaseEntity.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        content.closeLoading();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
//                        content.closeLoading();
                    }
                });
    }


    public void getPayNotice(final String order_no, final String qrCode, boolean first) {
        if (first) requestCount = 0;
        requestCount++;
        if (cancelCheck || requestCount >= mRunCount) {
            if (requestCount == mRunCount && content instanceof UseCashActivity) {
                UIUtils.postTaskSafely(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(content, "支付超时", Toast.LENGTH_SHORT).show();
                        content.finish();
                    }
                });
            }
            return;
        }
        RetrofitFactory.getInstance().API()
                .getPayNotice(order_no)
                .compose(this.<BaseEntity<PayNoticeBean>>setThread())
                .subscribe(new Observer<BaseEntity<PayNoticeBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(final BaseEntity<PayNoticeBean> payNoticeBeanBaseEntity) {
                        if (payNoticeBeanBaseEntity.isSuccess()) {
                            if (payNoticeBeanBaseEntity.getData().flag == 0) {
                                EventBus.getDefault().post(new BusEvent(BusEvent.PRINTER_LABEL, qrCode, order_no, payId, qrCode));
                                if (content instanceof UseCashActivity){
                                    content.finish();
                                    banner.showPayAmount(orderBean.getTotal_amount(),"");
                                    banner.showSelectedGoodsResult(orderBean.getGoods());
                                }
                            } else {
//                              轮循获取支付结果
                                Timer timer = new Timer();//实例化Timer类
                                timer.schedule(new TimerTask() {
                                    public void run() {
                                        getPayNotice(order_no, qrCode, false);
                                        this.cancel();
                                    }
                                }, 3000);//五百毫秒
                            }
                            LogUtils.d(payNoticeBeanBaseEntity.getData().msg);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void setCancelCheck(boolean cancelCheck) {
        this.cancelCheck = cancelCheck;
    }

    public <T> ObservableTransformer<T, T> setThread() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
