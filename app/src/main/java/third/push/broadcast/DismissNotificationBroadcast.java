package third.push.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.umeng.message.UTrack;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;

import acore.logic.XHClick;
import third.push.model.NotificationData;

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
        XHClick.statisticsNotify(context, data, "dismiss");
        //umeng统计
        String message = intent.getStringExtra("umengMessage");
        if(!TextUtils.isEmpty(message)){
            try {
                UMessage msg = (UMessage) new UMessage(new JSONObject(message));
                UTrack.getInstance(context).setClearPrevMessage(true);
                UTrack.getInstance(context).trackMsgDismissed(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
