package amodule.dish.share.module;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import acore.tools.StringManager;
import amodule.dish.share.module.listener.DataListener;
import amodule.dish.share.module.listener.ShareConfData;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by sll on 2018/1/3.
 */

public class ShareConfDataController implements DataListener, ShareConfData {

    private DataListener mDataListener;
    private Map<String, String> mDataMap;

    public ShareConfDataController() {
        mDataMap = new HashMap<>();
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

    @Override
    public Map<String, String> getMap(Object object) {
        if (!mDataMap.isEmpty())
            return mDataMap;
        Map<String, String> temp = StringManager.getFirstMap(object);
        String shareType = temp.get("shareType");
        String shareConf = temp.get("shareConfig");
        if (TextUtils.isEmpty(shareType) || TextUtils.isEmpty(shareConf))
            return null;
        Map<String, String> confMap = StringManager.getFirstMap(shareConf);
        Map<String, String> dataMap = StringManager.getFirstMap(confMap.get(shareType));
        dataMap.put("shareType", shareType);
        mDataMap.clear();
        mDataMap.putAll(dataMap);
        return mDataMap;
    }
}
