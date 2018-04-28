package third.ad.scrollerAd;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.qq.e.ads.nativ.NativeADDataRef;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.override.activity.base.BaseFragmentActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import third.ad.XHAdAutoRefresh;
import third.ad.db.XHAdSqlite;
import third.ad.db.bean.AdBean;
import third.ad.db.bean.XHSelfNativeData;
import third.ad.tools.BaiduAdTools;
import third.ad.tools.GdtAdTools;
import third.ad.tools.XHSelfAdTools;

import static third.ad.control.AdControlHomeDish.tag_yu;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_BANNER;
import static third.ad.scrollerAd.XHScrollerAdParent.TAG_BANNER;

/**
 * 广告主控制类
 */
public class XHAllAdControl implements ActivityMethodManager.IAutoRefresh {
    private ArrayList<String> listIds;//存储广告位置id的集合
    private boolean isShowGdt = false;//是否存在广点通
    private boolean isShowBaidu = false;//是否存在百度
    private ArrayList<XHOneAdControl> listAdContrls = new ArrayList<>();//子控制类集合
    private Map<String, String> AdData = new HashMap<>();//获取到数据集合
    private Map<String, String> AdTypeData = new HashMap<>();//获取到数据集合

    private XHBackIdsDataCallBack xhBackIdsDataCallBack;//外部回调接口数据
    private Activity act;

    private int count = 0;//返回的数据的个数
    private List<NativeADDataRef> gdtNativeArray = new ArrayList<>();
    private List<NativeResponse> baiduNativeArray = new ArrayList<>();
    private ArrayList<XHSelfNativeData> xhNativeArray = new ArrayList<>();

    private String GDT_ID = "";//广点
    private String BAIDU_ID = "";//广点
    private List<String> XH_IDS = new ArrayList<>();
    private int gdt_index = -1;//取广告位的数据位置 GDT
    private int baidu_index = -1;//取广告位的数据位置 Baidu
    private String StatisticKey;
    private String ad_show;//展示一级统计
    private String ad_click;//点击一级统一
    private String twoData;//二级统计
    private boolean isQuanList = false;//是否是生活圈列表结构。
    private int getCountGdtData = 6;//获取广点通数据个数，默认6个

    private long oneAdTime;//第一次请求广告的时间。
    private long lastSelfAdTime;
    private long showTime = 30 * 60 * 1000;//广告的过期时间。30分钟

    private boolean isJudgePicSize = false;
    private boolean isLoadOverGdt = false;
    private boolean isLoadOverBaidu = false;
    private boolean isLoadOverXH = false;


    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull Activity act, String StatisticKey) {
        this(listIds, act, StatisticKey, false);
    }

    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull Activity act, String StatisticKey, boolean isJudgePicSize) {
        this.listIds = listIds;
        this.act = act;
        this.StatisticKey = StatisticKey;
        this.isJudgePicSize = isJudgePicSize;
        if (listIds.size() > 0) getCountGdtData = listIds.size();
    }

    public void start(XHBackIdsDataCallBack callback) {
        this.xhBackIdsDataCallBack = callback;
        loadData();
    }

    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull XHBackIdsDataCallBack xhBackIdsDataCallBack,
                          @NonNull Activity act, String StatisticKey) {
        this(listIds, xhBackIdsDataCallBack, act, StatisticKey, false);
    }

    /**
     * 初始化
     *
     * @param listIds               id列表
     * @param xhBackIdsDataCallBack 回调
     * @param act                   所在的activity
     * @param StatisticKey          统计key
     * @param isJudgePicSize        是否判断大图
     */
    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull XHBackIdsDataCallBack xhBackIdsDataCallBack,
                          @NonNull Activity act, String StatisticKey, boolean isJudgePicSize) {
        this.listIds = listIds;
        this.act = act;
        this.xhBackIdsDataCallBack = xhBackIdsDataCallBack;
        this.StatisticKey = StatisticKey;
        this.isJudgePicSize = isJudgePicSize;
        if (listIds.size() > 0) getCountGdtData = listIds.size();
        loadData();
    }

    private void loadData() {
        getStiaticsData();
        getAllAdDataBySqlite();
    }

    //注册刷新回调
    public void registerRefreshCallback() {
        if (act == null) {
            return;
        }
        lastSelfAdTime = System.currentTimeMillis() - 1000;
        ActivityMethodManager activityMethodManager = null;
        if (act instanceof BaseActivity) {
            activityMethodManager = ((BaseActivity) act).getActMagager();
        } else if (act instanceof BaseAppCompatActivity) {
            activityMethodManager = ((BaseAppCompatActivity) act).getActMagager();
        } else if (act instanceof BaseFragmentActivity) {
            activityMethodManager = ((BaseFragmentActivity) act).getActMagager();
        } else if (act instanceof MainBaseActivity) {
            activityMethodManager = ((MainBaseActivity) act).getActMagager();
        }
        if (activityMethodManager != null) {
            activityMethodManager.registerADController(this);
        }
    }

    public void getAllAdDataBySqlite() {
        resetData();
        XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
        //根据广告位置id在广告数据 进行筛选通
        if (listIds.size() > 0) {
//           //YLKLog.i("tzy", "getAllAdDataBySqlite: size = " + listIds.size());
            for (int i = 0, size = listIds.size(); i < size; i++) {
                AdTypeData.put(listIds.get(i), "");
                /*获取数据广告位的数据体*/
                AdBean adBean = adSqlite.getAdConfig(listIds.get(i));
                if (adBean != null) {
                    /*广告实体数据集合*/
                    boolean state = false;//是否打开
                    ArrayList<Map<String, String>> adConfigList = new ArrayList<>();
                    ArrayList<Map<String, String>> adConfigDataList = StringManager.getListMapByJson(adBean.adConfig);
                    boolean once = false;
                    for (Map<String, String> adTypeConfig : adConfigDataList) {
                        if ("2".equals(adTypeConfig.get("open"))
                                && !TextUtils.isEmpty(adTypeConfig.get("data"))) {
                            state = true;
                            if (!once) {
                                once = true;
                                String adType = adTypeConfig.containsKey("type") ? adTypeConfig.get("type") : "";
                                AdTypeData.put(listIds.get(i), adType);
                            }
                            adConfigList.add(adTypeConfig);
                            judge(adTypeConfig);
                        }
                    }
                    if (!state) {//当前广告位没有打开
                        count++;
                        AdData.put(listIds.get(i), "");
                    }
                    if (state && adConfigList.size() > 0) {
                        initAdRequest(adConfigList, listIds.get(i),adBean.adPositionId, i);
                    } else {
                        listAdContrls.add(null);
                    }
                }
            }
        }
        isLoadOverGdt = !isShowGdt;
        isLoadOverBaidu = !isShowBaidu;
        //是否存在广点通
        getAllData(false);
    }

    private void resetData() {
        isLoadOverGdt = false;
        isLoadOverBaidu = false;
        isLoadOverXH = false;
        listAdContrls.clear();
        XH_IDS.clear();
    }

    /**
     * 判断是否是 gdt 或者 baidu 数据
     *
     * @param map_temp 数据体
     */
    private void judge(Map<String, String> map_temp) {
        String typeValue = map_temp.get("type");
        String isOpenValue = map_temp.get("open");
        if ("2".equals(isOpenValue)) {
            switch (typeValue) {
                case TAG_BANNER:
                    String data = map_temp.get("data");
                    XH_IDS.add(data != null ? data : "");
                    break;
                case "gdt":
                    if (!isShowGdt || TextUtils.isEmpty(GDT_ID)) {
                        String gdtData = map_temp.get("data");
                        LinkedHashMap<String, String> map_link = StringManager.getMapByString(gdtData, "&", "=");
                        if (map_link.containsKey("adid"))
                            GDT_ID = map_link.get("adid");
                    }
                    isShowGdt = true;
                    break;
                case "baidu":
                    if (!isShowBaidu || TextUtils.isEmpty(BAIDU_ID)) {
                        String baiduData = map_temp.get("data");
                        LinkedHashMap<String, String> map_link = StringManager.getMapByString(baiduData, "&", "=");
                        if (map_link.containsKey("adid")) {
                            BAIDU_ID = map_link.get("adid");
                        }
                    }
                    isShowBaidu = true;
                    break;
                default:
                    break;
            }
        }
    }

    private void getAllData(boolean isRefresh) {
        getAllXhData(isRefresh);
        getAllGdtData();
        getAllBaiduData();
    }

    private void getAllXhData(boolean isRefresh) {
        if (isLoadOverXH) {
            return;
        }
        XHSelfAdTools.getInstance().loadNativeData(XH_IDS, new XHSelfAdTools.XHSelfCallback() {
            @Override
            public void onNativeLoad(ArrayList<XHSelfNativeData> list) {
                isLoadOverXH = true;
                if (isRefresh) {
                    if (xhNativeArray != null && !xhNativeArray.isEmpty()
                            && list != null
                            && !xhNativeArray.equals(list)
                            ) {
                        count=0;
                        xhNativeArray.clear();
                        xhNativeArray.addAll(list);
                        handlerAdData(isRefresh);
                    }
                } else {
                    xhNativeArray = list;
                    handlerAdData(isRefresh);
                }
            }

            @Override
            public void onNativeFail() {
                isLoadOverXH = true;
                handlerAdData(isRefresh);
            }
        });
    }

    /** GDT广告获取 */
    private void getAllGdtData() {
        if (isLoadOverGdt) {
            return;
        }
        GdtAdTools.newInstance().loadNativeAD(XHApplication.in(), GDT_ID, getCountGdtData,
                new GdtAdTools.GdtNativeCallback() {

                    @Override
                    public void onNativeLoad(List<NativeADDataRef> data) {
                        isLoadOverGdt = true;
                        gdtNativeArray = data;
                        handlerAdData(false);
                    }

                    @Override
                    public void onNativeFail(NativeADDataRef nativeADDataRef, String msg) {
                        isLoadOverGdt = true;
                        handlerAdData(false);
                    }

                    @Override
                    public void onADStatusChanged(NativeADDataRef nativeADDataRef) {
                    }
                });
    }

    /** Baidu广告获取 */
    private void getAllBaiduData() {
        if (isLoadOverBaidu) {
            return;
        }
        BaiduAdTools.newInstance().loadNativeAD(XHApplication.in(), BAIDU_ID, new BaiduAdTools.BaiduNativeCallbck() {
            @Override
            public void onNativeLoad(List<NativeResponse> list) {
                isLoadOverBaidu = true;
                baiduNativeArray = list;
                handlerAdData(false);
            }

            @Override
            public void onNativeFail(NativeErrorCode nativeErrorCode) {
                isLoadOverBaidu = true;
                handlerAdData(false);
            }
        });
    }

    private void handlerAdData(boolean isRefresh) {
        if (isLoadOverGdt && isLoadOverBaidu && isLoadOverXH) {
            startAdRequest(isRefresh);
        }
    }

    /**
     * 创建单条管理类的请求事例
     *
     * @param arrayList 广告实体类的数据
     */
    private void initAdRequest(@NonNull ArrayList<Map<String, String>> arrayList, String backIds,String adPositionId, int num) {
        ArrayList<XHScrollerAdParent> adParentArrayList = new ArrayList<>();
        final int length = arrayList.size();
        for (int i = 0; i < length; i++) {
            Map<String, String> map = arrayList.get(i);
            String key = map.get("type");
            String data = map.get("data");
            XHScrollerAdParent parent = null;
            //判断当前类型
            switch (key) {
                case "gdt":
                    parent = new XHScrollerGdt(data, backIds,adPositionId, i);
                    break;
                case TAG_BANNER:
                    parent = new XHScrollerSelf(data, backIds,adPositionId, i);
                    break;
                case "baidu":
                    parent = new XHScrollerBaidu(data, backIds,adPositionId, i);
                    ((XHScrollerBaidu) parent).setJudgePicSize(isJudgePicSize);
                    break;
                default:
                    break;
            }
            if (parent != null) {
                if (isQuanList)
                    parent.setIsQuanList(isQuanList);
                adParentArrayList.add(parent);
            }
        }
        XHOneAdControl xhOneAdControl = new XHOneAdControl(adParentArrayList, backIds, num);
        listAdContrls.add(xhOneAdControl);
    }

    /** 开启单条数据 */
    private void startAdRequest(boolean isRefresh) {
        int size = listAdContrls.size();
        if (size > 0)
            oneAdTime = System.currentTimeMillis();//第一时间。
        if (size <= 0) {
            if (xhBackIdsDataCallBack != null)
                xhBackIdsDataCallBack.callBack(isRefresh, AdData);
            return;
        }
        for (int i = 0; i < size; i++) {
            if (listAdContrls.get(i) != null) {
                listAdContrls.get(i).setAdDataCallBack(new XHAdControlCallBack() {
                    @Override
                    public void onSuccess(String type, Map<String, String> map, int num) {
                        resetAdContrlDisplay(map, num);
                        if (map != null) {
                            map.put("index", String.valueOf(num));
                        }
                        count++;

                        AdData.put(listIds.get(num), mapToJson(map).toString());
                        //展示数据集合
                        if (count >= listAdContrls.size()) {
                            xhBackIdsDataCallBack.callBack(isRefresh, AdData);
                        }
                    }

                    @Override
                    public void onFail(String type, int num) {
                        if (count < listAdContrls.size() && listAdContrls.get(count) != null) {
                            listAdContrls.get(count).resetDispaly();
                        }
                        count++;
                        AdData.put(listIds.get(num), "");
                        //展示数据集合
                        if (count >= listAdContrls.size()) {
                            try {
                                xhBackIdsDataCallBack.callBack(isRefresh, AdData);
                            } catch (Exception e) {
                               //YLKLog.i("tzy", "Exception : " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public NativeADDataRef onGdtNativeData() {
                        if (gdtNativeArray != null && gdtNativeArray.size() > 0 && gdtNativeArray
                                .size() > gdt_index + 1) {
                            NativeADDataRef temp = gdtNativeArray.get(++gdt_index);
                            return temp;
                        }
                        return null;
                    }

                    @Override
                    public NativeResponse onBaiduNativeData() {
                        if (baiduNativeArray != null && baiduNativeArray.size() > 0 &&
                                baiduNativeArray.size() > baidu_index + 1) {
                            NativeResponse temp = baiduNativeArray.get(++baidu_index);
                            return temp;
                        }
                        return null;
                    }

                    @Override
                    @Nullable
                    public XHSelfNativeData onXHNativeData(int position) {
                        if (xhNativeArray != null && position < xhNativeArray.size()) {
                            return xhNativeArray.get(position);
                        }
                        return null;
                    }
                });
            }
        }
    }

    /**
     * 重置广告展示标记
     *
     * @param map
     * @param num
     */
    private void resetAdContrlDisplay(Map<String, String> map, int num) {
        if (AdData != null && !AdData.isEmpty()
                && listIds != null && listIds.size() > num
                && num > -1) {
            Map<String, String> originalMap = null;
            String adDataValue = AdData.get(listIds.get(num));
            if (!TextUtils.isEmpty(adDataValue)) {
                originalMap = StringManager.getFirstMap(adDataValue);
            }
            if (count < listAdContrls.size()
                    && listAdContrls.get(count) != null
                    && !isDataEqual(map, originalMap)) {
//               //YLKLog.i("tzy", "resetAdContrlDisplay: ");
                listAdContrls.get(count).resetDispaly();
            }
        }
    }

    /**
     * 判断广告数据是否变更
     *
     * @param newMap
     * @param originalMap
     *
     * @return
     */
    private boolean isDataEqual(Map<String, String> newMap, Map<String, String> originalMap) {
//       //YLKLog.i("tzy", "isDataEqual: 111");
        if (newMap != null && originalMap != null) {
//           //YLKLog.i("tzy", "isDataEqual: 222");
            if (ADKEY_BANNER.equals(newMap.get("type"))
                    && ADKEY_BANNER.equals(originalMap.get("type"))) {
//               //YLKLog.i("tzy", "isDataEqual: 333" + (TextUtils.equals(newMap.get("id"), originalMap.get("id"))
//                        && TextUtils.equals(newMap.get("updateTime"), originalMap.get("updateTime"))));
                return TextUtils.equals(newMap.get("id"), originalMap.get("id"))
                        && TextUtils.equals(newMap.get("updateTime"), originalMap.get("updateTime"));
            } else {
//               //YLKLog.i("tzy", "isDataEqual: 444" + (newMap.equals(originalMap)));
                return newMap.equals(originalMap);
            }
        }
//       //YLKLog.i("tzy", "isDataEqual: 555" + (newMap == null && originalMap == null));
        return newMap == null && originalMap == null;
    }

    /**
     * 广告被点击
     *
     * @param index     广告位置在数据的位置
     * @param listIndex 广告在真实数据的位置-----一个数据传""
     */
    public void onAdClick(int index, String listIndex) {
        listAdContrls.get(index).onAdClick(ad_click, twoData + listIndex);
    }

    /**
     * 广告被点击
     *
     * @param view      广告view
     * @param index     广告位置在数据的位置
     * @param listIndex 广告在真实数据的位置-----一个数据传""
     */
    public void onAdClick(View view, int index, String listIndex) {
        if (listAdContrls != null
                && index >= 0
                && listAdContrls.size() > index) {
            XHOneAdControl control = listAdContrls.get(index);
            if (control == null)
                return;
            if (control.getAdViewState())
                control.setView(view);
            control.onAdClick(ad_click, twoData + listIndex);
        }
    }

    /**
     * 广告曝光----必须在返回数据后调用该方法（重要）
     *
     * @param index     广告位置在数据的位置
     * @param view      广告显示的view
     * @param listIndex 广告位置---一个数据传""
     */
    public void onAdBind(int index, View view, String listIndex) {
        if (listAdContrls != null
                && index >= 0
                && listAdContrls.size() > index) {
            XHOneAdControl control = listAdContrls.get(index);
            if (control == null)
                return;
            control.onAdBind(view, ad_show, twoData + listIndex);
        }
    }

    public interface XHAdControlCallBack {
        public void onSuccess(String type, Map<String, String> map, int num);

        public void onFail(String type, int num);

        XHSelfNativeData onXHNativeData(int position);

        public NativeADDataRef onGdtNativeData();

        public NativeResponse onBaiduNativeData();
    }

    public interface XHBackIdsDataCallBack {
        public void callBack(boolean isRefresh, Map<String, String> map);
    }

    /**
     * @param map 数据
     *
     * @return @return json对象
     */
    private JSONObject mapToJson(Map<String, String> map) {
        try {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            return jsonObject;
        } catch (Exception e) {
           //YLKLog.i("tzy", "Exception : " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取广告统计层级数据
     */
    private void getStiaticsData() {
        String msg = FileManager.getFromAssets(XHApplication.in(), "adStatistics");
        Map<String, String> statisticsMap = StringManager.getFirstMap(msg);
        if (!statisticsMap.isEmpty() && statisticsMap.containsKey(StatisticKey)) {
            Map<String, String> configMap = StringManager.getFirstMap(statisticsMap.get(StatisticKey));
            ad_show = configMap.get("show");
            ad_click = configMap.get("click");
            twoData = configMap.get("twoData");
        }
        //判断当前是否是美食圈列表结构
        if ("community_list".equals(StatisticKey)
                || "result_works".equals(StatisticKey)) {
            isQuanList = true;
        }
    }

    /**
     * 是否需要刷新
     *
     * @return
     */
    public boolean isNeedRefersh() {
        long nowTime = System.currentTimeMillis();
       //YLKLog.i(tag_yu, "nowTime:::" + nowTime + ":::" + oneAdTime + "：：：" + (nowTime - oneAdTime) + ":::" + showTime);
        if (nowTime - oneAdTime >= showTime) {//当前广告已过期
            return true;
        }
        return false;
    }

    public void refreshAllAd() {
        isLoadOverXH = false;
        isLoadOverBaidu = !isShowBaidu;
        isLoadOverGdt = !isShowGdt;
        getAllData(true);
    }

    public void refreshSelfAd() {
        isLoadOverXH = false;
        gdt_index = -1;
        baidu_index = -1;
        getAllXhData(true);
    }

    @Override
    public void autoRefreshSelfAD() {
        final long noeTime = System.currentTimeMillis();
        if (noeTime - lastSelfAdTime >= XHAdAutoRefresh.intervalTime) {
            lastSelfAdTime = System.currentTimeMillis();
            refreshSelfAd();
        }
    }

    public boolean isJudgePicSize() {
        return isJudgePicSize;
    }

    public void setJudgePicSize(boolean judgePicSize) {
        isJudgePicSize = judgePicSize;
    }

    public List<NativeADDataRef> getGdtNativeArray() {
        return LoginManager.isShowAd() ? gdtNativeArray : new ArrayList<NativeADDataRef>();
    }

    public List<NativeResponse> getBaiduNativeArray() {
        return LoginManager.isShowAd() ? baiduNativeArray : new ArrayList<NativeResponse>();
    }

    public Map<String, String> getAdTypeData() {
        return AdTypeData;
    }
}
