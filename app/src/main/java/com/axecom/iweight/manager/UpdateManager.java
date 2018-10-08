package com.axecom.iweight.manager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.axecom.iweight.R;
import com.axecom.iweight.base.BaseActivity;
import com.axecom.iweight.base.BaseEntity;
import com.axecom.iweight.bean.VersionBean;
import com.axecom.iweight.net.RetrofitFactory;
import com.axecom.iweight.ui.activity.MainActivity;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.axecom.iweight.ui.view.CustomDialog;
import com.axecom.iweight.utils.CommonUtils;
import com.google.gson.Gson;


import java.io.File;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Administrator on 2016-9-11.
 */
public class UpdateManager {
    public static String TAG = "UpdateService";
    public static int NOTIFI_ID = 12345;
    private static File DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    private NotificationManager notificationManager;
    private Notification notification;
    private CustomDialog dialog;

    public void getNewVersion(final Activity context) {
        String url = " http://api.fir.im/apps/latest/57fb6dae959d69487d000008?api_token=9bb7210eb739780524f859683bcc40bc";
//        String url = " http://api.fir.im/latest/57ccb739780524f859683bcc40bc";
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("update", "update fail");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
//                final AppInfo appInfo = gson.fromJson(response.body().string(), AppInfo.class);
//                int versionCode = CommonUtils.getVersionCode(context);
//                if (appInfo.getVersion() == null) {
//                    return;
//                }
//
//                if (Integer.valueOf(appInfo.getVersion()) <= versionCode) {
//                    return;
//                }
//                UIUtils.postTaskSafely(new Runnable() {
//                    @Override
//                    public void run() {
//                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context)
//                                .setTitle(context.getString(R.string.update_tip_title))
//                                .setMessage(appInfo.getName() + "\n" + context.getString(R.string.update_new_version) + appInfo.getVersionShort() + "\n" + appInfo.getChangelog())
//                                .setCancelable(false)
//                                .setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                       /* Intent intent = new Intent(context, UpdateService.class);
//                                        intent.putExtra(UpdateService.UPDATE_URL, appInfo.getInstall_url());*/
////                                        context.startService(intent);
//                                    }
//                                }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
////                                        work(0);
//                                    }
//                                });
//                        builder.show();
//                    }
//                });
            }

        });
    }

    public void getNewVersion(final Activity context, int typ) {
        RetrofitFactory.getInstance().API()
                .getVersion()
                .compose(((BaseActivity)context).<BaseEntity<VersionBean>>setThread())
                .subscribe(new Observer<BaseEntity<VersionBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseEntity<VersionBean> versionBeanBaseEntity) {
                        if(versionBeanBaseEntity.isSuccess()){
                            VersionBean version = versionBeanBaseEntity.getData();
                            String versionName = CommonUtils.getVersionName(context);
                            /*if(!TextUtils.isEmpty(version.version) && !TextUtils.isEmpty(versionName) && !TextUtils.isEmpty(version.downloadurl)){

                                if(version.version.compareTo(versionName) > 0){
                                    showDialog(context, version);
                                }
                            }*/
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

    private void showDialog(final Activity context, final VersionBean version) {
        UIUtils.postTaskSafely(new Runnable() {
            @Override
            public void run() {
                final CustomDialog.Builder builder = new CustomDialog.Builder(context);
                builder.setMessage(context.getString(R.string.app_update_found_version) + version.version);
                dialog = builder.setMessage(version.description)
                        .setPositiveButton(context.getString(R.string.app_update_now), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startTask(context, version.downloadurl);
//                                startTask2(context, response.getDownloadurl());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.app_update_ignore), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        }).createTwoButtonDialog();
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }


    private void startTask(final Context context, String url) {
        createNotification(context);
        AppDownloadTask task = new AppDownloadTask(url, notificationManager, notification);
        task.downloadApk(DOWNLOAD_DIR);
    }

    private void startTask2(final Context context, String url) {
        String path = getFileName(url);
        new DownloadTask(context, url, path, new IDownloadListener() {
            @Override
            public void onDownloadStarted() {
                createNotification(context);
            }

            @Override
            public void onDownloadFinished(DownloadResult result) {
                Log.d(TAG, "Start download finshed ");
                notificationManager.cancel(UpdateManager.NOTIFI_ID);
                CommonUtils.install(UIUtils.getContext(), Uri.fromFile(new File(result.path)));
            }

            @Override
            public void onProgressUpdate(Float... value) {
                int progress = Math.round(value[0] / value[1] * 100);
                notification.contentView.setProgressBar(R.id.pb_update, 100, progress, false);
                notification.contentView.setTextViewText(R.id.tv_progress, progress + "%");
                notificationManager.notify(UpdateManager.NOTIFI_ID, notification);
            }
        }).execute();
    }

    private String getFileName(String url) {
        String filename = "";
        if (!TextUtils.isEmpty(url)) {
            String[] split = url.split("/");
            if (split.length > 0) {
                filename = split[split.length - 1];
            }
        }
        if (!filename.contains(".apk")) filename += ".apk";

        return DOWNLOAD_DIR + File.separator + filename;
    }


    public void createNotification(Context content) {
        notificationManager = (NotificationManager) content.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification();
        notification.icon = R.drawable.abnormal_setting;
        notification.tickerText = "开始下载";
        RemoteViews contentView = new RemoteViews(content.getPackageName(), R.layout.notification_item);
        contentView.setProgressBar(R.id.pb_update, 100, 0, false);
        contentView.setTextViewText(R.id.tv_progress, "0");
        notification.contentView = contentView;
        Intent updateIntent = new Intent(content, MainActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notification.contentIntent = PendingIntent.getActivity(content, 0, updateIntent, 0);
        notificationManager.notify(NOTIFI_ID, notification);
    }
}
