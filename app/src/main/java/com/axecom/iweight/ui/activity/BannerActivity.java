package com.axecom.iweight.ui.activity;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.axecom.iweight.R;
import com.axecom.iweight.bean.SubOrderReqBean;
import com.axecom.iweight.ui.uiutils.UIUtils;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/7/20.
 */

public class BannerActivity extends Presentation {

    public ConvenientBanner convenientBanner;
    private Context context;


    public TextView bannerTotalPriceTv,tvPayWay;
    public ImageView bannerQRCode;
    public LinearLayout bannerOrderLayout;
    public List<SubOrderReqBean.Goods> goodsList;
    public MyAdapter adapter;
    public List<String> list;
    private LinearLayout alertView;
    private Button messageBtn;
    private TextView titleTv;

    public BannerActivity(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_activity);
        ListView orderListView = findViewById(R.id.banner_order_listview);
        bannerQRCode = findViewById(R.id.banner_qrcode_iv);
        tvPayWay = findViewById(R.id.tvPayWay);
        bannerTotalPriceTv = findViewById(R.id.banner_total_price_tv);
        bannerOrderLayout = findViewById(R.id.banner_order_layout);
        convenientBanner = findViewById(R.id.banner_convenient_banner);

        alertView = findViewById(R.id.ll_alert);
        titleTv = findViewById(R.id.tv_title);
        messageBtn = findViewById(R.id.alert_message);
        List<Integer> localImages = new ArrayList<>();
        localImages.add(R.drawable.logo);
        convenientBanner.setPages(
                new CBViewHolderCreator<LocalImageHolderView>() {
                    @Override
                    public LocalImageHolderView createHolder() {
                        return new LocalImageHolderView();
                    }
                }, localImages);

        goodsList = new ArrayList<>();
        adapter = new MyAdapter(context, goodsList);
        orderListView.setAdapter(adapter);

    }

    public void showPayResult(String titleText,String confirmText,long times) {
        titleTv.setText(titleText);
        messageBtn.setText(confirmText);
        alertView.setVisibility(View.VISIBLE);

        UIUtils.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertView.setVisibility(View.GONE);
            }
        }, times);
    }

    public class LocalImageHolderView implements Holder<Integer> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, Integer data) {
            imageView.setImageResource(data);
        }
    }

    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<SubOrderReqBean.Goods> list;

        private MyAdapter(Context context, List<SubOrderReqBean.Goods> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.commodity_item, null);
                holder = new ViewHolder();
                holder.nameTv = convertView.findViewById(R.id.commodity_name_tv);
                holder.priceTv = convertView.findViewById(R.id.commodity_price_tv);
                holder.weightTv = convertView.findViewById(R.id.commodity_weight_tv);
                holder.subtotalTv = convertView.findViewById(R.id.commodity_subtotal_tv);
                holder.deleteBtn = convertView.findViewById(R.id.commodity_delete_btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SubOrderReqBean.Goods goods = list.get(position);
            holder.nameTv.setText(goods.getGoods_name());
            holder.weightTv.setText(goods.getGoods_weight());
            holder.priceTv.setText(goods.getGoods_price());
            holder.subtotalTv.setText(goods.getGoods_amount());

            return convertView;
        }

        class ViewHolder {
            TextView nameTv;
            TextView priceTv;
            TextView weightTv;
            TextView subtotalTv;
            Button deleteBtn;
        }
    }
}
