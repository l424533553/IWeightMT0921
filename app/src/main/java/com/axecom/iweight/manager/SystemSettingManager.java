package com.axecom.iweight.manager;

import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.net.RetrofitFactory;
import com.google.gson.internal.LinkedTreeMap;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by andy on 2018/10/10.
 */

public class SystemSettingManager {

    public static void getSettingData(BaseActivity context) {
        RetrofitFactory.getInstance().API()
                .getSettingData(AccountManager.getInstance().getToken(), MacManager.getInstace(context).getMac())
                .compose(context.<BaseEntity>setThread())
                .subscribe(new Observer<BaseEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(BaseEntity settingDataBeanBaseEntity) {
                        boolean isSu = settingDataBeanBaseEntity.isSuccess();
                        if (settingDataBeanBaseEntity.isSuccess()) {
                            LinkedTreeMap map = (LinkedTreeMap) settingDataBeanBaseEntity.getData();

                        } else {
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

}
