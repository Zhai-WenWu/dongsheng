package amodule.dish.share.module;

import android.text.TextUtils;

import acore.tools.StringManager;
import amodule.dish.share.module.listener.DataListener;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by sll on 2018/1/3.
 */

public class ShareConfDataController implements DataListener {

    private DataListener mDataListener;

    public ShareConfDataController() {
    }

    public void loadData(String dishCode) {
        if (TextUtils.isEmpty(dishCode))
            return;
        onLoadData();
        String params = "dishCode=" + dishCode;
        ReqEncyptInternet.in().doEncypt(StringManager.API_MAIN8_SHARE_CONF, params, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                onDataReady(i, s, o);
            }
        });
    }

    public void setOnDataListener(DataListener listener) {
        mDataListener = listener;
    }

    @Override
    public void onLoadData() {
        if (mDataListener != null)
            mDataListener.onLoadData();
    }

    @Override
    public void onDataReady(int flag, String type, Object object) {
        if (mDataListener != null)
            mDataListener.onDataReady(flag, type, object);
    }
}
