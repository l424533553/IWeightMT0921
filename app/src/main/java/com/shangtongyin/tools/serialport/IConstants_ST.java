package com.shangtongyin.tools.serialport;

/**
 * author: luofaxin
 * date： 2018/8/31 0031.
 * email:424533553@qq.com
 * describe:
 */
public interface IConstants_ST {

    //     0:默认值没有选着打印机
    int PrintMode_NORMAL = 0;
    //      1"佳博2120TF"
    int PrintMode_GP = 1;

    //       2 "美团打印机"
    int PrintMode_MT = 2;

    //        3 "商通打印机"
    int PrintMode_ST = 3;

    //      4 "香山打印机
    int PrintMode_XS = 4;


    /**
     * 称重更新了
     */
    int NOTIFY_WEIGHT = 9111;

    String BASE_IP_ST = "http://119.23.43.64/";


    /**
     * 市场 id
     */
    String MARKET_ID = "marketid";
    /**
     * 秤编号id
     */
    String TID = "tid";

    String SELLER = "seller";
    String SELLER_ID = "sellerid";



//    private int marketid;
//    private String marketname;
//    private String companyno;
//    private int tid;
//    private String seller;
//    private int sellerid;


}
