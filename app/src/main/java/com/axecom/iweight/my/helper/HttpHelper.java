package com.axecom.iweight.my.helper;

import com.alibaba.fastjson.JSON;
import com.axecom.iweight.base.SysApplication;
import com.axecom.iweight.my.entity.ItemsBean;
import com.axecom.iweight.my.entity.OrderInfo;
import com.luofx.listener.VolleyListener;
import com.luofx.listener.VolleyStringListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: luofaxin
 * date： 2018/9/10 0010.
 * email:424533553@qq.com
 * describe:
 */
public class HttpHelper {
    private static String IP = "http://119.23.43.64/";
    private VolleyListener listener;
    SysApplication application;

    public void setApplication(SysApplication application) {
        this.application = application;
    }

    public void setListener(VolleyListener listener) {
        this.listener = listener;
    }

    public HttpHelper(VolleyListener listener, SysApplication application) {
        this.listener = listener;
        this.application = application;
    }

    /**
     * @param marketid     市场编号
     * @param terid        秤id
     * @param flag         设备状态   0正常/1异常
     * @param requestIndex 请求索引
     */
    public void upState(int marketid, int terid, int flag, int requestIndex) {
        String url = IP + "api/smartsz/addatatus?marketid=" + marketid + "&terid=" + terid + "&flag=" + flag;
        application.volleyGet(url, listener, requestIndex);
    }


    /**
     * 订单信息
     *
     * @param orderInfo 订单信息
     * @param flag      表示
     */
    public void commitDD(OrderInfo orderInfo, VolleyStringListener volleyStringListener, int flag) {
        String url = IP + "api/smart/commitszex?";


        Map<String, String> map = new HashMap<>();
        map.put("marketid", String.valueOf(orderInfo.getMarketid()));
        map.put("billcode", orderInfo.getBillcode());
        map.put("billstatus", orderInfo.getBillstatus());
        map.put("seller", orderInfo.getSeller());

        map.put("sellerid", String.valueOf(orderInfo.getSellerid()));
        map.put("settlemethod", orderInfo.getSettlemethod());
        map.put("terid", String.valueOf(orderInfo.getTerid()));
        map.put("time", orderInfo.getTime());

        List<ItemsBean> items = orderInfo.getItems();
        if (items != null) {
            String jsonItem = JSON.toJSONString(items);
            map.put("items", jsonItem);
        }
        application.volleyPost(url, map, volleyStringListener, flag);
    }

    /**
     * 根据mac地址获得
     */
    private void getUserInfo(int flag) {
        String url = IP + "api/smart/ getinfobymac?";
        application.volleyGet(url, listener, flag);
    }
}
