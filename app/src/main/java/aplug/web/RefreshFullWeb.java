package aplug.web;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

import acore.tools.IObserver;
import acore.tools.ObserverManager;

/**
 * 全屏weview,只提供再次回来后自动刷新功能
 */
public class RefreshFullWeb extends FullScreenWeb implements IObserver {

    private boolean mRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObserverManager.getInstance().registerObserver(this,ObserverManager.NOTIFY_LOGIN, ObserverManager.NOTIFY_UPLOADOVER, ObserverManager.NOTIFY_PAYFINISH, ObserverManager.NOTIFY_SHARE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefresh) {
            mRefresh = false;
            reloadData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
    }

    /**
     * 重新加载数据
     */
    private void reloadData() {
        loadData();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (TextUtils.isEmpty(name) || data == null)
            return;
        switch (name) {
            case ObserverManager.NOTIFY_LOGIN:
                resetRefreShstatus(data);
                break;
            case ObserverManager.NOTIFY_UPLOADOVER:
                resetRefreShstatus(data);
                break;
            case ObserverManager.NOTIFY_PAYFINISH:
                resetRefreShstatus(data);
                break;
            case ObserverManager.NOTIFY_SHARE:
                if (!TextUtils.isEmpty(shareCallback) && data != null) {
                    Map<String, String> dataMap = (Map<String, String>) data;
                    webview.loadUrl("javascript:" + shareCallback + "(" + TextUtils.equals("2", dataMap.get("status")) + "," + "\'" + dataMap.get("callbackParams") + "\'" + ")");
                    Log.i("tzy", "javascript:" + shareCallback + "(" + TextUtils.equals("2", dataMap.get("status")) + "," + "\'" + dataMap.get("callbackParams") + "\'" + ")");
                }
                break;
            default:
                break;
        }
    }

    private void resetRefreShstatus(Object data){
        if (data instanceof Boolean) {
            mRefresh = (boolean) data;
        }
    }
}
