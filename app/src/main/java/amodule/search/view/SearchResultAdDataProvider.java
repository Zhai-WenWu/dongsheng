package amodule.search.view;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.tools.AdPlayIdConfig;

public class SearchResultAdDataProvider {
    public static final String[] AD_IDS = new String[]{
            AdPlayIdConfig.SEARCH_LIST_TOP,
            AdPlayIdConfig.SEARCH_LIST_1,
            AdPlayIdConfig.SEARCH_LIST_2,
            AdPlayIdConfig.SEARCH_LIST_3,
            AdPlayIdConfig.SEARCH_LIST_4,
            AdPlayIdConfig.SEARCH_LIST_5,
            AdPlayIdConfig.SEARCH_LIST_6,
    };

    BaseActivity mActivity;

    public SearchResultAdDataProvider(BaseActivity activity){
        mActivity = activity;
    }

    private XHAllAdControl xhAllAdControl;
    private ArrayList<Map<String, String>> list = new ArrayList<>();

    private AtomicBoolean topItemHasData = new AtomicBoolean(false);

    private OnAutoRefreshCallback mAutoRefreshCallback;

    public void getAdData() {

        final ArrayList<String> adPosList = new ArrayList<>();
        Collections.addAll(adPosList, AD_IDS);

        if(xhAllAdControl != null){
            if(mActivity != null && mActivity.getActMagager() != null){
                mActivity.getActMagager().unregisterADController(xhAllAdControl);
            }
        }
        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh, Map<String, String> map) {
                list.clear();
                if (map != null && map.size() > 0) {
                    for (int i = 0; i < adPosList.size(); i++) {
                        String adStr = map.get(adPosList.get(i));
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                Map<String, String> adDataMap = adList.get(0);
//                                    adDataMap.put("allClick", String.valueOf(Tools.getRandom(4000, 10000)));
                                list.add(adDataMap);
                            }
                        } else {
                            Map<String, String> adDataMap = new HashMap<>();
                            list.add(adDataMap);
                        }

                        //处理搜索列表顶部广告
                        if (i == 0 && list.size() > 0) {
                            topItemHasData.set(list.size() > 0);
                        }
                    }
                }
                if(isRefresh && mAutoRefreshCallback != null){
                    mAutoRefreshCallback.autoRefresh();
                }
            }
        }, mActivity, "search_list", false);
        xhAllAdControl.registerRefreshCallback();
    }

    public ArrayList<Map<String, String>> getAdDataList() {
        return list;
    }

    public XHAllAdControl getXhAllAdControl() {
        return xhAllAdControl;
    }

    public AtomicBoolean HasTopAdData() {
        return topItemHasData;
    }

    public interface OnAutoRefreshCallback{
        void autoRefresh();
    }

    public void setAutoRefreshCallback(OnAutoRefreshCallback autoRefreshCallback) {
        mAutoRefreshCallback = autoRefreshCallback;
    }
}
