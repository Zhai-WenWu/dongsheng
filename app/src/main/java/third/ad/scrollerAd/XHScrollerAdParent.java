package third.ad.scrollerAd;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import third.ad.tools.AdConfigTools;

/**
 * 广告父类
 */
public abstract class XHScrollerAdParent {
    public static final String ADKEY_GDT = "sdk_gdt";
    public static final String ADKEY_BAIDU = "sdk_baidu";
    public static final String ADKEY_BANNER = "xh";

    public static final String TAG_GDT = "gdt";
    public static final String TAG_BANNER = "personal";
    public static final String TAG_BAIDU = "baidu";

    public static final int ID_AD_ICON_GDT = R.id.icon_ad_gdt;
    public static final int ID_AD_ICON_BAIDU = R.id.icon_ad_baidu;

    public int num;//当前存在的位置--针对的是一个广告位
    public String mAdPlayId = "";//广告位置id
    protected String adid = "";
    public int index;//当前存在的位置---针对于广集合的位置
    public View view;
    public String key = "";
    private boolean isQuanList = false;

    private ExecuteStatisticCallback mExecuteStatisticCallback;

    public XHScrollerAdParent(String mAdPlayId, int num) {
        this.mAdPlayId = mAdPlayId;
        this.num = num;
    }

    /**
     * 广告曝光，onResume时调用
     */
    public abstract void onResumeAd(String oneLevel, String twoLevel);

    /**
     * 广告不显示：onPause时调用
     */
    public abstract void onPsuseAd();

    /**
     * 获取广告数据
     *
     * @param xhAdDataCallBack 请求数据回调
     */
    public abstract void getAdDataWithBackAdId(@NonNull XHAdDataCallBack xhAdDataCallBack);

    /**
     * 第三方点击
     */
    public abstract void onThirdClick(String oneLevel, String twoLevel);

    /**
     * 设置当地展示的view
     *
     * @param view
     */
    public void setShowView(View view) {
        if (this.view != null)
            this.view = null;
        this.view = view;
    }

    public View getShowView(){
        return view;
    }

    //释放view，避免内存泄漏
    public void realseView() {
        this.view = null;
    }

    /**
     * 获取当前状态view状态
     *
     * @return false view不为null,true view 为null
     */
    public boolean getViewState() {
        if (view != null)
            return false;
        return true;
    }

    public void setIndexControl(int index) {
        this.index = index;
    }

    protected void onAdClick(String oneLevel, String twoLevel, String threeLevel) {
        postTongji("click");
        //umeng的统计
        XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
        //自己网站的统计
        if (isQuanList)
            //美食圈列表
            AdConfigTools.getInstance().clickAds(mAdPlayId);
        else
            //其他统计
            AdConfigTools.getInstance().clickAds(mAdPlayId, key, "0");
        //统计点击
        if (!ADKEY_BANNER.equals(key)) {
            XHClick.track(XHApplication.in(), "点击广告");
        }
    }

    /**
     * 广告展示统计
     */
    protected void onAdShow(String oneLevel, String twoLevel, String threeLevel) {
        //自己网站上的统计

        postTongji("show");
        if (mExecuteStatisticCallback != null)
            mExecuteStatisticCallback.execute();
        XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
    }


    protected void postTongji(String event) {
        AdConfigTools.getInstance().postStatistics(event,mAdPlayId,key,adid);
    }

    public String getRealKey(String origalKey){
        switch (origalKey){
            case ADKEY_BAIDU: return TAG_BAIDU;
            case ADKEY_BANNER: return TAG_BANNER;
            case ADKEY_GDT:return TAG_GDT;
            default:return "";
        }
    }

    /**
     * 请求数据回调
     */
    public interface XHAdDataCallBack {
        public void onSuccees(String type, Map<String, String> map);

        public void onFail(String type);
    }

    /**
     * 设置当前是否数据集合
     *
     * @param state
     */
    public void setIsQuanList(boolean state) {
        this.isQuanList = state;
    }

    /**
     * 判断是否显示
     *
     * @return
     */
    public boolean isShow() {
        //xh自有广告全部显示
        return ADKEY_BANNER.equals(key) ? true : LoginManager.isShowAd();
    }

    /**
     * 判断当前是否支持该广告类型
     *
     * @param type
     *
     * @return
     */
    public static boolean supportType(String type) {
        if (!TextUtils.isEmpty(type)
                && (XHScrollerAdParent.TAG_GDT.equals(type)
                    || XHScrollerAdParent.TAG_BANNER.equals(type)
                    || XHScrollerAdParent.TAG_BAIDU.equals(type)
                    )
                ) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前广告尺寸
     *
     * @param type---广告类型
     * @param viewTag           ----外部要显示的图片样式 1-大图，2-小图
     */
    public static Map<String, String> getAdImageSize(String type, String viewTag) {
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(viewTag)) return null;
        Map<String, String> map = new HashMap<>();
        if (ADKEY_GDT.equals(type)) {//广点通
            map.put("width", "1280");
            map.put("height", "720");
            return map;
        } else if (ADKEY_BANNER.equals(type)) {//xh自己的广告
            if ("1".equals(viewTag)) {
                map.put("width", "750");
                map.put("height", "464");
            } else if ("2".equals(viewTag)) {
                map.put("width", "240");
                map.put("height", "180");
            }
            return map;
        }
        return null;
    }

    public interface ExecuteStatisticCallback {
        void execute();
    }

    public void setExecuteStatisticCallback (ExecuteStatisticCallback callback) {
        mExecuteStatisticCallback = callback;
    }
}
