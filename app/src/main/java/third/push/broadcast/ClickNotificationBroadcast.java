package third.push.broadcast;

import acore.logic.XHClick;
import third.push.model.NotificationData;
import third.push.model.NotificationEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.umeng.message.UTrack;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class ClickNotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //获取真正的intent跳转Acitivty
        Intent realIntent = intent.getParcelableExtra("realIntent");
        context.startActivity(realIntent);

        //统计
        NotificationData data = new NotificationData();
        data.type = realIntent.getIntExtra("type", 0);
        data.value = realIntent.getStringExtra("value");
        data.url = realIntent.getStringExtra("url");
        data.channel = intent.getStringExtra("channel");
        XHClick.statisticsNotify(context, data, NotificationEvent.EVENT_CLICK);
        //umeng统计
        String message = intent.getStringExtra("umengMessage");
        if (!TextUtils.isEmpty(message)) {
            try {
                UMessage msg = (UMessage) new UMessage(new JSONObject(message));
                UTrack.getInstance(context).setClearPrevMessage(true);
                UTrack.getInstance(context).trackMsgClick(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
