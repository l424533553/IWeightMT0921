package com.axecom.iweight.my.entity;

/**
 * author: luofaxin
 * date： 2018/9/26 0026.
 * email:424533553@qq.com
 * describe:
 */
public class UserInfo {
    /**
     * marketid : 1
     * marketname : 黄田市场
     * companyno : B070
     * tid : 101
     * seller : 郭金龙
     * sellerid : 127
     * key : null
     * mchid : null
     */

    private int marketid;
    private String marketname;
    private String companyno;
    private int tid;
    private String seller;
    private int sellerid;
    private Object key;
    private Object mchid;

    public int getMarketid() {
        return marketid;
    }

    public void setMarketid(int marketid) {
        this.marketid = marketid;
    }

    public String getMarketname() {
        return marketname;
    }

    public void setMarketname(String marketname) {
        this.marketname = marketname;
    }

    public String getCompanyno() {
        return companyno;
    }

    public void setCompanyno(String companyno) {
        this.companyno = companyno;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
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

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getMchid() {
        return mchid;
    }

    public void setMchid(Object mchid) {
        this.mchid = mchid;
    }
}
