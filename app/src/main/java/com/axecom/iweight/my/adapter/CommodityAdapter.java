package com.axecom.iweight.my.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.axecom.iweight.R;
import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.ScalesCategoryGoods;

import java.util.List;

/**
 * author: luofaxin
 * date： 2018/9/25 0025.
 * email:424533553@qq.com
 * describe:
 */
public class CommodityAdapter extends BaseAdapter {
    private Context context;
    private List<HotKeyBean> list;

    public CommodityAdapter(Context context, List<HotKeyBean> list) {
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.commodity_item, null);
            holder = new ViewHolder();
            holder.nameTv = convertView.findViewById(R.id.commodity_name_tv);
            holder.priceTv = convertView.findViewById(R.id.commodity_price_tv);
//                holder.countTv = convertView.findViewById(R.id.commodity_count_tv);
            holder.weightTv = convertView.findViewById(R.id.commodity_weight_tv);
            holder.subtotalTv = convertView.findViewById(R.id.commodity_subtotal_tv);
            holder.deleteBtn = convertView.findViewById(R.id.commodity_delete_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HotKeyBean goods = list.get(position);
        holder.nameTv.setText(goods.name);
        if (Integer.parseInt(goods.count) > 0) {
            holder.weightTv.setText(goods.count);
        } else {
            if ((goods.weight).indexOf('.') == -1 || goods.weight.length() - (goods.weight.indexOf(".") + 1) <= 1) {
                holder.weightTv.setText(Float.parseFloat(goods.weight) / 1000 + "");
//                holder.weightTv.setText(context.getResources().getString(R.string.string_weight_unit_kg, Float.parseFloat(goods.weight) / 1000 + ""));
            } else {
                holder.weightTv.setText( goods.weight);
//                holder.weightTv.setText(context.getResources().getString(R.string.string_weight_unit_kg, goods.weight));
            }
        }
        holder.priceTv.setText(goods.price);
        holder.subtotalTv.setText(goods.grandTotal);
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
