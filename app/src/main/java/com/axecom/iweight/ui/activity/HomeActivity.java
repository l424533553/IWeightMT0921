package com.axecom.iweight.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.VolleyError;
import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.base.BusEvent;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.bean.LoginData;
import com.axecom.iweight.bean.User;
import com.axecom.iweight.bean.User_Table;
import com.axecom.iweight.bean.WeightBean;
import com.axecom.iweight.manager.AccountManager;
import com.axecom.iweight.manager.MacManager;
import com.axecom.iweight.manager.SystemSettingManager;
import com.axecom.iweight.manager.UpdateManager;
import com.axecom.iweight.my.LogActivity;
import com.axecom.iweight.my.entity.LogBean;
import com.axecom.iweight.my.entity.ResultInfo;
import com.axecom.iweight.my.entity.UserInfo;
import com.axecom.iweight.my.net.NetHelper;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.view.CustomDialog;
import com.axecom.iweight.ui.view.SoftKeyborad;
import com.axecom.iweight.utils.ButtonUtils;
import com.axecom.iweight.utils.LogUtils;
import com.axecom.iweight.utils.NetworkUtil;
import com.axecom.iweight.utils.SPUtils;
import com.google.gson.internal.LinkedTreeMap;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.luofx.listener.VolleyListener;
import com.luofx.utils.DateUtils;
import com.luofx.utils.PreferenceUtils;
import com.luofx.utils.log.MyLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.shangtongyin.tools.serialport.IConstants_ST;

import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static com.axecom.iweight.ui.activity.SystemSettingsActivity.KEY_DEFAULT_LOGIN_TYPE;

/**
 * Created by Administrator on 2018-5-8.
 */
public class HomeActivity extends BaseActivity implements VolleyListener, IConstants_ST {

    private static final String AUTO_LOGIN = "auto_login";
    private static final String WEIGHT_ID = "weight_id";
    private TextView cardNumberTv;
    private TextView pwdTv;

    private TextView weightTv;
    private int weightId;
    private CheckedTextView savePwdCtv;
    UsbManager manager;
    public boolean threadStatus = false;
    UsbSerialPort port;
    public BannerActivity banner = null;
    private String loginType;
    private CheckedTextView autoLogin;
    private Handler mHandler = new Handler();
    private boolean cancelAutoLogin;
    private Button confirmBtn;
    private boolean reBoot;

    /****************************************************************************************/

    @Override
    public View setInitView() {
        reBoot = getIntent().getBooleanExtra(SettingsActivity.IS_RE_BOOT, false);

        View rootView = null;
        try {
            rootView = LayoutInflater.from(this).inflate(R.layout.activity_home, null);
            confirmBtn = rootView.findViewById(R.id.home_confirm_btn);
            cardNumberTv = rootView.findViewById(R.id.home_card_number_tv);
            pwdTv = rootView.findViewById(R.id.home_pwd_tv);
            TextView loginTv = rootView.findViewById(R.id.home_login_tv);
            weightTv = rootView.findViewById(R.id.home_weight_number_tv);
            savePwdCtv = rootView.findViewById(R.id.home_save_pwd_ctv);
            savePwdCtv.setChecked(AccountManager.getInstance().getRememberPwdState());
            autoLogin = rootView.findViewById(R.id.home_login_auto);
            autoLogin.setOnClickListener(this);
            boolean autoLoin = (boolean) SPUtils.get(this, AUTO_LOGIN, false);
            autoLogin.setChecked(autoLoin);
            pwdTv.setOnClickListener(this);
            loginTv.setOnClickListener(this);
            cardNumberTv.setOnClickListener(this);
            confirmBtn.setOnClickListener(this);
            savePwdCtv.setOnClickListener(this);
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            //获取屏幕数量
            Display[] presentationDisplays = new Display[0];
            if (displayManager != null) {
                presentationDisplays = displayManager.getDisplays();
            }

            if (presentationDisplays.length > 1) {
                if (banner == null) {
                    banner = new BannerActivity(this.getApplicationContext(), presentationDisplays[1]);
                    Objects.requireNonNull(banner.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    banner.show();
                }
            }

            if (!banner.isShowing())
                banner.show();
        } catch (Exception e) {
            LogBean logBean = new LogBean();
            logBean.setMessage(e.getMessage());
            logBean.setTime(DateUtils.getYY_TO_ss(new Date()));
            logBean.setType(sysApplication.TYPE_ERROR);
            logBean.setLocation(getLocalClassName());
            sysApplication.getBaseDao().insert(logBean);
        }
        return rootView;
    }

    /**
     * 上下文对象
     */
    private Context context;

    @Override
    public void initView() {
        context = this;
        if (NetworkUtil.isConnected(this)) {
            getSettingData(MacManager.getInstace(this).getMac());
        }
        getScalesIdByMac(MacManager.getInstace(this).getMac());
        SysApplication application = (SysApplication) getApplication();
        NetHelper netHelper = new NetHelper(application, this);
        netHelper.getUserInfo(netHelper.getIMEI(HomeActivity.this), 1);
        findViewById(R.id.ivLog).setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        registerReceiver(usbReceiver, intentFilter);

        initAutoLogin();
    }

    private void initAutoLogin() {
        String lastSerialNumber = AccountManager.getInstance().getLastSerialNumber();
        boolean serialNumberEmpty = TextUtils.isEmpty(lastSerialNumber);
        String pwd ="";
        if(!serialNumberEmpty){
            pwd = AccountManager.getInstance().getPwdBySerialNumber(lastSerialNumber);
            cardNumberTv.setText(lastSerialNumber);
            pwdTv.setText(pwd);
        }
        if (reBoot||!autoLogin.isChecked() || serialNumberEmpty||TextUtils.isEmpty(pwd)) return;
        CustomDialog.Builder builder;
        builder = new CustomDialog.Builder(this);
        builder.setMessage("自动登录中...").setSingleButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAutoLogin = true;
            }
        });
        final CustomDialog singleButtonDialog = builder.createSingleButtonDialog();
        singleButtonDialog.show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                singleButtonDialog.dismiss();
                if (cancelAutoLogin) return;
                if (confirmBtn != null) confirmBtn.callOnClick();
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadStatus = true;
        unregisterReceiver(usbReceiver);
    }

    @Override
    public void onEventMainThread(BusEvent event) {
        super.onEventMainThread(event);
        if (event.getType() == BusEvent.NET_WORK_AVAILABLE) {
            boolean available = event.getBooleanParam();
            if (available) {
                getScalesIdByMac(MacManager.getInstace(this).getMac());
                getSettingData(MacManager.getInstace(this).getMac());
//                getScalesIdByMac("80:5e:4f:85:57:9d");
            }
        }

    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                threadStatus = true;
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    if (device.getVendorId() == 6790 && device.getProductId() == 29987) {
                        showLoading("读卡器被拔出，请检查设备");
                    }
                    if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                        showLoading("打印机被拔出，请检查设备");
                    }
                }
            }
            if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                threadStatus = false;
                LogUtils.d("ACTION_USB_DEVICE_ATTACHED");
            }
        }
    };

    @Override
    public void onClick(View v) {
        SoftKeyborad.Builder builder = new SoftKeyborad.Builder(HomeActivity.this);
        switch (v.getId()) {
            case R.id.home_login_tv:
//                openGpinter();
                break;
            case R.id.home_confirm_btn:
                String serialNumber = cardNumberTv.getText().toString();
                AccountManager.getInstance().saveLastSerialNumber(serialNumber);
                String password = pwdTv.getText().toString();
                if (TextUtils.isEmpty(serialNumber) && getString(R.string.Administrators_pwd).equals(password)) {
                    startDDMActivity(SettingsActivity.class, true);
                }

                if (NetworkUtil.isConnected(this)) {
                    LinkedHashMap valueMap = (LinkedHashMap) SPUtils.readObject(this, KEY_DEFAULT_LOGIN_TYPE);
                    String value = "";
                    if (valueMap != null) {
                        value = valueMap.get("val").toString();
                    }
                    loginType = SystemSettingManager.default_login_type();
                    if (TextUtils.equals(loginType, "卖方卡") || TextUtils.equals(loginType, "3.0") || TextUtils.isEmpty(loginType)) {
                        clientLogin(weightId + "", serialNumber, password);
                    } else {
                        staffMemberLogin(weightId + "", serialNumber, password);
                    }
                } else {
                    if (!TextUtils.isEmpty(cardNumberTv.getText())) {
                        User user = SQLite.select().from(User.class).where(User_Table.card_number.is(serialNumber)).querySingle();
                        if (user != null) {
                            if (TextUtils.equals(pwdTv.getText(), user.password)) {
                                AccountManager.getInstance().setAdminToken(user.user_token);
                                startDDMActivity(MainActivity.class, false);
                                finish();
                            } else {
                                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "没有该卡号", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                break;
            case R.id.home_card_number_tv:
                if (!ButtonUtils.isFastDoubleClick(R.id.home_card_number_tv)) {
                    builder.create(new SoftKeyborad.OnConfirmedListener() {
                        @Override
                        public void onConfirmed(String result) {
                            cardNumberTv.setText(result);
                            if (AccountManager.getInstance().getPwdBySerialNumber(result) != null) {
                                pwdTv.setText(AccountManager.getInstance().getPwdBySerialNumber(result));
                                savePwdCtv.setChecked(true);
                            } else {
                                savePwdCtv.setChecked(false);
                            }
                        }
                    }).show();
                }

                break;
            case R.id.home_pwd_tv:
                if (!ButtonUtils.isFastDoubleClick(R.id.home_pwd_tv)) {
                    builder.create(new SoftKeyborad.OnConfirmedListener() {
                        @Override
                        public void onConfirmed(String result) {
                            pwdTv.setText(result);
                        }
                    }).show();
                }
                break;
            case R.id.home_save_pwd_ctv:
                savePwdCtv.setChecked(!savePwdCtv.isChecked());
                AccountManager.getInstance().saveRememberPwdState(savePwdCtv.isChecked());
                break;

            case R.id.ivLog:
                //进入日志界面
                Intent intent = new Intent(this, LogActivity.class);
                startActivity(intent);
                break;
            case R.id.home_login_auto:
                autoLogin.setChecked(!autoLogin.isChecked());
                SPUtils.put(this, AUTO_LOGIN, autoLogin.isChecked());
                break;
            default:
                break;
        }
    }

    public void getSettingData(String mac) {
        SystemSettingManager.getSettingData(this);
    }

    public void getScalesIdByMac(String mac) {
        final AccountManager instance = AccountManager.getInstance();

        String scalesId = instance.getScalesId();
        if(scalesId !=null){
            weightTv.setText(scalesId);
            weightId = Integer.valueOf(scalesId);
            return;
        }
        RetrofitFactory.getInstance().API()
                .getScalesIdByMac(mac)
                .compose(this.<BaseEntity<WeightBean>>setThread())
                .subscribe(new Observer<BaseEntity<WeightBean>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(final BaseEntity<WeightBean> baseEntity) {
                        if (baseEntity.isSuccess()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    weightId = baseEntity.getData().getId();
                                    weightTv.setText(weightId + "");
                                    instance.saveScalesId(weightId + "");
                                }
                            });
                        } else {
                            showLoading(baseEntity.getMsg());
                        }
                    }


                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    public void clientLogin(String scalesId, final String serialNumber, final String password) {
        RetrofitFactory.getInstance().API()
                .clientLogin(scalesId, serialNumber, password)
                .compose(this.<BaseEntity<LoginData>>setThread())
                .subscribe(new Observer<BaseEntity<LoginData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoading();

                    }

                    @Override
                    public void onNext(BaseEntity<LoginData> loginDataBaseEntity) {
                        if (loginDataBaseEntity.isSuccess()) {
                            AccountManager.getInstance().saveToken(loginDataBaseEntity.getData().getToken());
                            if (savePwdCtv.isChecked()||autoLogin.isChecked()) {
                                AccountManager.getInstance().savePwd(serialNumber, password);
                            } else {
                                AccountManager.getInstance().savePwd(serialNumber, null);
                            }
                            User user = new User();
                            ModelAdapter<User> userAdapter = FlowManager.getModelAdapter(User.class);
                            user.card_number = serialNumber;
                            user.password = password;
                            user.user_token = loginDataBaseEntity.getData().getToken();
                            userAdapter.insert(user);
                            startDDMActivity(MainActivity.class, false);
                            finish();
                        } else {
                            showLoading(loginDataBaseEntity.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoading();

                    }

                    @Override
                    public void onComplete() {
                        closeLoading();

                    }
                });
    }

    public void staffMemberLogin(String scalesId, final String serialNumber, final String password) {
        RetrofitFactory.getInstance().API()
                .staffMemberLogin(scalesId, serialNumber, password)
                .compose(this.<BaseEntity<LoginData>>setThread())
                .subscribe(new Observer<BaseEntity<LoginData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        showLoading();

                    }

                    @Override
                    public void onNext(BaseEntity<LoginData> loginDataBaseEntity) {
                        if (loginDataBaseEntity.isSuccess()) {
                            AccountManager.getInstance().saveToken(loginDataBaseEntity.getData().getAdminToken());
                            if (savePwdCtv.isChecked()) {
                                AccountManager.getInstance().savePwd(serialNumber, password);
                            } else {
                                AccountManager.getInstance().savePwd(serialNumber, null);
                            }
                            User user = new User();
                            ModelAdapter<User> userAdapter = FlowManager.getModelAdapter(User.class);
                            user.card_number = serialNumber;
                            user.password = password;
                            user.user_token = loginDataBaseEntity.getData().getToken();
                            userAdapter.insert(user);
                            startDDMActivity(MainActivity.class, false);
                        } else {
                            showLoading(loginDataBaseEntity.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        closeLoading();
                    }

                    @Override
                    public void onComplete() {
                        closeLoading();
                    }
                });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResponse(VolleyError volleyError, int flag) {
        MyLog.myInfo("错误");
    }

    @Override
    public void onResponse(JSONObject jsonObject, int flag) {
        ResultInfo resultInfo = JSON.parseObject(jsonObject.toString(), ResultInfo.class);
        if (resultInfo != null) {
            UserInfo userInfo = JSON.parseObject(resultInfo.getData(), UserInfo.class);
            if (userInfo != null) {
                SharedPreferences sp = PreferenceUtils.getSp(context);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(MARKET_ID, userInfo.getMarketid());
                editor.putInt(TID, userInfo.getTid());
                editor.putInt(SELLER_ID, userInfo.getSellerid());
                editor.putString(SELLER, userInfo.getSeller());
                editor.apply();
            }
        }
    }
}
