package third.push.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import acore.logic.XHClick;
import third.push.model.NotificationData;
import third.push.model.NotificationEvent;

/**
 * Created by mitic_xue on 16/10/26.
 */
public class DismissNotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //统计
        NotificationData data = new NotificationData();
        data.type = intent.getIntExtra("type",0);
        data.value = intent.getStringExtra("value");
        data.url = intent.getStringExtra("url");
        data.channel = intent.getStringExtra("channel");
        XHClick.statisticsNotify(context, data, NotificationEvent.EVENT_DISMISS);
        //umeng统计
        String message = intent.getStringExtra("umengMessage");
        //TODO 统计通知消失
    }

}
