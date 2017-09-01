package aplug.web;

import android.os.Bundle;
import android.text.TextUtils;

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
        ObserverManager.getInstence().registerObserver(this,ObserverManager.NOTIFY_REFRESH_H5);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstence().unRegisterObserver(this);
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
            case ObserverManager.NOTIFY_REFRESH_H5:
                resetRefreShstatus(data);
                break;
            case ObserverManager.NOTIFY_PAYFINISH:
                resetRefreShstatus(data);
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
