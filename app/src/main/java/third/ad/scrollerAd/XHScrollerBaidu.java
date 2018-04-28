package third.ad.scrollerAd;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mobad.feeds.NativeResponse;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.StringManager;
import third.ad.tools.BaiduAdTools;

/**
 * PackageName : third.ad.scrollerAd
 * Created by MrTrying on 2017/7/10 13:02.
 * E_mail : ztanzeyu@gmail.com
 */

public class XHScrollerBaidu extends XHScrollerAdParent {
    private Map<String,String> map_data;
    private NativeResponse nativeResponse;
    private boolean isJudgePicSize = false;

    public XHScrollerBaidu(String data,String mAdPlayId,String adPositionId, int num) {
        super(mAdPlayId,adPositionId, num);
        key = "sdk_baidu";
        LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
    }

    @Override
    public void onResumeAd(String oneLevel, String twoLevel) {
        if(null != nativeResponse && null != view){
           //YLKLog.i("tzy","广告展示:::"+XHScrollerAdParent.ADKEY_BAIDU+":::位置::"+twoLevel);
            nativeResponse.recordImpression(view);
            onAdShow(oneLevel,twoLevel,key);
        }
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onThirdClick(String oneLevel, String twoLevel) {
        if(null != nativeResponse && null != view){
           //YLKLog.i("tzy","广告点击:::"+XHScrollerAdParent.ADKEY_BAIDU+":::位置:"+twoLevel);
            nativeResponse.handleClick(view);
            onAdClick(oneLevel,twoLevel,key);
        }
    }

    @Override
    public void getAdDataWithBackAdId(@NonNull final XHAdDataCallBack xhAdDataCallBack) {
        if(!isShow()){
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BAIDU);
            return;
        }
        if(null == nativeResponse){
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BAIDU);
        }
        BaiduAdTools.newInstance().getNativeData(nativeResponse, new BaiduAdTools.OnHandlerDataCallback() {
            @Override
            public void onHandlerData(String title, String desc, String iconUrl, String imageUrl,boolean isBigPic) {
                if(!TextUtils.isEmpty(title)
                        && !TextUtils.isEmpty(desc)
                        && (!TextUtils.isEmpty(imageUrl) || !TextUtils.isEmpty(iconUrl))
                        ){
                    Map<String,String> map= new HashMap<>();
                    if(title.length() > desc.length()){
                        //交换title和desc
                        map.put("title",desc);
                        map.put("desc",title);
                    }else{
                        map.put("title",title);
                        map.put("desc",desc);
                    }
                    map.put("iconUrl",iconUrl);
                    //如果imageUrl为空，则使用iconurl
                    map.put("imgUrl" , TextUtils.isEmpty(imageUrl) ? iconUrl : imageUrl);
                    map.put("type" , XHScrollerAdParent.ADKEY_BAIDU);
                    map.put("isBigPic" , isBigPic ? "2" : "1");
                    map.put("hide","1");//2隐藏，1显示
//                    Log.d("tzy", "XHScrollerBaidu :: map = " + map.toString());
                    if(TextUtils.isEmpty(imageUrl)
                            || (isJudgePicSize && !isBigPic)) {
                        xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BAIDU);
                    }else{
                        map_data=map;
                        xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_BAIDU,map_data);
                    }
                }else
                    xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BAIDU);
            }
        });
    }

    public void setNativeResponse(NativeResponse nativeResponse) {
        this.nativeResponse = nativeResponse;
    }

    public boolean isJudgePicSize() {
        return isJudgePicSize;
    }

    public void setJudgePicSize(boolean judgePicSize) {
        isJudgePicSize = judgePicSize;
    }
}
