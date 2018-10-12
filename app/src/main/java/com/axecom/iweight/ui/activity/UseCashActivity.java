package com.axecom.iweight.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.manager.PayCheckManage;
import com.axecom.iweight.ui.uiutils.ImageLoaderHelper;
import com.axecom.iweight.ui.view.SoftKey;
import com.axecom.iweight.utils.MoneyTextWatcher;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2018-5-15.
 */

public class UseCashActivity extends BaseActivity implements View.OnClickListener {

    private View rootView;
    private Button confirmBtn;
    private Button cancelBtn;
    private Button cashPayBtn;
    private Button aliPayBtn;
    private Button wechatPayBtn;
    private EditText cashEt;
    private TextView priceTotalTv;
    private TextView priceRoundTv;
    private TextView priceChangeTv;
    private SubOrderReqBean orderBean;
    private LinearLayout cashPayLayout;
    private ImageView qrCodeIv;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Intent intent;
    private Bundle bundle;
    private SoftKey softKey;
    private String payId;
    private String bitmap;
    private boolean flag = true;
    public BannerActivity banner = null;
    private PayCheckManage mPayCheckManage;

    @Override
    public View setInitView() {
        rootView = LayoutInflater.from(this).inflate(R.layout.cash_dialog_layout, null);
        confirmBtn = rootView.findViewById(R.id.cash_dialog_confirm_btn);
        cancelBtn = rootView.findViewById(R.id.cash_dialog_cancel_btn);
        cashPayBtn = rootView.findViewById(R.id.cash_dialog_cash_pay_btn);
        aliPayBtn = rootView.findViewById(R.id.cash_dialog_alipay_btn);
        wechatPayBtn = rootView.findViewById(R.id.cash_dialog_wechat_pay_btn);
        cashPayLayout = rootView.findViewById(R.id.cash_dialog_cash_pay_layout);
        qrCodeIv = rootView.findViewById(R.id.cash_dialog_qr_code_iv);
        cashEt = rootView.findViewById(R.id.cash_dialog_cash_et);
        priceTotalTv = rootView.findViewById(R.id.use_cash_price_total_tv);
        priceRoundTv = rootView.findViewById(R.id.use_cash_price_round_tv);
        priceChangeTv = rootView.findViewById(R.id.use_cash_change_tv);
        softKey = rootView.findViewById(R.id.cash_dialog_softkey);
        disableShowInput(cashEt);
        cashEt.requestFocus();
        cashEt.addTextChangedListener(new MoneyTextWatcher(cashEt));
        orderBean = (SubOrderReqBean) getIntent().getExtras().getSerializable("orderBean");
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] presentationDisplays = displayManager.getDisplays();
        if (presentationDisplays.length > 1) {
            if (banner == null) {
                banner = new BannerActivity(this.getApplicationContext(), presentationDisplays[1]);
            }

            Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            banner.show();
        }

        priceTotalTv.setText(orderBean.getTotal_amount());
        priceRoundTv.setText(String.format("%.1f", Double.parseDouble(orderBean.getTotal_amount())));

        imageLoader = ImageLoader.getInstance();
        options = ImageLoaderHelper.getInstance(this).getDisplayOptions();
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        cashPayBtn.setOnClickListener(this);
        aliPayBtn.setOnClickListener(this);
        wechatPayBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void initView() {
        LinkedHashMap valueMap = (LinkedHashMap) SPUtils.readObject(this, SystemSettingsActivity.KEY_STOP_CASH);
        if (NetworkUtil.isConnected(this)) {
            aliPayBtn.setEnabled(true);
            wechatPayBtn.setEnabled(true);
        } else {
            aliPayBtn.setEnabled(false);
            wechatPayBtn.setEnabled(false);
        }
        softKey.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getAdapter().getItem(position).toString();
                setEditText(cashEt, position, text);
                String txt = cashEt.getText().toString();
                if (!TextUtils.isEmpty(cashEt.getText())) {
                    priceChangeTv.setText(String.format("%.1f", Float.parseFloat(cashEt.getText().toString()) - Float.parseFloat(priceRoundTv.getText().toString())));
                } else {
                    priceChangeTv.setText("");
                }

            }
        });
        showInfoToBanner(orderBean);
    }

    public void showInfoToBanner(SubOrderReqBean bean) {
        banner.bannerOrderLayout.setVisibility(View.VISIBLE);
        banner.bannerTotalPriceTv.
                setText(getString(R.string.string_amount_txt3, Float.parseFloat(bean.getTotal_amount())));
        banner.goodsList.clear();
        banner.goodsList.addAll(bean.getGoods());
        banner.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(wechatPayBtn.getVisibility()==View.VISIBLE){
            wechatPayBtn.callOnClick();
        }else if(wechatPayBtn.getVisibility()==View.GONE&&aliPayBtn.getVisibility()==View.VISIBLE){
            aliPayBtn.callOnClick();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag = false;
        banner.bannerOrderLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cash_dialog_confirm_btn:
                payId = "4";
                setOrderBean(payId);
                break;
            case R.id.cash_dialog_cancel_btn:
                finish();
                break;
            case R.id.cash_dialog_cash_pay_btn:
                cashPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_green_bg2));
                cashPayBtn.setTextColor(this.getResources().getColor(R.color.white));
                aliPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                aliPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                wechatPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                wechatPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                cashPayLayout.setVisibility(View.VISIBLE);
                qrCodeIv.setVisibility(View.GONE);
                banner.bannerOrderLayout.setVisibility(View.VISIBLE);
                banner.bannerTotalPriceTv.setText(getString(R.string.string_amount_txt3, Float.parseFloat(orderBean.getTotal_amount())));
                banner.tvPayWay.setText("支付方式：现金支付");
                banner.bannerQRCode.setImageDrawable(this.getResources().getDrawable(R.drawable.logo));
                banner.goodsList.clear();
                banner.goodsList.addAll(orderBean.getGoods());
                banner.adapter.notifyDataSetChanged();
                break;
            case R.id.cash_dialog_alipay_btn:
                cashPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                cashPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                aliPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_green_bg2));
                aliPayBtn.setTextColor(this.getResources().getColor(R.color.white));
                wechatPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                wechatPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                cashPayLayout.setVisibility(View.GONE);
                qrCodeIv.setVisibility(View.VISIBLE);
                banner.tvPayWay.setText("支付方式：支付宝支付");
                payId = "2";
                setOrderBean(payId);
                break;
            case R.id.cash_dialog_wechat_pay_btn:
                cashPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                cashPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                aliPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_white_btn_bg));
                aliPayBtn.setTextColor(this.getResources().getColor(R.color.black));
                wechatPayBtn.setBackground(this.getResources().getDrawable(R.drawable.shape_green_bg2));
                wechatPayBtn.setTextColor(this.getResources().getColor(R.color.white));
                cashPayLayout.setVisibility(View.GONE);
                qrCodeIv.setVisibility(View.VISIBLE);
                banner.tvPayWay.setText("支付方式：微信支付");
                payId = "1";
                setOrderBean(payId);
                break;
        }
    }

    public void setOrderBean(String payId) {
        orderBean.setPayment_id(payId);
        if (NetworkUtil.isConnected(this)) {
            if(mPayCheckManage!=null){
                mPayCheckManage.setCancelCheck(true);
                mPayCheckManage = null;
            }
            mPayCheckManage = new PayCheckManage(this, banner, qrCodeIv, orderBean, payId);
            mPayCheckManage.submitOrder();
        } else {
            List<SubOrderReqBean> orders = (List<SubOrderReqBean>) SPUtils.readObject(this, "local_order");
            if (orders != null) {
                orders.add(orderBean);
                SPUtils.saveObject(this, "local_order", orders);
            } else {
                List<SubOrderReqBean> localOrder = new ArrayList<>();
                localOrder.add(orderBean);
                SPUtils.saveObject(this, "local_order", localOrder);
            }
            EventBus.getDefault().post(new BusEvent(BusEvent.PRINTER_NO_BITMAP, "", payId, ""));
            finish();
        }
    }

    @Override
    public void onEventMainThread(BusEvent event) {
        super.onEventMainThread(event);
        if (event != null) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPayCheckManage!=null){
            mPayCheckManage.setCancelCheck(true);
            mPayCheckManage = null;
        }

    }
}
