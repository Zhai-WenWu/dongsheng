package amodule.dish.BrocastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ：airfly on 2016/10/24 18:41.
 */


public class BatteryBrocastReceiver extends BroadcastReceiver {

    BatteryBrocastReceiverCallback callback;

    public BatteryBrocastReceiver(BatteryBrocastReceiverCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {// 判断Action
            int level = intent.getIntExtra("level", 0);// 取得电池剩余容量
            int scale = intent.getIntExtra("scale", 100);// 取得电池总理
            if (callback != null) {
                callback.onGetBatterylevel((int) ((level * 1.0f / scale) * 100));
            }
        }
    }

    public interface BatteryBrocastReceiverCallback {
        void onGetBatterylevel(int level);
    }

}
