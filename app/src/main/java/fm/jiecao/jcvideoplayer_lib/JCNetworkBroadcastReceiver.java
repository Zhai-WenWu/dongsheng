package fm.jiecao.jcvideoplayer_lib;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import acore.tools.ToolsDevice;

/**
 * 网络更改时候调用，加SDK里面确实很奇葩
 * 
 * @author sunxiao
 * 
 */
public class JCNetworkBroadcastReceiver extends BroadcastReceiver {
    public final static String TYPE_WIFI = "wifi";
    public final static String TYPE_MOBILE = "mobile";
    public final static String TYPE_NOTHING = "null";

    private static final String TAG = "NetworkBroadcastReceiver";
    private List<NetworkNotifyListener> mNotifyListeners = new ArrayList<NetworkNotifyListener>();
    private String currentType = "";

    public JCNetworkBroadcastReceiver(Context context) {
        currentType = getNetWorkState(context);
    }

    /**
     *
     * @param context
     * @return
     */
    private String getNetWorkState(Context context){
        String netWorkState = ToolsDevice.getNetWorkSimpleType(context);
        switch (netWorkState){
            case TYPE_WIFI:
                return TYPE_WIFI;
            case TYPE_NOTHING:
                return TYPE_NOTHING;
            default:
                return TYPE_MOBILE;
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        State wifiState = null;
        State mobileState = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.w(TAG, "onReceive -- ConnectivityManager is null!");
            return;
        }
        NetworkInfo networkInfo = null;
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            wifiState = networkInfo.getState();
        }
        try {
            networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (networkInfo != null) {
            mobileState = networkInfo.getState();
        }
        Log.d(TAG, "onReceive -- wifiState = " + wifiState + " -- mobileState = " + mobileState);
        if (wifiState != null && mobileState != null && State.CONNECTED != wifiState && State.CONNECTED == mobileState) {
            Log.d(TAG, "onReceive -- 手机网络连接成功");
            // 手机网络连接成功
            if(!TYPE_MOBILE.equals(currentType))
                mobileNotify();
            currentType = TYPE_MOBILE;
        } else if (wifiState != null && mobileState != null && State.CONNECTED != wifiState
                && State.CONNECTED != mobileState) {
            Log.d(TAG, "onReceive -- 手机没有任何的网络");
            // 手机没有任何的网络
            if(!TYPE_NOTHING.equals(currentType))
                nothingNotify();
            currentType = TYPE_NOTHING;
        } else if (wifiState != null && State.CONNECTED == wifiState) {
            Log.d(TAG, "onReceive -- 无线网络连接成功");
            // 无线网络连接成功
            if(!TYPE_WIFI.equals(currentType))
                wifiNotify();
            currentType = TYPE_WIFI;
        }

    }

    public void wifiNotify() {
        Log.d("tzy", "wifiNotify : hashCode = " + this.hashCode());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.wifiConnected();
        }
    }

    public void mobileNotify() {
        Log.d("tzy", "mobileNotify : hashCode = " + this.hashCode());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.mobileConnected();
        }
    }

    public void nothingNotify() {
        Log.d("tzy", "nothingNotify : hashCode = " + this.hashCode());
        for (NetworkNotifyListener listener : mNotifyListeners) {
            listener.nothingConnected();
        }
    }

    public void addListener(NetworkNotifyListener listener) {
        mNotifyListeners.add(listener);
    }

    public void removeListener(NetworkNotifyListener listener) {
        mNotifyListeners.remove(listener);
    }

    public void destory() {
        mNotifyListeners.clear();
    }

    public interface NetworkNotifyListener {

        void wifiConnected();

        void mobileConnected();

        void nothingConnected();
    }

}