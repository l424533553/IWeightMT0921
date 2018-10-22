package com.axecom.iweight.ui.activity;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.Order;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.PayCheckManage;
import com.axecom.iweight.manager.SystemSettingManager;
import com.axecom.iweight.ui.view.SoftKey;
import com.axecom.iweight.utils.MoneyTextWatcher;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
    private SoftKey softKey;
    private String payId;
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
        priceTotalTv.setText(orderBean.getTotal_amount());
        priceRoundTv.setText(String.format("%.1f", Double.parseDouble(orderBean.getTotal_amount())));
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

            if(SystemSettingManager.disable_weixin_mode()){
                aliPayBtn.setVisibility(View.GONE);
            }else {
                aliPayBtn.setVisibility(View.VISIBLE);
            }
            if(SystemSettingManager.disable_alipay_mode()){
                wechatPayBtn.setVisibility(View.GONE);
            }else {
                wechatPayBtn.setVisibility(View.VISIBLE);
            }
//        showInfoToBanner(orderBean);
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
                payId = "1";
                setOrderBean(payId);
                break;
        }
    }

    public void setOrderBean(String payId) {
        orderBean.setPayment_id(payId);
        String orderNo = "AX" + getCurrentTime("yyyyMMddHHmmss") + AccountManager.getInstance().getScalesId();
        ModelAdapter<Order> modelAdapter = FlowManager.getModelAdapter(Order.class);
        for (int i = 0; i < orderBean.getGoods().size(); i++) {
            Order order = new Order();
            order.mac = orderBean.getMac();
            order.token = orderBean.getToken();
            order.create_time = orderBean.getCreate_time();
            order.create_time_day = getCurrentTime("yyyy-MM-dd");
            order.create_time_month = getCurrentTime("yyyy-MM");
            order.payment_id = orderBean.getPayment_id();
            order.pricing_model = orderBean.getPricing_model();
            order.order_no = orderNo;
            order.card_id = orderBean.getCard_id();
            order.total_amount = orderBean.getTotal_amount();
            order.total_number = orderBean.getTotal_number();
            order.total_weight = orderBean.getTotal_weight();

            SubOrderReqBean.Goods hotKeyBean = orderBean.getGoods().get(i);
            order.goods_id = hotKeyBean.getGoods_id();
            order.amount = hotKeyBean.getGoods_amount();
            order.goods_name = hotKeyBean.getGoods_name();
            order.goods_number = hotKeyBean.getGoods_number();
            order.goods_price = hotKeyBean.getGoods_price();
            order.goods_weight = hotKeyBean.getGoods_weight();
            modelAdapter.insert(order);
        }

        if (NetworkUtil.isConnected(this)) {
            if(mPayCheckManage!=null){
                mPayCheckManage.setCancelCheck(true);
                mPayCheckManage = null;
            }
            mPayCheckManage = new PayCheckManage(this, SysApplication.bannerActivity, qrCodeIv, orderBean, payId);
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
    protected void onPause() {
        super.onPause();
        if(mPayCheckManage!=null){
            mPayCheckManage.setCancelCheck(true);
            mPayCheckManage = null;
        }
    }
}
