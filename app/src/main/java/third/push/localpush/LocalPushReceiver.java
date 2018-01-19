package third.push.localpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by sll on 2018/1/19.
 */

public class LocalPushReceiver extends BroadcastReceiver {

    public static final String TAG = LocalPushReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        String tag = intent.getStringExtra(LocalPushManager.TAG_MANAGER);
        if (TextUtils.equals(tag, TAG))
            LocalPushManager.pushNotification(context, intent);
    }
}
