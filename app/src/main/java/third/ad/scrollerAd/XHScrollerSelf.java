package third.ad.scrollerAd;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;

/**
 * 自有广告
 */
public class XHScrollerSelf extends XHScrollerAdParent{
    private String data;
    private Map<String, String> map_link;
    private Activity activity;
    public XHScrollerSelf(String data, String mAdPlayId, int num, Activity activity) {
        super(mAdPlayId, num);
        this.data= data;
        this.activity=activity;
        map_link = StringManager.getFirstMap(data);
        key="xh";

    }
   public XHScrollerSelf(String data, String mAdPlayId, int num) {
        super(mAdPlayId, num);
        this.data= data;
        map_link = StringManager.getFirstMap(data);
        key="xh";

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
//        AppCommon.openUrl(activity,map_link.get("url"),true);
        AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),map_link.get("url"),true);
        Log.i("zhangyujian","广告点击:::"+XHScrollerAdParent.ADKEY_BANNER+":::位置::"+twoLevel);
    }

    @Override
    public void getAdDataWithBackAdId(final XHAdDataCallBack xhAdDataCallBack) {
        if(!isShow()){//判断是否显示---不显示
            xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
            return;
        }
        if(!LoginManager.isShowAd()){//特权不能去除活动广告
            if(map_link.containsKey("adType")&&!"1".equals(map_link.get("adType"))){//1为活动，2广告
                xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
                return;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(map_link!=null) {
                    map_link.put("type",XHScrollerAdParent.ADKEY_BANNER);
                    Map<String,String> map = new HashMap<>();
                    map.put("title", map_link.get("name"));
                    map.put("desc",  map_link.get("subhead"));
                    map.put("adType",map_link.get("adType"));
                    String imgUrl= "";
                    String imgUrl2= "";
                    String imgUrl3= "";
                    if(map_link.containsKey("imgs")){
                        Map<String,String> temp = StringManager.getFirstMap(map_link.get("imgs"));
                        imgUrl= temp.get("appImg");
                        imgUrl2= temp.get("appHomeImg");
                        imgUrl3= temp.get("appSearchImg");
                    }
                    if(!TextUtils.isEmpty(map_link.get("name"))){//不缺少数据才是成功的状态
                        map.put("appImg", imgUrl);
                        //自动选择图片---默认样式首页大图
                        if(!TextUtils.isEmpty(imgUrl2))map.put("imgUrl", imgUrl2);
                            else if(!TextUtils.isEmpty(imgUrl3))map.put("imgUrl", imgUrl3);
                        else map.put("imgUrl", imgUrl);

                        map.put("appHomeImg", imgUrl2);
                        map.put("appSearchImg", imgUrl3);
                        map.put("iconUrl", imgUrl);
                        map.put("type",XHScrollerAdParent.ADKEY_BANNER);
                        xhAdDataCallBack.onSuccees(XHScrollerAdParent.ADKEY_BANNER, map);
                    }else
                        xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
                }else
                    xhAdDataCallBack.onFail(XHScrollerAdParent.ADKEY_BANNER);
            }
        }).start();

    }
}
