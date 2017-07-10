package third.ad.scrollerAd;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.baidu.mobad.feeds.NativeResponse;

import java.util.HashMap;
import java.util.Map;

import third.ad.tools.BaiduAdTools;

/**
 * PackageName : third.ad.scrollerAd
 * Created by MrTrying on 2017/7/10 13:02.
 * E_mail : ztanzeyu@gmail.com
 */

public class XHScrollerBaidu extends XHScrollerAdParent {
    private Map<String,String> map_data;
    private NativeResponse nativeResponse;
    public XHScrollerBaidu(String mAdPlayId, int num) {
        super(mAdPlayId, num);
        key = "sdk_baidu";
    }

    @Override
    public void onResumeAd(String oneLevel, String twoLevel) {
        if(null != nativeResponse && null != view){
            Log.i("tzy","广告展示:::"+XHScrollerAdParent.ADKEY_BAIDU+":::位置::"+twoLevel);
            nativeResponse.recordImpression(view);
            onAdShow(oneLevel,twoLevel,key);
        }
    }

    @Override
    public void onPsuseAd() {

    }

    @Override
    public void onThirdClick(String oneLevel, String twoLevel) {
        if(null == nativeResponse){
            Log.i("tzy","nativeResponseObject为null");
        }
        if(view==null){
            Log.i("tzy","view为null");
        }
        if(null != nativeResponse && null != view){
            Log.i("tzy","广告点击:::"+XHScrollerAdParent.ADKEY_BAIDU+":::位置:"+twoLevel);
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
        BaiduAdTools.newInstance().getNativeData(null, nativeResponse, new BaiduAdTools.AddAdView() {
            @Override
            public void addAdView(String title, String desc, String iconUrl, String imageUrl, View.OnClickListener clickListener) {
                Log.i("tzy", "Baidu NactiveAD addAdView");
                if(!TextUtils.isEmpty(title)&&!TextUtils.isEmpty(imageUrl)){
                    Map<String,String> map= new HashMap<>();
                    map.put("title",title);
                    map.put("desc",desc);
                    map.put("iconUrl",iconUrl);
                    map.put("imgUrl",imageUrl);
                    map.put("type",XHScrollerAdParent.ADKEY_BAIDU);
                    map.put("hide","1");//2隐藏，1显示
                    if(TextUtils.isEmpty(imageUrl)){
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
}
