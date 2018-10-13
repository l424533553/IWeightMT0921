package com.axecom.iweight.manager;

import android.text.TextUtils;

import com.axecom.iweight.bean.HotKeyBean;
import com.axecom.iweight.bean.OrderGoods;
import com.axecom.iweight.bean.OrderLocal;
import com.axecom.iweight.utils.CommonUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/10/13.
 */

public class RecordManage {
    public static void record(
            String companyName,
            String bitmap, String orderNo,
            String seller,
            int sellerid, int tid,
            int marketId, String payId,
            String operator, String price,
            String stallNumber, List<HotKeyBean> seledtedGoodsList) {

        OrderLocal orderLocal = new OrderLocal();
        orderLocal.companyName = companyName;
        orderLocal.qrCode = bitmap;
        orderLocal.orderNumber = orderNo;
        orderLocal.seller = seller;
        orderLocal.sellerid = sellerid;
        orderLocal.tid = tid;
        orderLocal.marketId = marketId;
        orderLocal.payId = payId;
        orderLocal.operator = operator;
        orderLocal.stallNumber = stallNumber;
        orderLocal.totalAmount = parseDouble(price);
        orderLocal.orderTime = new Date(System.currentTimeMillis());
        orderLocal.save();

        for (HotKeyBean bean :
                seledtedGoodsList) {
            OrderGoods goods = new OrderGoods();
            goods.orderNumber = orderNo;
            goods.name = bean.name;
            goods.weight = parseFloat(bean.weight);
            goods.price = parseFloat(bean.price);
            goods.amount =parseDouble(bean.grandTotal);
//            goods.countType =bean.
            goods.save();
        }
    }

    private static float parseFloat(String s) {
        if (TextUtils.isEmpty(s)) return 0;
        return Float.parseFloat(s);
    }


    private static double parseDouble(String s) {
        if (TextUtils.isEmpty(s)) {
            s = "0";
        }
        return Double.parseDouble(s);
    }

}
