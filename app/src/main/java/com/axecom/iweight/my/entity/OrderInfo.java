package com.axecom.iweight.my.entity;

import java.util.List;

/**
 * author: luofaxin
 * date： 2018/9/11 0011.
 * email:424533553@qq.com
 * describe:
 */
public class OrderInfo {
    /**
     * billcode : AX1234
     * billstatus : 成功
     * seller : 梁日成
     * sellerid : 1025
     * settlemethod : 现金支付
     * terid : 91
     * time : 2018-08-28 08:56:52
     * items : [{"itemno":"1847","money":"149.30","name":"小草鱼","price":"15.00","unit":"kg","weight":"9.955","x0":"0.1","x1":"0.2","x2":"0.3","k":"0.4","xcur":"0.5"},{"itemno":"1847","money":"149.30","name":"小草鱼","price":"15.00","unit":"kg","weight":"9.955","x0":"0.1","x1":"0.2","x2":"0.3","k":"0.4","xcur":"0.5"}]
     */

    private String billcode;
    private String billstatus;
    private String seller;
    private int sellerid;
    private String settlemethod;
    private int terid;
    private int marketid;
    private String time;
    private List<ItemsBean> items;

    public String getBillcode() {
        return billcode;
    }

    public void setBillcode(String billcode) {
        this.billcode = billcode;
    }

    public String getBillstatus() {
        return billstatus;
    }

    public void setBillstatus(String billstatus) {
        this.billstatus = billstatus;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public int getSellerid() {
        return sellerid;
    }

    public void setSellerid(int sellerid) {
        this.sellerid = sellerid;
    }

    public String getSettlemethod() {
        return settlemethod;
    }

    public void setSettlemethod(String settlemethod) {
        this.settlemethod = settlemethod;
    }


    public int getTerid() {
        return terid;
    }

    public void setTerid(int terid) {
        this.terid = terid;
    }

    public int getMarketid() {
        return marketid;
    }

    public void setMarketid(int marketid) {
        this.marketid = marketid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }


}
