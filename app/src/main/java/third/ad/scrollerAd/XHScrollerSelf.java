package third.ad.scrollerAd;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import third.ad.db.bean.XHSelfNativeData;

/**
 * 自有广告
 */
public class XHScrollerSelf extends XHScrollerAdParent{
    public static final String IMG_KEY = "littleImage";
    private XHSelfNativeData mNativeData = null;

   public XHScrollerSelf(String data, String mAdPlayId, int num) {
        super(mAdPlayId, num);
        key="xh";
       LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
       if (map_link.containsKey("adid")){
           adid = map_link.get("adid");
       }
    }

    @Override
    public void onResumeAd(String oneLevel,String twoLevel) {
        onAdShow(oneLevel,twoLevel,key);
        Log.i("zhangyujian","广告展示:::"+XHScrollerAdParent.ADKEY_BANNER+":::位置::"+twoLevel);
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onThirdClick(String oneLevel,String twoLevel) {
        onAdClick(oneLevel,twoLevel,key);
        if(mNativeData != null){
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),mNativeData.getUrl(),true);
        }
        Log.i("zhangyujian","广告点击:::"+XHScrollerAdParent.ADKEY_BANNER+":::位置::"+twoLevel);
    }

    @Override
    public void getAdDataWithBackAdId(@NonNull final XHAdDataCallBack xhAdDataCallBack) {
        if(!isShow() || mNativeData == null){//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
            return;
        }
        if(!LoginManager.isShowAd()){//特权不能去除活动广告
            if("2".equals(mNativeData.getType())){
                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
                return;
            }
        }
        final Map<String,String> map = new HashMap<>();
        map.put("title", mNativeData.getTitle());
        map.put("desc", mNativeData.getDesc());
        map.put("adType", mNativeData.getType());
        map.put("imgUrl", mNativeData.getBigImage());
        map.put(IMG_KEY, mNativeData.getLittleImage());
        map.put("iconUrl", mNativeData.getLittleImage());
        //TODO
        map.put("appSearchImg", mNativeData.getLittleImage());
        map.put("appHomeImg", mNativeData.getLittleImage());
        map.put("type",XHScrollerAdParent.ADKEY_BANNER);
        map.put("hide", "1");//2隐藏，1显示
        xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_BANNER, map);
    }

    public void setNativeData(XHSelfNativeData nativeData) {
        mNativeData = nativeData;
    }
}
