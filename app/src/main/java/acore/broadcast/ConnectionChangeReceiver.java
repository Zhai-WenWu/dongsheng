package acore.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

/**
 * PackageName : acore
 * Created by MrTrying on 2016/9/29 11:45.
 * E_mail : ztanzeyu@gmail.com
 */

public class ConnectionChangeReceiver extends BroadcastReceiver {
    private ConnectionChangeListener listener;

    public ConnectionChangeReceiver(@NonNull ConnectionChangeListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
            if(netInfo != null && netInfo.isAvailable()) {
                //网络连接
                int type = netInfo.getType();
                switch (type){
                    case ConnectivityManager.TYPE_WIFI:
                        if(listener != null){
                            listener.wifi();
                        }
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        if(listener != null){
                            listener.mobile();
                        }
                        break;
                }
            } else {
                //网络断开
                if(listener != null){
                    listener.disconnect();
                }
            }
        }
    }

    /**监听网络改变对调接口*/
    public interface ConnectionChangeListener{
        void disconnect();
        void wifi();
        void mobile();
    }
}