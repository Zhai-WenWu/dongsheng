package third.cling.service.callback;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;

import java.util.Map;

import third.cling.config.Config;
import third.cling.config.Intents;
import third.cling.util.Utils;

/**
 * 说明：
 *
 * 日期：15/7/17 AM11:33
 */

public class AVTransportSubscriptionCallback  extends BaseSubscriptionCallback {

    private static final String TAG = AVTransportSubscriptionCallback.class.getSimpleName();
    private ActionCallback mCallback;

    public AVTransportSubscriptionCallback(org.fourthline.cling.model.meta.Service service, Context context) {
        super(service, context);
    }

    @Override
    protected void eventReceived(GENASubscription subscription) { // 这里进行 事件接收处理
        if (Utils.isNull(mContext))
            return;

        Map values = subscription.getCurrentValues();
        if (values != null && values.containsKey("LastChange")) {
            String lastChangeValue = values.get("LastChange").toString();
            Log.i(TAG, "LastChange:" + lastChangeValue);
            doAVTransportChange(lastChangeValue);
        }
    }

    private void doAVTransportChange(String lastChangeValue) {
        try {
            LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), lastChangeValue);

            //Parse TransportState value.
            AVTransportVariable.TransportState transportState = lastChange.getEventedValue(0, AVTransportVariable.TransportState.class);
            if (transportState != null) {
                TransportState ts = transportState.getValue();
                if (ts == TransportState.PLAYING) {
                    Log.i(TAG, "PLAYING");
                    if (mCallback != null)
                        mCallback.action(Intents.ACTION_PLAYING);
                    return;
                } else if (ts == TransportState.PAUSED_PLAYBACK) {
                    Log.i(TAG, "PAUSED_PLAYBACK");
                    if (mCallback != null)
                        mCallback.action(Intents.ACTION_PAUSED_PLAYBACK);
                    return;
                } else if (ts == TransportState.STOPPED) {
                    Log.i(TAG, "STOPPED");
                    if (mCallback != null)
                        mCallback.action(Intents.ACTION_STOPPED);
                    return;
                } else if (ts == TransportState.TRANSITIONING) { // 转菊花状态
                    Log.i(TAG, "BUFFER");
                    if (mCallback != null)
                        mCallback.action(Intents.ACTION_TRANSITIONING);
                    return;
                }
            }

            //RelativeTimePosition
            String position = "00:00:00";
            AVTransportVariable.RelativeTimePosition eventedValue = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class);
            if (Utils.isNotNull(eventedValue)) {
                position = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class).getValue();
                int intTime = Utils.getIntTime(position);
                Log.i(TAG, "position: " + position + ", intTime: " + intTime);

                // 该设备支持进度回传
                Config.getInstance().setHasRelTimePosCallback(true);

                Intent intent = new Intent(Intents.ACTION_POSITION_CALLBACK);
                intent.putExtra(Intents.EXTRA_POSITION, intTime);
                mContext.sendBroadcast(intent);

                // TODO: 17/7/20 ACTION_PLAY_COMPLETE 播完了

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setActionCallback(ActionCallback callback) {
        this.mCallback = callback;
    }

}
