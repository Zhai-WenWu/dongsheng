package third.ad.scrollerAd;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.qq.e.ads.nativ.NativeADDataRef;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.StringManager;
import third.ad.tools.GdtAdTools;

/**
 * GDT广告
 */
public class XHScrollerGdt extends XHScrollerAdParent {
    private Map<String, String> map_data;
    private NativeADDataRef nativeADDataRef;
    public XHScrollerGdt(String data,String mAdPlayId,String adPositionId, int num) {
        super(mAdPlayId,adPositionId, num);
        key = "sdk_gdt";
        LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
        if (map_link.containsKey("adid"))
            adid = map_link.get("adid");
    }

    @Override
    public void onResumeAd(String oneLevel, String twoLevel) {
        if (null != nativeADDataRef && null != view) {
            Log.i("tzy", "广告展示:::" + XHScrollerAdParent.ADKEY_GDT + ":::位置::" + twoLevel);
            nativeADDataRef.onExposured(view);
            onAdShow(oneLevel, twoLevel, key);
        }
    }

    @Override
    public void onPsuseAd() {
    }

    @Override
    public void onThirdClick(String oneLevel, String twoLevel) {
        if (null == nativeADDataRef) {
            Log.i("tzy", "nativeResponseObject为null");
            return;
        }
        if (view == null) {
            Log.i("tzy", "view为null");
            return;
        }
        Log.i("tzy", "广告点击:::" + XHScrollerAdParent.ADKEY_GDT + ":::位置:" + twoLevel);
        nativeADDataRef.onClicked(view);
        onAdClick(oneLevel, twoLevel, key);
    }

    @Override
    public void getAdDataWithBackAdId(final XHAdDataCallBack xhAdDataCallBack) {
        if (!isShow()) {//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_GDT);
            return;
        }
        if (null == nativeADDataRef) {
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_GDT);
        }
        GdtAdTools.newInstance().getNativeData(null, nativeADDataRef,
                GdtAdTools.newInstance().new AddGgView() {
                    @Override
                    public void addAdView(String title, String desc, String iconUrl,
                                          String imageUrl, View.OnClickListener clickListener) {
//                        Log.i("tzy", "GDT NactiveAD onHandlerData");
                        if((!TextUtils.isEmpty(title) || !TextUtils.isEmpty(desc))
                                && (!TextUtils.isEmpty(imageUrl) || !TextUtils.isEmpty(iconUrl))
                                ){
                            Map<String, String> map = new HashMap<>();
                            if(TextUtils.isEmpty(title)){
                                map.put("title",desc);
                                map.put("desc",desc);
                            }else if(TextUtils.isEmpty(desc)){
                                map.put("title",title);
                                map.put("desc",title);
                            }else{
                                //交换title和desc
                                map.put("title",title.length() > desc.length() ? desc : title);
                                map.put("desc",title.length() > desc.length()?title:desc);
                            }
                            map.put("title", title);
                            map.put("desc", desc);
                            map.put("iconUrl", iconUrl);
                            map.put("imgUrl", imageUrl);
                            map.put("type", XHScrollerAdParent.ADKEY_GDT);
                            map.put("hide", "1");//2隐藏，1显示
                            map.put("adid", adid);
                            if (TextUtils.isEmpty(imageUrl)) {
                                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_GDT);
                            } else {
                                map_data = map;
                                xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_GDT, map_data);
                            }
                        } else
                            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_GDT);
                    }
                });
    }

    public void setGdtData(NativeADDataRef data) {
        this.nativeADDataRef = data;
    }
}
