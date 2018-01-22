package amodule.search.view;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.tools.StringManager;
import acore.tools.Tools;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdPlayIdConfig;

/**
 * Created by ：fei_teng on 2017/3/28 17:29.
 */

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

    private XHAllAdControl xhAllAdControl;
    private ArrayList<Map<String, String>> list = new ArrayList<>();

    private SearchResultAdDataProvider() {
    }

    private static SearchResultAdDataProvider instance;
    private AtomicBoolean topItemHasData = new AtomicBoolean(false);

    public static SearchResultAdDataProvider getInstance() {

        if (instance == null) {
            synchronized (SearchResultAdDataProvider.class) {
                if (instance == null) {
                    instance = new SearchResultAdDataProvider();
                }
            }
        }
        return instance;
    }


    public void getAdData(){

        final ArrayList<String> adPosList = new ArrayList<>();
        Collections.addAll(adPosList, AD_IDS);

        xhAllAdControl = new XHAllAdControl(adPosList, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                list.clear();
                if (map != null && map.size() > 0) {
                    for (int i = 0;i<adPosList.size();i++) {
                        String adStr = map.get(adPosList.get(i));
                        if (!TextUtils.isEmpty(adStr)) {
                            ArrayList<Map<String, String>> adList = StringManager.getListMapByJson(adStr);
                            if (adList != null && adList.size() > 0) {
                                Map<String, String> adDataMap = adList.get(0);
                                //自由广告，取搜索图片
                                if(adDataMap.containsKey("type")&& XHScrollerAdParent.ADKEY_BANNER.equals(adDataMap.get("type"))
                                        &&adDataMap.containsKey("appSearchImg")&&!TextUtils.isEmpty(adDataMap.get("appSearchImg"))){
                                    adDataMap.put("imgUrl",adDataMap.get("appSearchImg"));
                                }
                                adDataMap.put("allClick", String.valueOf(Tools.getRandom(4000,10000)));
                                list.add(adDataMap);
                            }
                        }else{
                            Map<String, String> adDataMap = new HashMap<>();
                            list.add(adDataMap);
                        }

                        //处理搜索列表顶部广告
                        if (i == 0) {
                            if (list.size() > 0)
                                topItemHasData.set(list.size() > 0);
                        }

                    }
                }
            }
        }, "search_list",false);
    }

    public ArrayList<Map<String,String>> getAdDataList(){
        return list;
    }

    public XHAllAdControl getXhAllAdControl(){
        return xhAllAdControl;
    }

    public AtomicBoolean HasTopAdData(){
        return topItemHasData;
    }

}
