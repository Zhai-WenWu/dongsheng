package third.ad.scrollerAd;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import third.ad.tools.TencenApiAdTools;

/**
 * 腾讯API广告
 */
public class XHScrollerTencentApi extends XHScrollerAdParent {

    //    private Activity activity;
    private String loid;
    private Map<String, String> mapData;

    public XHScrollerTencentApi(Activity activity, String data, String mAdPlayId, int num) {
        super(mAdPlayId, num);
//        this.activity = activity;
        LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        if (map_link.containsKey("loid"))
            loid = map_link.get("loid");
        key = "api_tfp";
    }

    public XHScrollerTencentApi(String data, String mAdPlayId, int num) {
        super(mAdPlayId, num);
        LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
        if (map_link.containsKey("loid"))
            loid = map_link.get("loid");
        key = "api_tfp";
    }

    @Override
    public void onResumeAd(String oneLevel, String twoLevel) {
        if (mapData != null
                && mapData.containsKey("tjShowUrl")) {
            Log.i("zhangyujian", "广告展示:::" + XHScrollerAdParent.ADKEY_API + ":::位置::" + twoLevel);
            TencenApiAdTools.onShowAd(XHApplication.in(), mapData.get("tjShowUrl"));
            onAdShow(oneLevel, twoLevel, key);
        }
    }

    @Override
    public void onPsuseAd() {
    }

    @Override
    public void onThirdClick(String oneLevel, String twoLevel) {
        if (mapData != null
                && mapData.containsKey("tjClickUrl")
                && mapData.containsKey("clickUrl")) {
            Log.i("zhangyujian", "广告点击:::" + XHScrollerAdParent.ADKEY_API + ":::位置::" + twoLevel);
//            TencenApiAdTools.onClickAd(activity,mapData.get("clickUrl"),mapData.get("tjClickUrl"));
            TencenApiAdTools.onClickAd(XHActivityManager.getInstance().getCurrentActivity(), mapData.get("clickUrl"), mapData.get("tjClickUrl"));
            onAdClick(oneLevel, twoLevel, key);
        }
    }

    @Override
    public void getAdDataWithBackAdId(@NonNull final XHAdDataCallBack xhAdDataCallBack) {
        if (!isShow()) {//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_API);
            return;
        }
        if (TextUtils.isEmpty(adid) || TextUtils.isEmpty(loid)) {//数据为null
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_API);
            return;
        }
        TencenApiAdTools.getTencenApiAdTools().getApiAd(XHApplication.in(), adid, loid,
                new TencenApiAdTools.OnTencenAdCallback() {
                    String content, title, imgUrl, tjShowUrl, clickUrl, tjClickUrl;

                    @Override
                    public void onAdShow(ArrayList<Map<String, String>> listReturn) {
                        if (listReturn != null && listReturn.size() > 0) {
                            for (int i = 0; i < listReturn.size(); i++) {
                                Map<String, String> mAdMap = listReturn.get(i);
                                if (mAdMap == null || mAdMap.size() < 1)
                                    continue;
                                String seatbid = mAdMap.get("seatbid");
                                Log.i("zhangyujian3", seatbid.toString());
                                ArrayList<Map<String, String>> array = StringManager.getListMapByJson(seatbid);
                                if (array.size() > 0) {
                                    Map<String, String> map = array.get(0);
                                    String bid = map.get("bid");
                                    array = StringManager.getListMapByJson(bid);
                                    if (array.size() > 0) {
                                        map = array.get(0);
                                        String ext = map.get("ext");
                                        array = StringManager.getListMapByJson(ext);
                                        if (array.size() > 0) {
                                            map = array.get(0);
                                            String aurl = map.get("aurl");
                                            array = StringManager.getListMapByJson(aurl);
                                            JSONArray jsonArray = new JSONArray();
                                            if (array.size() > 0) {
                                                Map<String, String> aurlMap = array.get(0);
                                                imgUrl = aurlMap.get("");
                                                if (array.size() > 1) {
                                                    for (int j = 0; j < array.size(); j++) {
                                                        jsonArray.put(array.get(j).get(""));
                                                    }
                                                }
                                            }
                                            //点击的监测地址数组(最多三个）
                                            tjClickUrl = map.get("cmurl");
                                            // 点击后跳转地址 【宏替换】
                                            clickUrl = map.get("curl");
                                            //曝光监测地址数组（最多五个）
                                            tjShowUrl = map.get("murl");
                                            title = map.get("title");
                                            content = map.get("text");

                                            //拼装数据集合
                                            mAdMap.put("tjClickUrl", tjClickUrl);
                                            mAdMap.put("clickUrl", clickUrl);
                                            mAdMap.put("tjShowUrl", tjShowUrl);
                                            mAdMap.put("title", title);
                                            mAdMap.put("desc", content);
                                            mAdMap.put("imgUrl", imgUrl);
                                            mAdMap.put("iconUrl", imgUrl);
                                            mAdMap.put("stype", map.get("stype"));
                                            mAdMap.put("type", XHScrollerAdParent.ADKEY_API);
                                            mAdMap.put("hide", "1");//2隐藏，1显示
                                            if (jsonArray.length() > 1)
                                                mAdMap.put("imgs", jsonArray.toString());
                                            if ((!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) && !TextUtils.isEmpty(imgUrl)) {
                                                if (TextUtils.isEmpty(title)) {
                                                    mAdMap.put("title", content);
                                                }
                                                mapData = mAdMap;
                                                xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_API, mAdMap);
                                            } else {
                                                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_API);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onAdFail() {
                        xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_API);
                    }
                });
    }
}
