
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



    public UploadStateChangeBroadcasterReceiver(ReceiveBack callback){
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(STATE_KEY);
        String dataType = intent.getStringExtra(DATA_TYPE);
        callback.onGetReceive(state, dataType);
    }


    public void register(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        context.registerReceiver(this,intentFilter);
    }

    public interface ReceiveBack{
        void onGetReceive(String state, String dataType);
    }

}


