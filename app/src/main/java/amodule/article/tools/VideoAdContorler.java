package amodule.article.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.helper.XHActivityManager;
import third.ad.scrollerAd.XHAllAdControl;

import static amodule.article.adapter.ArticleDetailAdapter.TYPE_KEY;
import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_CONTENT_BOTTOM;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_RECM_1;

/**
 * PackageName : amodule.article.tools
 * Created by MrTrying on 2017/6/19 18:34.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoAdContorler extends ArticleAdContrler{
    //广告跟随相关推荐的数据位置
    public final int ARTICLE_BOTTOM = 101;
    public final int ARTICLE_RECOMMEND = 0;

    @Override
    public void initADData() {
        //请求广告数据
        xhAllAdControlBootom = requestAdData(new String[]{ARTICLE_CONTENT_BOTTOM}, "wz_wz");
        xhAllAdControlList = requestAdData(new String[]{ARTICLE_RECM_1}, "wz_list");
    }

    @Override
    protected XHAllAdControl requestAdData(final String[] ads, String id) {
        ArrayList<String> adData = new ArrayList<>();
        for (String str : ads)
            adData.add(str);
        return new XHAllAdControl(adData, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                for (String key : ads) {
                    String adStr = map.get(key);
                    switch (key) {
                        case ARTICLE_CONTENT_BOTTOM:
                            sendAdMessage(adStr, ARTICLE_BOTTOM);
                            break;
                        case ARTICLE_RECM_1:
                            sendAdMessage(adStr, ARTICLE_RECOMMEND);
                            break;
                    }
                }
            }
        }, XHActivityManager.getInstance().getCurrentActivity(), id);
    }

    /**
     * @param allDataListMap 所有数据
     */
    @Override
    public void handlerAdData(List<Map<String, String>> allDataListMap) {
        if (adRcomDataArray != null && !adRcomDataArray.isEmpty()
                && allDataListMap != null) {
            //循环ad数据
            for (int adIndex = 0, adLength = adRcomDataArray.size(); adIndex < adLength; adIndex++) {
                //验证是否已经插入
                if (adInsteredArray.get(adIndex) != null && adInsteredArray.get(adIndex)) continue;
                //获取广告map
                Map<String, String> adMap = getAdMap(adRcomDataArray.get(adIndex), adIndex);
                //遍历原始数据体插入数据
                //暂时只有一个数据，固定插入第一个位置
                for (int oriDataIndex = 0, allDataSize = allDataListMap.size(); oriDataIndex < allDataSize; oriDataIndex++) {
                    Map<String, String> oriData = allDataListMap.get(oriDataIndex);
                    if (String.valueOf(Type_recommed).equals(oriData.get(TYPE_KEY))
                            && "1".equals(oriData.get("isAd"))) {
                        allDataListMap.add(0, adMap);
                        adInsteredArray.put(adIndex, true);
                        break;
                    }
                }
            }
            if(!allDataListMap.isEmpty()){
                for(int index = 0;index < allDataListMap.size();index++){
                    if(index == 0){
                        allDataListMap.get(index).put("showheader","1");
                    }else{
                        allDataListMap.get(index).remove("showheader");
                    }
                }
            }
        }
    }
}
