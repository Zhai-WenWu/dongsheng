package third.ad.scrollerAd;

import android.app.Activity;
import android.support.annotation.NonNull;
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

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import third.ad.tools.BaiduAdTools;
import third.ad.tools.GdtAdTools;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * 广告主控制类
 */
public class XHAllAdControl {
    private ArrayList<String> listIds;//存储广告位置id的集合
    private Map<String, String> mapBase = new HashMap<>();
    private boolean isShowGdt = false;//是否存在广点通
    private boolean isShowBaidu = false;//是否存在百度
    private ArrayList<XHOneAdControl> listAdContrls = new ArrayList<>();//子控制类集合
    private Map<String, String> AdData = new HashMap<>();//获取到数据集合

    private XHBackIdsDataCallBack xhBackIdsDataCallBack;//外部回调接口数据
    private Activity act;

    private int count = 0;//返回的数据的个数
    private List<NativeADDataRef> gdtNativeArray = new ArrayList<>();
    private List<NativeResponse> baiduNativeArray = new ArrayList<>();
    private String GDT_ID = "";//广点
    private String BAIDU_ID = "";//广点
    private int gdt_index = 0;//取广告位的数据位置 GDT
    private int baidu_index = 0;//取广告位的数据位置 Baidu
    private String StatisticKey;
    private String ad_show;//展示一级统计
    private String ad_click;//点击一级统一
    private String twoData;//二级统计
    private boolean isQuanList = false;//是否是生活圈列表结构。
    private int getCountGdtData = 6;//获取广点通数据个数，默认6个

    private boolean isNeedRefersh = false;//是否需要刷新
    private long oneAdTime;//第一次请求广告的时间。
    public long showTime = 30 * 60 * 1000;//广告的过期时间。30分钟

    private boolean isJudgePicSize = false;
    private boolean isLoadOverGdt = false;
    private boolean isLoadOverBaidu = false;

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
        getStiaticsData();
        getAllAdData();
    }

    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull XHBackIdsDataCallBack xhBackIdsDataCallBack,
                          @NonNull Activity act, String StatisticKey) {
        this(listIds, xhBackIdsDataCallBack, act, StatisticKey, false);
    }

    public XHAllAdControl(@NonNull ArrayList<String> listIds, @NonNull XHBackIdsDataCallBack xhBackIdsDataCallBack,
                          String StatisticKey, boolean isJudgePicSize) {
        this(listIds, xhBackIdsDataCallBack, null, StatisticKey, isJudgePicSize);
    }

    /**
     * 获取当前要显示的广告
     */
    private void getAllAdData() {
        listAdContrls.clear();
        /*服务端返回的广告数据信息*/
        String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
        Map<String, String> map = StringManager.getFirstMap(data);
        //根据广告位置id在广告数据 进行筛选通
        if (listIds.size() > 0 && !map.isEmpty()) {
            for (int i = 0, size = listIds.size(); i < size; i++) {
                /*获取数据广告位的数据体*/
                if (map.containsKey(listIds.get(i))) {
                    /*存储数据*/
                    mapBase.put(listIds.get(i), map.get(listIds.get(i)));
                    Map<String, String> mapTemp = StringManager.getFirstMap(map.get(listIds.get(i)));
//                    ArrayList<Map<String, String>> listTemp = StringManager.getListMapByJson(map.get(listIds.get(i)));
                    if (mapTemp.containsKey("adConfig")) {
                        String banner = mapTemp.get("banner");

                        /*广告实体数据集合*/
                        boolean state = false;//是否打开
                        ArrayList<Map<String, String>> adConfigDataList = new ArrayList<>();
                        Map<String, String> configMap = StringManager.getFirstMap(mapTemp.get("adConfig"));
                        final String[] keys = {"1", "2", "3", "4", "5"};
                        for (String key : keys) {
                            if (configMap.containsKey(key)) {
                                String value = configMap.get(key);
                                if ("2".equals(StringManager.getFirstMap(value).get("open"))) {
                                    state = true;
                                }
                                handlerAdData(value, adConfigDataList, banner);
                            }
                        }
                        if (!state) {//当前广告位没有打开
                            count++;
                            AdData.put(listIds.get(i), "");
                        }
                        if (adConfigDataList.size() > 0) {
                            initAdRequest(adConfigDataList, listIds.get(i), i);
                        } else {
                            listAdContrls.add(null);
                        }
                    }
                }
            }
        }
        isLoadOverGdt = !isShowGdt;
        isLoadOverBaidu = !isShowBaidu;
        //是否存在广点通
        if ((isShowGdt && !TextUtils.isEmpty(GDT_ID))
                || (isShowBaidu && !TextUtils.isEmpty(BAIDU_ID))) {
            getAllData();
        } else {
            startAdRequest();
        }
    }

    /**
     * 处理当广告体数据的拆分判断
     *
     * @param adData    广告体
     * @param arrayList 存储广告体的集合
     * @param data      banner 的广告数据
     */
    private void handlerAdData(String adData, ArrayList<Map<String, String>> arrayList, String data) {
        Map<String, String> map_ad = StringManager.getFirstMap(adData);
        if ("2".equals(map_ad.get("open"))
                && XHScrollerAdParent.supportType(map_ad.get("type"))) {
            /*banner广告数据存储到广告体*/
            if ("personal".equals(map_ad.get("type")))
                map_ad.put("data", data);
            arrayList.add(map_ad);
        }
        judge(map_ad);
    }

    /**
     * 判断是否是 gdt 或者 baidu 数据
     *
     * @param map_temp 数据体
     */
    private void judge(Map<String, String> map_temp) {
        if ("gdt".equals(map_temp.get("type"))
                && "2".equals(map_temp.get("open"))) {
            if (!isShowGdt || TextUtils.isEmpty(GDT_ID)) {
                String data = map_temp.get("data");
                LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
                if (map_link.containsKey("adid"))
                    GDT_ID = map_link.get("adid");
            }
            isShowGdt = true;
        } else if ("baidu".equals(map_temp.get("type"))
                && "2".equals(map_temp.get("open"))) {
            if (!isShowBaidu || TextUtils.isEmpty(BAIDU_ID)) {
                String data = map_temp.get("data");
                LinkedHashMap<String, String> map_link = StringManager.getMapByString(data, "&", "=");
                if (map_link.containsKey("adid"))
                    BAIDU_ID = map_link.get("adid");
            }
            isShowBaidu = true;
        }
    }

    private void getAllData() {
        getAllGdtData();
        getAllBaiduData();
    }

    /**
     * GDT广告获取
     */
    private void getAllGdtData() {
        if (isLoadOverGdt) return;
        GdtAdTools.newInstance().loadNativeAD(XHApplication.in(), GDT_ID, getCountGdtData,
                new GdtAdTools.GdtNativeCallback() {

                    @Override
                    public void onNativeLoad(List<NativeADDataRef> data) {
                        isLoadOverGdt = true;
                        gdtNativeArray = data;
//                        int num = gdtNativeArray.size();
                        if (isLoadOverGdt && isLoadOverBaidu)
                            startAdRequest();
                    }

                    @Override
                    public void onNativeFail(NativeADDataRef nativeADDataRef, String msg) {
                        isLoadOverGdt = true;
                        if (isLoadOverGdt && isLoadOverBaidu)
                            startAdRequest();
                    }

                    @Override
                    public void onADStatusChanged(NativeADDataRef nativeADDataRef) {
                    }
                });
    }

    private void getAllBaiduData() {
        if (isLoadOverBaidu) return;
        BaiduAdTools.newInstance().loadNativeAD(XHApplication.in(), BAIDU_ID, new BaiduAdTools.BaiduNativeCallbck() {
            @Override
            public void onNativeLoad(List<NativeResponse> list) {
                isLoadOverBaidu = true;
                baiduNativeArray = list;
                if (isLoadOverGdt && isLoadOverBaidu)
                    startAdRequest();
            }

            @Override
            public void onNativeFail(NativeErrorCode nativeErrorCode) {
                isLoadOverBaidu = true;
                if (isLoadOverGdt && isLoadOverBaidu)
                    startAdRequest();
            }
        });
    }

    /**
     * 创建单条管理类的请求事例
     *
     * @param arrayList 广告实体类的数据
     */
    private void initAdRequest(@NonNull ArrayList<Map<String, String>> arrayList, String backIds, int num) {
        ArrayList<XHScrollerAdParent> adParentArrayList = new ArrayList<>();
        final int length = arrayList.size();
        for (int i = 0; i < length; i++) {
            Map<String, String> map = arrayList.get(i);
            String key = map.get("type");
            String data = map.get("data");
            XHScrollerAdParent parent = null;
            //判断当前类型
            switch (key) {
                case "api":
                    parent = new XHScrollerTencentApi(act, data, backIds, i);
                    break;
                case "gdt":
                    parent = new XHScrollerGdt(backIds, i);
                    break;
                case "personal":
                    parent = new XHScrollerSelf(data, backIds, i, act);
                    break;
                case "baidu":
                    parent = new XHScrollerBaidu(backIds, i);
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
    private void startAdRequest() {
        int size = listAdContrls.size();
        if (size > 0)
            oneAdTime = System.currentTimeMillis();//第一时间。
        for (int i = 0; i < size; i++) {
            if (listAdContrls.get(i) != null) {
                listAdContrls.get(i).setAdDataCallBack(new XHAdControlCallBack() {
                    @Override
                    public void onSuccess(String type, Map<String, String> map, int num) {
                        if (map != null)
                            map.put("index", String.valueOf(num));
                        count++;

                        AdData.put(listIds.get(num), mapToJson(map).toString());
                        //展示数据集合
                        if (count >= listIds.size()) {
                            xhBackIdsDataCallBack.callBack(AdData);
                        }
                    }

                    @Override
                    public void onFail(String type, int num) {
                        count++;
                        AdData.put(listIds.get(num), "");
                        //展示数据集合
                        if (count >= listIds.size()) {
                            try {
                                xhBackIdsDataCallBack.callBack(AdData);
                            } catch (Exception e) {
                                Log.e("tzy", "Exception : " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public NativeADDataRef onGdtNativeData() {
                        if (gdtNativeArray != null && gdtNativeArray.size() > gdt_index) {
                            NativeADDataRef temp = gdtNativeArray.get(gdt_index);
                            ++gdt_index;
                            return temp;
                        }
                        return null;
                    }

                    @Override
                    public NativeResponse onBaiduNativeData() {
                        if (baiduNativeArray != null && baiduNativeArray.size() > baidu_index) {
                            NativeResponse temp = baiduNativeArray.get(baidu_index);
                            ++baidu_index;
                            return temp;
                        }
                        return null;
                    }
                });
            }
        }
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
        XHOneAdControl control = listAdContrls.get(index);
        if (control == null)
            return;
        if (control.getAdViewState())
            control.setView(view);
        control.onAdClick(ad_click, twoData + listIndex);
    }

    /**
     * 广告曝光----必须在返回数据后调用该方法（重要）
     *
     * @param index     广告位置在数据的位置
     * @param view      广告显示的view
     * @param listIndex 广告位置---一个数据传""
     */
    public void onAdBind(int index, View view, String listIndex) {
        XHOneAdControl control = listAdContrls.get(index);
        if (control == null)
            return;
        control.onAdBind(view, ad_show, twoData + listIndex);
    }

    public interface XHAdControlCallBack {
        public void onSuccess(String type, Map<String, String> map, int num);

        public void onFail(String type, int num);

        //        public Object onGdtData();
        public NativeADDataRef onGdtNativeData();

        public NativeResponse onBaiduNativeData();
    }

    public interface XHBackIdsDataCallBack {
        public void callBack(Map<String, String> map);
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
            Log.e("tzy", "Exception : " + e.getMessage());
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


    //退出activity时，释放view
    public void releaseView() {
        if (listAdContrls != null && listAdContrls.size() > 0) {
            for (int i = 0; i < listAdContrls.size(); i++) {
                if (listAdContrls.get(i) != null)
                    listAdContrls.get(i).releaseView();
            }
        }
    }

    /**
     * 是否需要刷新
     *
     * @return
     */
    public boolean isNeedRefersh() {
        long nowTime = System.currentTimeMillis();
        Log.i(tag_yu, "nowTime:::" + nowTime + ":::" + oneAdTime + "：：：" + (nowTime - oneAdTime) + ":::" + showTime);
        if (nowTime - oneAdTime >= showTime) {//当前广告已过期
            return true;
        }
        return false;
    }

    public boolean isJudgePicSize() {
        return isJudgePicSize;
    }

    public void setJudgePicSize(boolean judgePicSize) {
        isJudgePicSize = judgePicSize;
    }
}
