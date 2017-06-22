
package amodule.user.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by ：fei_teng on 2016/12/30 10:50.
 */

public class UploadStateChangeBroadcasterReceiver extends BroadcastReceiver {



    private ReceiveBack callback;
    public static final String ACTION = "uploadState";
    public static final String STATE_KEY = "state";
    public static final String STATE_SUCCESS = "success";
    public static final String STATE_FAIL = "fail";
    public static final String STATE_SUSPEND = "suspend";
    public static final String DATA_TYPE = "dataType";

    //详情页的删除操作
    public static final String ACTION_DEL = "actionDel";//2:删除成功
    //详情页的关注操作
    public static final String ACTION_ATT = "actionAtt";//表示“关注”操作

    public static final String SECONDE_EDIT = "secondEdit";//是否二次编辑 1：否 2：是


    public UploadStateChangeBroadcasterReceiver(ReceiveBack callback){
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        callback.onGetReceive(intent);
    }


    public void register(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        context.registerReceiver(this,intentFilter);
    }

    public interface ReceiveBack{
        void onGetReceive(Intent intent);
    }

}


