package amodule.home;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.StringManager;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetAdController;
import amodule._common.delegate.StatisticCallback;
import amodule._common.plugin.WidgetVerticalLayout;
import amodule._common.utility.WidgetUtility;
import amodule.main.activity.MainHomePage;
import third.ad.scrollerAd.XHAllAdControl;

import static amodule._common.helper.WidgetDataHelper.KEY_SORT;
import static amodule._common.helper.WidgetDataHelper.KEY_WIDGET_DATA;
import static third.ad.tools.AdPlayIdConfig.HOME_BANNEER_LIST;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 21:47.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeHeaderControler implements ISaveStatistic, ISetAdController {

    private View mHeaderView, mFeedHeaderView,mLine;

    private LinearLayout mFeedLayout;

    private TextView mFeedTitle;

    private WidgetVerticalLayout[] mLayouts = new WidgetVerticalLayout[6];

    private View.OnLayoutChangeListener onLayoutChangeListener;

    private XHAllAdControl mAdController;

    private List<Map<String, String>> mDatas;

    private boolean hasFeedData = false;
    private boolean hasHeaderData = false;
    private boolean mSettingAdData = false;
    private boolean mSetttingRemoteData = false;
    private boolean mIsShowCache = false;

    HomeHeaderControler(View header) {
        this.mHeaderView = header;
        //banner
        mLayouts[0] = (WidgetVerticalLayout) header.findViewById(R.id.banner_widget);
        mLayouts[0].setAdID(Arrays.asList(HOME_BANNEER_LIST));
        //功能导航 4按钮
        mLayouts[1] = (WidgetVerticalLayout) header.findViewById(R.id.funcnav1_widget);
        //功能导航 2按钮
        mLayouts[2] = (WidgetVerticalLayout) header.findViewById(R.id.funcnav2_widget);
        //横向滑动
        mLayouts[3] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal1_widget);
        mLayouts[4] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal2_widget);
        mLayouts[5] = (WidgetVerticalLayout) header.findViewById(R.id.horizontal3_widget);

        mFeedHeaderView = header.findViewById(R.id.a_home_feed_title);
        mFeedTitle = (TextView) header.findViewById(R.id.feed_title);
        mLine = header.findViewById(R.id.line);
        mFeedLayout = (LinearLayout) header.findViewById(R.id.feed_title_layout);
    }

    public void setData(List<Map<String, String>> array, boolean isShowCache) {
        mDatas = array;
        mIsShowCache = isShowCache;
        mSetttingRemoteData = !isShowCache;
        handleData();
    }

    public void setVisibility(boolean isShow) {
        for (WidgetVerticalLayout itemLayout : mLayouts) {
            itemLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void saveStatisticData(String page) {
        for (WidgetVerticalLayout layout : mLayouts) {
            layout.saveStatisticData(page);
        }
    }

    void setFeedheaderVisibility(boolean hasFeedData) {
        this.hasFeedData = hasFeedData;
        mFeedHeaderView.setVisibility((hasFeedData && hasHeaderData) ? View.VISIBLE : View.GONE);
    }

    void setFeedTitleText(String text) {
        WidgetUtility.setTextToView(mFeedTitle, text);
    }

    private List<String> mAdIDs;
    public void setAdID(List<String> adIDs) {
        mAdIDs = adIDs;
    }

    private Map<String,String> mAdData;
    public void setAdData(Map<String,String> adData, boolean refresh) {
        mAdData = adData;
        mSettingAdData = true;
        if (!refresh)
            handleData();
        else
            handleBannerAdDataRefresh();
    }

    private void handleBannerAdDataRefresh() {
        startHandleData(true);
        setViewData(mIsShowCache);
    }

    private void startHandleData(boolean adRefresh) {
        if (mDatas != null && !mDatas.isEmpty()) {
            Map<String, String> map = mDatas.get(0);
            if (TextUtils.equals("1", map.get("widgetType"))) {
                mDatas.set(0, handleBannerData(map, adRefresh));
            } else if (mAdData != null && !mAdData.isEmpty()) {
                Map<String, String> adMap = combineBannerAdMap(mAdData);
                mDatas.add(0, adMap);
            }
        } else if (mAdData != null && !mAdData.isEmpty()){
            mDatas = new ArrayList<>();
            Map<String, String> adMap = combineBannerAdMap(mAdData);
            mDatas.add(adMap);
        }
    }

    private void handleData() {
        if (mIsShowCache) {
            setViewData(mIsShowCache);
            mIsShowCache = false;
            return;
        }
        if (mSetttingRemoteData && mSettingAdData) {
            startHandleData(false);
            setViewData(mIsShowCache);
            mSetttingRemoteData = false;
            mSettingAdData = false;
            mIsShowCache = false;
        }
    }

    private void setViewData(boolean isShowCache) {
        if (null == mDatas || mDatas.isEmpty()) {
            setVisibility(false);
            return;
        }
        String[] twoLevelArray = {"轮播banner", "功能入口", "功能入口", "精品厨艺", "限时抢购", "精选菜单"};
        String[] threeLevelArray = {"轮播banner位置", "", "", "精品厨艺位置", "限时抢购位置", "精选菜单位置"};
        final int length = Math.min(mDatas.size(), mLayouts.length);
        for (int i = 0, x = 0; i < length; i++) {
            final int index = i;
            Map<String, String> map = mDatas.get(index);

            if (isShowCache && "1".equals(map.get("cache"))) {
                mLayouts[index].setVisibility(View.GONE);
                continue;
            }
            String widgetData = map.get(KEY_WIDGET_DATA);
            Map<String, String> dataMap = StringManager.getFirstMap(widgetData);
            if (dataMap.containsKey(KEY_SORT)) {
                mLayouts[index].setShowIndex(x);
                x ++;
            }
            mLayouts[index].setStatisticPage("home");
            mLayouts[index].setAdController(mAdController);
            mLayouts[index].setData(map);
            mLayouts[index].setStatictusData(MainHomePage.STATICTUS_ID_HOMEPAGE, twoLevelArray[index], threeLevelArray[index]);
            StatisticCallback statisticCallback = (id, twoLevel, threeLevel, position) -> {
                if(!TextUtils.isEmpty(id)&&!TextUtils.isEmpty(twoLevel)&&!TextUtils.isEmpty(threeLevel)){
                    XHClick.mapStat(mLayouts[index].getContext(),id,twoLevel,threeLevel);
                }
            };
            mLayouts[index].setStatisticCallback(statisticCallback);
            mLayouts[index].setTitleStaticCallback(statisticCallback);
            mLayouts[index].setVisibility(View.VISIBLE);
        }
        if(onLayoutChangeListener == null){
            onLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                hasHeaderData = mHeaderView.getHeight() > mFeedHeaderView.getHeight();
                int needVisibility = (hasFeedData && hasHeaderData) ? View.VISIBLE : View.GONE;
                if(needVisibility != mFeedHeaderView.getVisibility()){
                    mFeedHeaderView.setVisibility(needVisibility);
                    mLine.setVisibility(needVisibility);
                    mFeedLayout.setVisibility(needVisibility);
                }
            };
            mHeaderView.addOnLayoutChangeListener(onLayoutChangeListener);
        }
    }

    private Map<String, String> handleBannerData(Map<String, String> map, boolean refresh) {
        Map<String, String> retMap = new HashMap<>();
        if (mAdData == null || mAdData.isEmpty())
            return retMap;
        retMap.putAll(map);
        String widgetDataValue = retMap.get("widgetData");
        Map<String, String> wdMap = StringManager.getFirstMap(widgetDataValue);
        wdMap.put("sort", "1");
        String dataValue = wdMap.get("data");
        Map<String, String> listMap = StringManager.getFirstMap
                (dataValue);
        ArrayList<Map<String, String>> listValue = StringManager.getListMapByJson(listMap.get
                ("list"));
        if (refresh) {
            Iterator<Map<String, String>> listIterator = listValue.iterator();
            while (listIterator.hasNext()) {
                Map<String, String> valueMap = listIterator.next();
                if (valueMap != null && !valueMap.isEmpty() && (valueMap.containsKey("adType") || TextUtils.equals("xh", valueMap.get("adType")))) {
                    listIterator.remove();
                }
            }
        }
        addAdMapToList(listValue, mAdData);
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> lv : listValue) {
            JSONObject object = map2JSON(lv);
            jsonArray.put(object);
        }
        listMap.put("list", jsonArray.toString());
        wdMap.put("data", map2JSON(listMap).toString());
        retMap.put("widgetData", map2JSON(wdMap).toString());
        return retMap;
    }

    private ArrayList<Map<String, String>> addAdMapToList(ArrayList<Map<String, String>> targetList, Map<String, String> originalMap) {
        if (originalMap == null || originalMap.isEmpty())
            return targetList;
        for (String key : originalMap.keySet()) {
            Map<String, String> adMap = new HashMap<>();
            adMap.put("adPosId", key);
            Map<String, String> m = StringManager.getFirstMap(originalMap.get(key));
            if(!"xh".equals(m.get("type"))){
                continue;
            }
            adMap.put("img", m.get("imgUrl"));
            adMap.put("hide", m.get("hide"));
            adMap.put("iconUrl", m.get("iconUrl"));
            adMap.put("title", m.get("title"));
            adMap.put("desc", m.get("desc"));
            adMap.put("index", m.get("index"));
            adMap.put("type", m.get("type"));
            adMap.put("adType", m.get("adType"));
            targetList.add(adMap);
        }
        return targetList;
    }

    private JSONObject map2JSON(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private Map<String, String> combineBannerAdMap(Map<String, String> adMap) {
        JSONObject obj = new JSONObject();
        JSONObject extraObj = new JSONObject();
        JSONObject dataObj = new JSONObject();
        JSONObject dataListObj = new JSONObject();
        JSONObject parameterObj = new JSONObject();

        JSONArray dataArr = new JSONArray();
        JSONArray extraArr_top = new JSONArray();
        JSONArray extraArr_bottom = new JSONArray();
        JSONArray dataListArr = new JSONArray();

        try {
            dataListObj.put("img", "");
            dataListObj.put("url", "");
            dataListObj.put("weight", "");
            dataListObj.put("code", "");
            dataListObj.put("type", "");
            dataListObj.put("adData", StringManager.getJsonByMap(adMap));
            dataListArr.put(dataListObj);

            dataObj.put("style", "1");
            dataObj.put("data", dataListArr);
            dataObj.put("appFixed", "2");
            dataObj.put("sort", "1");
            dataArr.put(dataObj);

            extraObj.put("top", extraArr_top);
            extraObj.put("bottom", extraArr_bottom);

            parameterObj.put("scalar", "");
            obj.put("widgetExtra", extraObj);
            obj.put("widgetData", dataArr);
            obj.put("widgetParameter", parameterObj);
            obj.put("widgetType", "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return StringManager.getFirstMap(obj);
    }

    @Override
    public void setAdController(XHAllAdControl controller) {
        mAdController = controller;
    }
}
