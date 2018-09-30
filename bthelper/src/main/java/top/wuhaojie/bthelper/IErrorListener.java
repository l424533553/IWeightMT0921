package top.wuhaojie.bthelper;

/**
 * Created by wuhaojie on 2016/9/8 16:57.
 */
public interface IErrorListener {
    void onError(Exception e);
    void onConnected(BtHelperClient.STATUS mCurrStatus);
}
