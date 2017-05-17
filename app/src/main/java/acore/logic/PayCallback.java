package acore.logic;

/**
 * Created by Fang Ruijiao on 2017/3/23.
 */

public class PayCallback {

    private static OnPayCallback mOnPayCallback;

    public static void setPayCallBack(OnPayCallback onPayCallback){
        mOnPayCallback = onPayCallback;
    }

    public static OnPayCallback getPayCallBack(){
        return mOnPayCallback;
    }


    public interface OnPayCallback{
        public void onPay(boolean isOk,Object data);
    }

}
