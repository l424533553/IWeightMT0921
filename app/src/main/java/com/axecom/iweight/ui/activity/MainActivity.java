package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.Advertis;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.HotKeyBean_Table;
import com.axecom.iweight.bean.LoginInfo;
import com.axecom.iweight.bean.PayNoticeBean;
import com.axecom.iweight.bean.SaveGoodsReqBean;
import com.axecom.iweight.bean.ScalesCategoryGoods;
import com.axecom.iweight.bean.SubOrderBean;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.MacManager;
import com.axecom.iweight.manager.PayCheckManage;
import com.axecom.iweight.manager.SystemSettingManager;
import com.axecom.iweight.manager.ThreadPool;
import com.axecom.iweight.my.adapter.CommodityAdapter;
import com.axecom.iweight.my.adapter.DigitalAdapter;
import com.axecom.iweight.my.entity.ItemsBean;
import com.axecom.iweight.my.entity.OrderInfo;
import com.axecom.iweight.my.helper.HeartBeatServcice;
import com.axecom.iweight.my.helper.HttpHelper;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.adapter.GoodMenuAdapter;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.axecom.iweight.utils.ButtonUtils;
import com.axecom.iweight.utils.FileUtils;
import com.axecom.iweight.utils.LogUtils;
import com.axecom.iweight.utils.MoneyTextWatcher;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.luofx.listener.VolleyListener;
import com.luofx.listener.VolleyStringListener;
import com.luofx.utils.PreferenceUtils;
import com.luofx.utils.log.MyLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.shangtongyin.tools.serialport.Print;
import com.shangtongyin.tools.serialport.WeightHelper;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.axecom.iweight.utils.CommonUtils.parseFloat;
import static com.shangtongyin.tools.serialport.IConstants_ST.MARKET_ID;
import static com.shangtongyin.tools.serialport.IConstants_ST.NOTIFY_WEIGHT;
import static com.shangtongyin.tools.serialport.IConstants_ST.SELLER;
import static com.shangtongyin.tools.serialport.IConstants_ST.SELLER_ID;
import static com.shangtongyin.tools.serialport.IConstants_ST.TID;

public class MainActivity extends BaseActivity implements VolleyListener, VolleyStringListener {

    private View rootView;
    private GridView commoditysGridView;
    private GridView digitalGridView;
    private ListView commoditysListView;
    private CommodityAdapter commodityAdapter;
    private GoodMenuAdapter goodMenuAdapter;

    private LinearLayout weightLayout;
    private LinearLayout countLayout;
    private EditText countEt;
    private EditText etPrice;
    private TextView commodityNameTv;
    private TextView tvgrandTotal;
    private TextView weightTotalTv;
    private TextView weightTv;
    private TextView weightNumberTv;
    private TextView priceTotalTv;
    private TextView operatorTv;
    private TextView stallNumberTv;
    private TextView componyTitleTv;
    private TextView weightTopTv;
    private List<HotKeyBean> HotKeyBeanList;
    private List<HotKeyBean> seledtedGoodsList;
    private HotKeyBean selectedGoods;
    private int mTotalCopies = 1;
    private String bitmap;


    public BannerActivity banner = null;
    boolean switchSimpleOrComplex;
    boolean stopPrint;

    /************************************************************************************/
    private SysApplication application;
    private ThreadPool threadPool;  //线程池 管理
    private boolean flag = true;
    private TextView loginTypeName;
    private boolean mNotPushRemote;

    @SuppressLint("InflateParams")
    @Override
    public View setInitView() {
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        application = (SysApplication) getApplication();
        commoditysGridView = rootView.findViewById(R.id.gvGoodMenu);
        digitalGridView = rootView.findViewById(R.id.main_digital_keys_grid_view);
        commoditysListView = rootView.findViewById(R.id.main_commoditys_list_view);
        weightLayout = rootView.findViewById(R.id.main_weight_layout);
        countLayout = rootView.findViewById(R.id.main_count_layout);
        countEt = rootView.findViewById(R.id.main_count_et);
        commodityNameTv = rootView.findViewById(R.id.main_commodity_name_tv);
        tvgrandTotal = rootView.findViewById(R.id.main_grandtotal_tv);
        weightTotalTv = rootView.findViewById(R.id.main_weight_total_tv);
//        weightTotalMsgTv = rootView.findViewById(R.id.main_weight_total_msg_tv);
        weightTv = rootView.findViewById(R.id.main_weight_tv);
        operatorTv = rootView.findViewById(R.id.main_operator_tv);
        loginTypeName = rootView.findViewById(R.id.tv_login_type_name);
        stallNumberTv = rootView.findViewById(R.id.main_stall_number_tv);
        weightNumberTv = rootView.findViewById(R.id.main_weight_number_tv);
        componyTitleTv = rootView.findViewById(R.id.main_compony_title_tv);
        priceTotalTv = rootView.findViewById(R.id.main_price_total_tv);
        weightTopTv = rootView.findViewById(R.id.main_weight_top_tv);
//        weightTopMsgTv = rootView.findViewById(R.id.main_weight_msg_tv);
        rootView.findViewById(R.id.main_cash_btn).setOnClickListener(this);
        rootView.findViewById(R.id.main_settings_btn).setOnClickListener(this);
        rootView.findViewById(R.id.main_clear_btn).setOnClickListener(this);
        rootView.findViewById(R.id.main_digital_clear_btn).setOnClickListener(this);
        rootView.findViewById(R.id.main_digital_add_btn).setOnClickListener(this);
        rootView.findViewById(R.id.main_scan_pay).setOnClickListener(this);
        etPrice = rootView.findViewById(R.id.main_commodity_price_et);
        etPrice.requestFocus();
        etPrice.addTextChangedListener(new MoneyTextWatcher(etPrice));
        countEt.addTextChangedListener(countTextWatcher);

        disableShowInput(etPrice);
        disableShowInput(countEt);
        getLoginInfo();
        HotKeyBeanList = new ArrayList<>();
        seledtedGoodsList = new ArrayList<>();
        goodMenuAdapter = new GoodMenuAdapter(this, HotKeyBeanList);

        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        assert displayManager != null;
        Display[] presentationDisplays = displayManager.getDisplays();
//        LogUtils.d("------------: " + presentationDisplays.length + "  --- " + presentationDisplays[1].getName());
        if (presentationDisplays.length > 1) {
            banner = new BannerActivity(this.getApplicationContext(), presentationDisplays[1]);
            Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            banner.show();
        }

        advertising();

        weightTopTv.setOnClickListener(this);
        SystemSettingManager.getSettingData(this);
        return rootView;
    }

    private Handler handler;

    /**
     * 获取称重重量信息
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case NOTIFY_WEIGHT:
                        String weight = (String) msg.obj;
                        weightTv.setText(weight);
                        weightTopTv.setText(weight);
                        setGrandTotalTxt();
                        break;
                }
                return false;
            }
        });
    }


    /*    *//**
     * 清除  按键
     *//*
    private void eliminate() {
        tvgrandTotal.setHint("0");
        tvgrandTotal.setText("0.00");
    }*/

    /**
     * 初始化结算列表
     */
    private void initSettlement() {
        commodityAdapter = new CommodityAdapter(this, seledtedGoodsList);
        commoditysListView.setAdapter(commodityAdapter);
        commoditysListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                seledtedGoodsList.remove(position);
                commodityAdapter.notifyDataSetChanged();
                calculatePrice();
                return true;
            }
        });
    }

    private void initDigital() {

        DigitalAdapter digitalAdapter = new DigitalAdapter(this, null);
        digitalGridView.setAdapter(digitalAdapter);
        digitalGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedGoods == null) {
                    return;
                }

                String text = parent.getAdapter().getItem(position).toString();
                switch (rootView.findFocus().getId()) {
                    case R.id.main_count_et:
                        setEditText(countEt, position, text);
                        break;
                    case R.id.main_commodity_price_et:
//                        if(priceEt.requestFocus()){
//                            priceEt.setText("");
//                        }
                        setEditText(etPrice, position, text, 0);


                        break;
                }
                setGrandTotalTxt();
            }
        });
    }


    private Context context;

    @Override
    public void initView() {
        context = this;
        weightNumberTv.setText(AccountManager.getInstance().getScalesId());
        initSettlement();
        commoditysGridView.setAdapter(goodMenuAdapter);

       /* List<HotKeyBean> hotKeyBeanList = SQLite.select().from(HotKeyBean.class).queryList();
        if (hotKeyBeanList.size() > 0) {
            HotKeyBeanList.addAll(hotKeyBeanList);
            goodMenuAdapter.notifyDataSetChanged();
        } else {*/
        getGoodsData();
//        }

        commoditysGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedGoods = (HotKeyBean) parent.getAdapter().getItem(position);
                commodityNameTv.setText(selectedGoods.name);
                etPrice.setText("");
                etPrice.setHint(selectedGoods.price);

                float count = 0;
                if (!TextUtils.isEmpty(countEt.getText())) {
                    count = parseFloat(countEt.getText().toString());
                } else if (!TextUtils.isEmpty(countEt.getHint())) {
                    count = parseFloat(countEt.getHint().toString());
                }
                if (switchSimpleOrComplex) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.price) * count));
                } else {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.price) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(selectedGoods.price) * parseFloat(weightTopTv.getText().toString())));
                    }
                }

                goodMenuAdapter.setCheckedAtPosition(position);
                goodMenuAdapter.notifyDataSetChanged();
            }
        });


        //初始化 键盘 设置
        initDigital();
        initHandler();
        weighUtils = new WeightHelper(handler);
        weighUtils.open();

        initHeartBeat();
    }

    // 商通的称重  工具类
    private WeightHelper weighUtils;
    private HeartBeatServcice.MyBinder myBinder;
    private HeartBeatServcice heartBeatServcice;
    private ServiceConnection mConnection;
    private int tid = -1;  //秤的编号
    private int marketId = -1;  // 市场id
    private String seller;  //售卖人
    private int sellerid;  // 售卖人id

    /**
     * 初始化心跳
     */
    private void initHeartBeat() {
        tid = PreferenceUtils.getInt(context, TID, -1);
        marketId = PreferenceUtils.getInt(context, MARKET_ID, -1);
        sellerid = PreferenceUtils.getInt(context, SELLER_ID, -1);
        seller = PreferenceUtils.getString(context, SELLER, null);
        if (tid > 0 && marketId > 0) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    myBinder = (HeartBeatServcice.MyBinder) service;
                    heartBeatServcice = myBinder.getService();
                    heartBeatServcice.setMarketid(marketId);
                    heartBeatServcice.setTerid(tid);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("MainActivity", "onServiceDisconnected");
                }
            };
            Intent serviceIntent = new Intent(this, HeartBeatServcice.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        } else {
            //TODO  未获取到秤的市场id 和秤编号 ，需要重新登陆
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        LinkedHashMap valueMap = (LinkedHashMap) SPUtils.readObject(this, SystemSettingsActivity.KEY_STOP_PRINT);
        if (valueMap != null) {
            stopPrint = (boolean) valueMap.get("val");
        }
        switchSimpleOrComplex = (boolean) SPUtils.get(this, SettingsActivity.KET_SWITCH_SIMPLE_OR_COMPLEX, false);
        if (switchSimpleOrComplex) {
            countLayout.setVisibility(View.VISIBLE);
            weightLayout.setVisibility(View.GONE);
        } else {
            countEt.setText("0");
            countLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.VISIBLE);
        }
        mTotalCopies = (int) SPUtils.get(this, LocalSettingsActivity.KEY_PRINTER_COUNT, mTotalCopies);
    }


    private TextWatcher countTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    countEt.setText(s.subSequence(1, 2));
                    countEt.setSelection(1);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            String temp = s.toString();
            int posDot = temp.indexOf(".");
            //小数点之前保留3位数字或者一千
            if (posDot <= 0) {
                //temp
                if (temp.equals("10000")) {
                    return;
                } else {
                    if (temp.length() <= 4) {
                        return;
                    } else {
                        s.delete(4, 5);
                        return;
                    }
                }
            }
            //保留三位小数
            if (temp.length() - posDot - 1 > 1) {
                s.delete(posDot + 2, posDot + 3);
            }
        }
    };

    public void advertising() {
        RetrofitFactory.getInstance().API()
                .advertising()
                .compose(this.<BaseEntity<Advertis>>setThread())
                .subscribe(new Observer<BaseEntity<Advertis>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseEntity<Advertis> advertisBaseEntity) {
                        if (advertisBaseEntity.isSuccess()) {
                            final Advertis advertis = advertisBaseEntity.getData();
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < advertis.list.size(); i++) {
                                list.add(advertis.list.get(i).img);
                            }
                            banner.convenientBanner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
                                @Override
                                public NetworkImageHolderView createHolder() {
                                    return new NetworkImageHolderView();
                                }
                            }, list).startTurning(2000);
//                            banner.showAutoCancel(2000);
                            banner.show();
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

    @SuppressLint("DefaultLocale")
    public void setGrandTotalTxt() {
        try {
            float count = 0;
            if (!TextUtils.isEmpty(countEt.getText())) {
                count = parseFloat(countEt.getText().toString());
            } else if (!TextUtils.isEmpty(countEt.getHint())) {
                count = parseFloat(countEt.getHint().toString());
            }
            if (switchSimpleOrComplex) {
                if (!TextUtils.isEmpty(etPrice.getText())) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * count));
                } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                    tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * count));
                }

            } else {
                if (!TextUtils.isEmpty(etPrice.getText())) {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getText().toString()) * parseFloat(weightTopTv.getText().toString())));
                    }
                } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                    if (weightTopTv.getText().toString().indexOf('.') == -1 || weightTopTv.getText().length() - (weightTopTv.getText().toString().indexOf(".") + 1) <= 1) {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * parseFloat(weightTopTv.getText().toString()) / 1000));
                    } else {
                        tvgrandTotal.setText(String.format("%.2f", parseFloat(etPrice.getHint().toString()) * parseFloat(weightTopTv.getText().toString())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        weighUtils.closeSerialPort();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            startDDMActivity(SettingsActivity.class, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_cash_btn:
                if (SystemSettingManager.disable_cash_mode()) {
                    Toast.makeText(this, "已停用现金模式", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!ButtonUtils.isFastDoubleClick(R.id.main_cash_btn)) {
                    //结算时带上当前称重的记录
                    appendCurrentGood();
                    if (parseFloat(priceTotalTv.getText().toString()) > 0 || parseFloat(tvgrandTotal.getText().toString()) > 0) {
                        showDialog(v, true);
                    }
                }
                break;
            case R.id.main_scan_pay:
                if (!ButtonUtils.isFastDoubleClick(R.id.main_scan_pay)) {
                    //结算时带上当前称重的记录
                    appendCurrentGood();
                    if (parseFloat(priceTotalTv.getText().toString()) > 0 || parseFloat(tvgrandTotal.getText().toString()) > 0) {
                        showDialog(v, false);
                    }
                }
                break;
            case R.id.main_settings_btn:
                if (!ButtonUtils.isFastDoubleClick(R.id.home_card_number_tv)) {
                    Intent intent2 = new Intent();
                    intent2.setClass(this, StaffMemberLoginActivity.class);
                    startActivityForResult(intent2, 1002);
                }
                break;
            case R.id.main_clear_btn:
                clear(0, false);
                break;
            case R.id.main_digital_clear_btn:
                clear(1, false);
                break;
            case R.id.main_digital_add_btn:
                accumulative(false);
                break;
        }
    }

    private void appendCurrentGood() {
        if (seledtedGoodsList.size() == 0)
            accumulative(true);
    }

    private void displayToBanner(SubOrderReqBean orderBean) {
        banner.bannerOrderLayout.setVisibility(View.VISIBLE);
        banner.bannerTotalPriceTv.setText(getString(R.string.string_amount_txt3, parseFloat(orderBean.getTotal_amount())));
        banner.tvPayWay.setText("支付方式：现金支付");
        banner.bannerQRCode.setImageDrawable(this.getResources().getDrawable(R.drawable.logo));
        banner.goodsList.clear();
        banner.goodsList.addAll(orderBean.getGoods());
        banner.adapter.notifyDataSetChanged();
    }

    /**
     * 累计 菜品价格
     */
    private void accumulative(boolean clean) {
        if (selectedGoods == null) {
            return;


        }
        if (TextUtils.isEmpty(etPrice.getText()) && TextUtils.isEmpty(etPrice.getHint().toString())) {
            return;
        }
        if (parseFloat(tvgrandTotal.getText().toString()) <= 0) {
            return;
        }
        HotKeyBean selectedGoods = new HotKeyBean();
        selectedGoods.cid = MainActivity.this.selectedGoods.cid;
        selectedGoods.id = MainActivity.this.selectedGoods.id;
        selectedGoods.name = MainActivity.this.selectedGoods.name;
        selectedGoods.traceable_code = MainActivity.this.selectedGoods.traceable_code;
        selectedGoods.is_default = MainActivity.this.selectedGoods.is_default;
        selectedGoods.batch_code = MainActivity.this.selectedGoods.batch_code;

        selectedGoods.weight = weightTopTv.getText().toString();
        selectedGoods.price = TextUtils.isEmpty(etPrice.getText().toString()) ? etPrice.getHint().toString() : etPrice.getText().toString();
        selectedGoods.grandTotal = tvgrandTotal.getText().toString();
        selectedGoods.count = countEt.getText().toString();
        seledtedGoodsList.add(selectedGoods);
        commodityAdapter.notifyDataSetChanged();

        SQLite.update(HotKeyBean.class)
                .set(HotKeyBean_Table.price.eq(selectedGoods.price))
                .where(HotKeyBean_Table.id.eq(selectedGoods.id))
                .query();
        MainActivity.this.selectedGoods.price = selectedGoods.price;
        calculatePrice();
        if (clean)
            clear(3, false);
    }

    @SuppressLint("DefaultLocale")
    public void calculatePrice() {
        float weightTotalF = 0.0000f;
        float priceTotal = 0;
        for (int i = 0; i < seledtedGoodsList.size(); i++) {
            HotKeyBean goods = seledtedGoodsList.get(i);
            if (!TextUtils.isEmpty(goods.price)) {
                weightTotalF += parseFloat(goods.weight);
                priceTotal += parseFloat(goods.grandTotal);
            }
        }

        weightTotalTv.setText(String.format("%.2f", weightTotalF));
        priceTotalTv.setText(String.format("%.2f", priceTotal));
    }


    @Override
    public void onEventMainThread(BusEvent event) {
        super.onEventMainThread(event);
        if (event != null) {
            if (event.getType() == BusEvent.POSITION_PATCH22) {  //补打上一笔 交易
                String bmpUrl = SPUtils.getString(MainActivity.this, "print_bitmap", "");
                String price = SPUtils.getString(MainActivity.this, "print_price", "");
                String orderNo = SPUtils.getString(MainActivity.this, "print_orderno", "");
                String payId = SPUtils.getString(MainActivity.this, "print_payid", "");
                String priceTotal = SPUtils.getString(MainActivity.this, "print_price", "");
                btnShangtongPrint(bmpUrl,
                        orderNo,
                        payId,
                        operatorTv.getText().toString(),
                        priceTotalTv.getText().toString(),
                        (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));

            }

            if (event.getType() == BusEvent.PRINTER_LABEL || event.getType() == BusEvent.POSITION_PATCH) {
                if (event.getType() == BusEvent.PRINTER_LABEL) {
                    String title = "支付成功";
                    String message = "支付金额：" + priceTotalTv.getText().toString() + "元";
                    int delayTimes = 2000;
                    showLoading(title, message, delayTimes);
                    banner.showPayResult(title, message, delayTimes);
                }

//                bitmap = (Bitmap) event.getParam();
                bitmap = event.getQrString();
                if (bitmap == null) {
                    String bmpUrl = SPUtils.getString(MainActivity.this, "print_bitmap", "");
                    String price = SPUtils.getString(MainActivity.this, "print_price", "");
                    String orderNo = SPUtils.getString(MainActivity.this, "print_orderno", "");
                    String payId = SPUtils.getString(MainActivity.this, "print_payid", "");
                    String priceTotal = SPUtils.getString(MainActivity.this, "print_price", "");
                    btnShangtongPrint(bmpUrl,
                            orderNo,
                            payId,
                            operatorTv.getText().toString(),
                            priceTotalTv.getText().toString(),
                            (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));
                } else {
                    if (stopPrint) {
                        return;
                    }
                    String orderNo = event.getStrParam();
                    String payId = event.getStrParam02();
                    SPUtils.putString(this, "print_orderno", orderNo);
                    SPUtils.putString(this, "print_payid", payId);
                    SPUtils.putString(this, "print_price", priceTotalTv.getText().toString());

                    btnShangtongPrint(bitmap,
                            orderNo,
                            payId,
                            operatorTv.getText().toString(),
                            priceTotalTv.getText().toString(),
                            (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));
                    clear(1, true);
                }
            }
            if (event.getType() == BusEvent.PRINTER_NO_BITMAP) {
                String title = "支付成功";
                String message = "支付金额：" + priceTotalTv.getText().toString() + "元";
                int delayTimes = 2000;
                showLoading(title, message, delayTimes);
                banner.showPayResult(title, message, delayTimes);


//                orderNo = (Math.random() * 9 + 1) * 100000 + getCurrentTime("yyyyMMddHHmmss");
                int random = (int) (Math.random() * 9 + 1) * 100;
                String orderNo = "AX" + getCurrentTime("yyyyMMddHHmmss") + random;
                String payId = event.getStrParam02();
                SPUtils.putString(this, "print_price", priceTotalTv.getText().toString());

                btnShangtongPrint(bitmap,
                        orderNo,
                        payId,
                        operatorTv.getText().toString(),
                        priceTotalTv.getText().toString(),
                        (List<HotKeyBean>) SPUtils.readObject(MainActivity.this, "selectedGoodList"));

                clear(1, true);
            }
            if (event.getType() == BusEvent.NET_WORK_AVAILABLE) {
                boolean available = event.getBooleanParam();
                if (available) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = null;
                    if (connectivityManager != null) {
                        networkInfo = connectivityManager.getActiveNetworkInfo();
                    }
                    if (networkInfo != null && networkInfo.isAvailable()) {
                        Object object = SPUtils.readObject(MainActivity.this, "local_order");
                        List<SubOrderReqBean> orders = (List<SubOrderReqBean>) object;
                        submitOrders(orders);
                    }
                }
            }
            if (event.getType() == BusEvent.SAVE_COMMODITY_SUCCESS) {
                getGoodsData();
            }

            if (event.getType() == BusEvent.LOGIN_OUT) {
                finish();
            }
            if (event.getType() == BusEvent.notifiySellerInfo) {
                getLoginInfo();
                getGoodsData();
            }
        }
    }


    /**
     * shangtong 打印机打印
     *
     * @param bitmap            tupian
     * @param orderNo           订单号
     * @param payId             支付id
     * @param operator          操作员
     * @param price             价格
     * @param seledtedGoodsList 货物选择的列表
     */
    @SuppressLint("StaticFieldLeak")
    public void btnShangtongPrint(final String bitmap, final String orderNo, final String payId, final String operator, final String price, final List<HotKeyBean> seledtedGoodsList) {
        threadPool = ThreadPool.getInstantiation();
        SysApplication application = (SysApplication) MainActivity.this.getApplication();
        final Print print = application.getPrint();
        final HttpHelper helper = new HttpHelper(this, application);
        final String stallNumber2 = stallNumberTv.getText().toString();//摊位号

        // 还需要传订单信息
        final OrderInfo orderInfo = new OrderInfo();

        new AsyncTask<Void, Void, OrderInfo>() {
            @Override
            protected OrderInfo doInBackground(Void... voids) {
                orderInfo.setBillcode(orderNo);
                orderInfo.setBillstatus("成功");
                orderInfo.setSeller(seller);
                orderInfo.setSellerid(sellerid);
                orderInfo.setTerid(tid);
                orderInfo.setMarketid(marketId);
                orderInfo.setTime(getCurrentTime());
                List<ItemsBean> itemsBeans = new ArrayList<>();

                StringBuilder sb = new StringBuilder();
                sb.append("深圳市安鑫宝科技发展有限公司\n");
                String stallNumber;
                if (TextUtils.isEmpty(stallNumber2)) {
                    stallNumber = " ";
                } else {
                    stallNumber = stallNumber2;
                }

                sb.append("交易日期：").append(getCurrentTime()).append("\n");
                sb.append("交易单号：").append(orderNo).append("\n");

                if (TextUtils.equals(payId, "1")) {
                    sb.append("结算方式：微信支付\n");
                    orderInfo.setSettlemethod("微信支付");
                }
                if (TextUtils.equals(payId, "2")) {
                    sb.append("结算方式：支付宝支付\n");
                    orderInfo.setSettlemethod("支付宝支付");
                }
                if (TextUtils.equals(payId, "4")) {
                    sb.append("结算方式：现金支付\n");
                    orderInfo.setSettlemethod("现金支付");
                }

                sb.append("卖方名称：").append(operator).append("\n");
                sb.append("摊位号：").append(stallNumber).append("\n");
                sb.append("商品名\b" + "单价/元\b" + "重量/kg\b" + "金额/元" + "\n");

                for (int i = 0; i < seledtedGoodsList.size(); i++) {
                    HotKeyBean goods = seledtedGoodsList.get(i);
                    ItemsBean itemsBean = new ItemsBean();
                    itemsBean.setItemno(orderNo);
                    itemsBean.setMoney(goods.grandTotal);
                    itemsBean.setName(goods.name);
                    itemsBean.setPrice(goods.price);
                    itemsBean.setUnit("kg");
                    itemsBean.setWeight(goods.weight);
                    itemsBean.setX0("0012017");
                    itemsBean.setX1("0013174");
                    itemsBean.setX2("0013084");
                    itemsBean.setK("0.000151516");
                    itemsBean.setXcur("0.5");
                    itemsBeans.add(itemsBean);
                    sb.append(goods.name).append("\t").append(goods.price).append("\t").append(goods.weight).append("\t").append(goods.grandTotal).append("\n");
                }

                sb.append("--------------------------------\n");
                sb.append("合计(元)：").append(price).append("\n");
                sb.append("司磅员：").append(operator).append("\t\t").append("秤号：").append(AccountManager.getInstance().getScalesId()).append("\n");
                sb.append("\n\n");


                byte[] bytes = null;
                try {
                    int index1 = bitmap.indexOf("url=");
                    if (index1 > 0) {
                        String qrString = bitmap.substring(index1 + 4);
                        if (qrString.length() > 0) {
                            bytes = print.getbyteData(qrString, 32, 32);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                print.setLineSpacing((byte) 32);
                print.PrintString(sb.toString());

                try {
                    if (bytes != null) {
                        print.PrintltString("扫一扫获取追溯信息：");
                        print.printQR(bytes);
                        print.PrintltString("--------------------------------\n\n\n");
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                orderInfo.setItems(itemsBeans);
                return orderInfo;
            }

            @Override
            protected void onPostExecute(OrderInfo orderInfo) {
                helper.commitDD(orderInfo, MainActivity.this, 1);
            }
        }.execute();
    }

//    public void btnReceiptPrint(final Bitmap bitmap, final String orderNo, final String payId, final String operator, final String price, final List<HotKeyBean> seledtedGoodsList) {
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addTask(new Runnable() {
//            @Override
//            public void run() {
//                printerManager.printer(orderNo, payId, operator, price, bitmap, seledtedGoodsList);
//            }
//        });
//    }
//
//    public void btnLabelPrint(final Bitmap bitmap, final String orderNo, final String payId, final String operator, final String price, final List<HotKeyBean> seledtedGoodsList) {
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addTask(new Runnable() {
//            @Override
//            public void run() {
//                printerManager.sendLabel(orderNo, payId, operator, price, bitmap, seledtedGoodsList);
//            }
//        });
//    }


    @SuppressLint("SetTextI18n")
    public void clear(int type, boolean b) {
        if (type == 0) {
            weighUtils.resetBalance();
        }
        if (type == 1) {
            weightTopTv.setText("0.000");
            weightTv.setText("");
            etPrice.setHint("0");
            etPrice.setText("");
            weightTotalTv.setText("0");
            tvgrandTotal.setText("0.00");
            priceTotalTv.setText("0.00");
            seledtedGoodsList.clear();
            commodityNameTv.setText("");

            commodityAdapter = new CommodityAdapter(this, seledtedGoodsList);
            commoditysListView.setAdapter(commodityAdapter);

            goodMenuAdapter.cleanCheckedPosition();
            goodMenuAdapter.notifyDataSetChanged();

        }
        if (type == 3) {
            selectedGoods = null;
            commodityNameTv.setText("");
            goodMenuAdapter.cleanCheckedPosition();
            goodMenuAdapter.notifyDataSetChanged();
            String hint = "";
            if (!TextUtils.isEmpty(etPrice.getText())) {
                hint = etPrice.getText().toString();
            } else if (!TextUtils.isEmpty(etPrice.getHint())) {
                hint = etPrice.getHint().toString();
            }
            etPrice.setText("");
            etPrice.setHint(hint);
        }
    }

    public void showDialog(View v, boolean useCash) {

        if (seledtedGoodsList.size() < 1) {
            accumulative(true);
        }
        Intent intent = new Intent();
        SubOrderReqBean subOrderReqBean = new SubOrderReqBean();
        SubOrderReqBean.Goods good;
        List<SubOrderReqBean.Goods> goodsList = new ArrayList<>();
        for (int i = 0; i < seledtedGoodsList.size(); i++) {
            good = new SubOrderReqBean.Goods();
            HotKeyBean HotKeyBean = seledtedGoodsList.get(i);
            good.setGoods_id(HotKeyBean.id + "");
            good.setGoods_name(HotKeyBean.name);
            good.setGoods_price(HotKeyBean.price);
            good.setGoods_number(countEt.getText().toString());
            good.setGoods_weight(HotKeyBean.weight);
            good.setGoods_amount(HotKeyBean.grandTotal);
            good.setBatch_code(HotKeyBean.batch_code);
            goodsList.add(good);
        }
        subOrderReqBean.setToken(AccountManager.getInstance().getToken());
        subOrderReqBean.setMac(MacManager.getInstace(this).getMac());
        subOrderReqBean.setTotal_amount(priceTotalTv.getText().toString());
        subOrderReqBean.setTotal_weight(weightTotalTv.getText().toString());
        subOrderReqBean.setCreate_time(getCurrentTime());
        subOrderReqBean.setGoods(goodsList);
        if (switchSimpleOrComplex) {
            subOrderReqBean.setPricing_model("2");
        } else {
            subOrderReqBean.setPricing_model("1");
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("orderBean", subOrderReqBean);
        intent.putExtras(bundle);
        intent.setClass(this, UseCashActivity.class);
        SPUtils.saveObject(this, "selectedGoodList", seledtedGoodsList);
        if (useCash) {
            payCashDirect(subOrderReqBean);
        } else {
            startActivity(intent);
        }
    }

    public void payCashDirect(SubOrderReqBean orderBean) {
//        现金直接支付

        displayToBanner(orderBean);
        String payId = "4";
        orderBean.setPayment_id(payId);
        if (NetworkUtil.isConnected(this)) {
//            submitOrder(orderBean);
            new PayCheckManage(this,banner,null,orderBean,payId).submitOrder();
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
        }
    }

    int requestCount;

/*
    public void getPayNotice(final String order_no, final String qrCode, boolean first) {
        if (first) requestCount = 0;
        requestCount++;
        if (requestCount >= 10) return;
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
                                EventBus.getDefault().post(new BusEvent(BusEvent.PRINTER_LABEL, bitmap, order_no, "4", qrCode));
                            } else {
//                              轮循获取支付结果
                                UIUtils.postTaskSafelyDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getPayNotice(order_no, qrCode,false);
                                    }
                                }, 1000);
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
*/

    /*public void submitOrder(final SubOrderReqBean subOrderReqBean) {
        RetrofitFactory.getInstance().API()
                .submitOrder(subOrderReqBean)
                .compose(this.<BaseEntity<SubOrderBean>>setThread())
                .subscribe(new Observer<BaseEntity<SubOrderBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoading();
                    }

                    @Override
                    public void onNext(final BaseEntity<SubOrderBean> subOrderBeanBaseEntity) {
                        if (subOrderBeanBaseEntity.isSuccess()) {
                            switch (subOrderReqBean.getPayment_id()) {
                                case "1":
                                    banner.tvPayWay.setText("支付方式：微信支付");
                                    break;
                                case "2":
                                    banner.tvPayWay.setText("支付方式：支付宝支付");
                                    break;
                                case "4":
                                    banner.tvPayWay.setText("支付方式：现金支付");
                                    break;
                            }
                            SubOrderBean data = subOrderBeanBaseEntity.getData();
                            SPUtils.putString(SysApplication.getContext(), "print_bitmap", data.getPrint_code_img());

                           *//* banner.bannerOrderLayout.setVisibility(View.VISIBLE);
                            banner.bannerTotalPriceTv.setText(getString(R.string.string_amount_txt3, parseFloat(subOrderReqBean.getTotal_amount())));
                            banner.goodsList.clear();
                            banner.goodsList.addAll(subOrderReqBean.getGoods());
                            banner.adapter.notifyDataSetChanged();
                            bitmap = (data.getPrint_code_img());*//*
                            getPayNotice(data.getOrder_no(), data.getPrint_code_img(), true);
                        } else {
                            showLoading(subOrderBeanBaseEntity.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoading();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }
*/

    public void getLoginInfo() {
        RetrofitFactory.getInstance().API()
                .getLoginInfo(AccountManager.getInstance().getToken(), MacManager.getInstace(this).getMac())
                .compose(this.<BaseEntity<LoginInfo>>setThread())
                .subscribe(new Observer<BaseEntity<LoginInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntity<LoginInfo> loginInfoBaseEntity) {
                        if (loginInfoBaseEntity.isSuccess()) {
                            LoginInfo data = loginInfoBaseEntity.getData();
                            stallNumberTv.setText(data.boothNumber);
                            operatorTv.setText(data.client_name);
                            componyTitleTv.setText(data.organizationName);
                            switch (data.user_type) {
                                case LoginInfo.TYPE_seller:
                                    loginTypeName.setText("商户:");
                                    break;
                                case LoginInfo.TYPE_ADMIN:
                                    loginTypeName.setText(MainActivity.this.getString(R.string.string_operator));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });

    }

    public void getGoodsData() {
        mNotPushRemote = (boolean) SPUtils.get(this, CommodityManagementActivity.SelectedGoodsState.NOT_PUSH_REMOTE, false);
        List<HotKeyBean> hotKeyGoods = (List<HotKeyBean>) FileUtils.readObject(MainActivity.this, CommodityManagementActivity.SelectedGoodsState.selectedGoods);
        boolean connected = NetworkUtil.isConnected(MainActivity.this);
        showSelectedGoods(hotKeyGoods);
        if (!connected) return;
        if (mNotPushRemote) {
            requestPushStoreGoods(hotKeyGoods);
        } else {
            requestGoodsData();
        }
    }

    private void requestGoodsData() {
        RetrofitFactory.getInstance().API()
                .getGoodsData(AccountManager.getInstance().getToken(), MacManager.getInstace(this).getMac())
                .compose(this.<BaseEntity<ScalesCategoryGoods>>setThread())
                .subscribe(new Observer<BaseEntity<ScalesCategoryGoods>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntity<ScalesCategoryGoods> scalesCategoryGoodsBaseEntity) {
                        if (scalesCategoryGoodsBaseEntity.isSuccess()) {
                            ScalesCategoryGoods scalesCategoryGoods = scalesCategoryGoodsBaseEntity.getData();
                            List<HotKeyBean> hotKeyGoods = scalesCategoryGoods.getHotKeyGoods();
                            FileUtils.saveObject(MainActivity.this, (Serializable) hotKeyGoods, CommodityManagementActivity.SelectedGoodsState.selectedGoods);
                            showSelectedGoods(hotKeyGoods);
                        } else {
                            showLoading(scalesCategoryGoodsBaseEntity.getMsg(), "数据加载失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    private void showSelectedGoods(List<HotKeyBean> hotKeyGoods) {
        if (hotKeyGoods == null) return;
        HotKeyBeanList.clear();
        HotKeyBeanList.addAll(hotKeyGoods);
        HotKeyBean hotKey = new HotKeyBean();
        SQLite.delete(HotKeyBean.class).execute();
        ModelAdapter<HotKeyBean> modelAdapter = FlowManager.getModelAdapter(HotKeyBean.class);
        for (HotKeyBean goods : HotKeyBeanList) {
            hotKey.id = goods.id;
            hotKey.cid = goods.cid;
            hotKey.grandTotal = goods.grandTotal;
            hotKey.is_default = goods.is_default;
            hotKey.name = goods.name;
            hotKey.price = goods.price;
            hotKey.traceable_code = goods.traceable_code;
            hotKey.weight = goods.weight;
            hotKey.batch_code = goods.batch_code;
            modelAdapter.insert(hotKey);
        }
        goodMenuAdapter.notifyDataSetChanged();
    }

    public void requestPushStoreGoods(List<HotKeyBean> hotKeyGoods) {
        if (hotKeyGoods == null) {
            requestGoodsData();
            return;
        }
        SaveGoodsReqBean goodsReqBean = new SaveGoodsReqBean();
        List<SaveGoodsReqBean.Goods> goodsList = new ArrayList<>();
        SaveGoodsReqBean.Goods good;
        for (int i = 0; i < hotKeyGoods.size(); i++) {
            good = new SaveGoodsReqBean.Goods();
            HotKeyBean bean = hotKeyGoods.get(i);
            good.id = bean.id;
            good.cid = bean.cid;
            good.is_default = bean.is_default;
            good.name = bean.name;
            good.price = bean.price;
            good.traceable_code = bean.traceable_code;
            goodsList.add(good);
        }
        goodsReqBean.setToken(AccountManager.getInstance().getAdminToken());
        goodsReqBean.setMac(MacManager.getInstace(SysApplication.getContext()).getMac());
        goodsReqBean.setGoods(goodsList);
        RetrofitFactory.getInstance().API()
                .storeGoodsData(goodsReqBean)
                .compose(this.<BaseEntity>setThread())
                .subscribe(new Observer<BaseEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntity baseEntity) {
                        if (mNotPushRemote && baseEntity.isSuccess()) {
                            SPUtils.put(MainActivity.this, CommodityManagementActivity.SelectedGoodsState.NOT_PUSH_REMOTE, false);
                        }
                        requestGoodsData();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    public void showInfoToBanner(SubOrderReqBean bean) {
        banner.bannerOrderLayout.setVisibility(View.VISIBLE);
        banner.bannerTotalPriceTv.
                setText(getString(R.string.string_amount_txt3, Float.parseFloat(bean.getTotal_amount())));
        banner.goodsList.clear();
        banner.goodsList.addAll(bean.getGoods());
        banner.adapter.notifyDataSetChanged();
    }


    /**
     * 连接错误
     *
     * @param volleyError 错误信息
     * @param flag        请求表示索引
     */
    @Override
    public void onResponse(VolleyError volleyError, int flag) {
        MyLog.myInfo("错误");
    }

    @Override
    public void onResponse(JSONObject jsonObject, int flag) {
        MyLog.myInfo("成功" + jsonObject.toString());
    }


    @Override
    public void onResponseError(VolleyError volleyError, int flag) {
        MyLog.myInfo("错误");
    }

    @Override
    public void onResponse(String response, int flag) {
        MyLog.myInfo("成功" + response);
    }
}
