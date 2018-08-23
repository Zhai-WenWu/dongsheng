package amodule.home;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.ActivityMethodManager;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule._common.delegate.ILoadAdData;
import amodule.main.activity.MainHomePage;
import amodule.main.bean.HomeModuleBean;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.ad.control.AdControlHomeDish;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * 数据控制器
 */
public class HomeDataControler implements ActivityMethodManager.IAutoRefresh, ILoadAdData{

    private XHAllAdControl mViewAdControl;

    private String CACHE_PATH = "";
    private final String SP_KEY_BACKURL = "backUrl";
    private final String SP_KEY_NEXTURL = "nexturl";
    private MainHomePage mActivity;
    private String backUrl, nextUrl;
    private HomeModuleBean mHomeModuleBean;
    private ArrayList<Map<String, String>> mData = new ArrayList<>();

    private InsertADCallback mInsertADCallback;
    private NotifyDataSetChangedCallback mNotifyDataSetChangedCallback;
    private EntryptDataCallback mEntryptDataCallback;
    //向上刷新数据集合大小
    private int upDataSize = 0;
    //执行数据有问题时，数据请求，只执行一次。
    private boolean isNextUrl = true;
    //广告控制器
    private AdControlHomeDish mAdControl;

    private long lastSelfAdTime;

    private int mRandom;

    private int mLastDataSize;

    public HomeDataControler(MainHomePage activity) {
        this.mActivity = activity;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(activity, null);
        CACHE_PATH = FileManager.getSDCacheDir() + "homeDataCache";
        mAdControl = AdControlHomeDish.getInstance().getTwoLoadAdData();
        registerRefreshCallback();
    }

    //注册刷新回调
    private void registerRefreshCallback() {
        if(mActivity == null){
            return;
        }
        lastSelfAdTime = System.currentTimeMillis();
        ActivityMethodManager activityMethodManager = mActivity.getActMagager();
        if(activityMethodManager != null){
            activityMethodManager.registerADController(this);
        }
        mAdControl.setRefreshCallback(() -> {
            mData = mAdControl.getAutoRefreshAdData(mData);
            safeNotifySetChanged();
        });
    }

    //读取缓存数据
    public void loadCacheHomeData(InternetCallback callback) {
        String hoemDataStr = FileManager.readFile(CACHE_PATH).trim();
        if (!TextUtils.isEmpty(hoemDataStr)) {
            callback.loaded(ReqEncyptInternet.REQ_OK_STRING, "", hoemDataStr);
        }
    }

    public void saveCacheHomeData(String data) {
        if (TextUtils.isEmpty(data) || data.trim().length() < 10) return;
        new Thread(() -> FileManager.saveFileToCompletePath(CACHE_PATH, data, false)).start();
    }

    //获取服务端首页数据
    public void loadServiceHomeData(@Nullable InternetCallback callback) {
        String url = StringManager.API_HOMEPAGE_6_0;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("randNum", String.valueOf(mRandom));
        ReqEncyptInternet.in().doEncypt(url, params, callback);
    }

    public void loadServiceTopData(InternetCallback callback) {
        String url = StringManager.API_RECOMMEND_TOP;
        ReqEncyptInternet.in().doEncyptAEC(url, "", callback);
    }

    //获取服务端Feed流数据
    public void loadServiceFeedData(boolean firstLoad, @NonNull OnLoadDataCallback callback) {
        StringBuilder params = new StringBuilder();
        String type = mHomeModuleBean.getType();
        params.append(firstLoad ? "type=" + type : nextUrl).append(firstLoad ? "&page=1" : "").append(firstLoad ? "&randNum=" + mRandom : "");
        Log.i("tzy", "firstLoad::" + firstLoad + "::data:" + params.toString());
        //准备请求
        if (callback != null)
            callback.onPrepare();
        ReqEncyptInternet.in().doEncyptAEC(StringManager.API_RECOMMEND, params.toString(),
                new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object object) {
                        int loadCount = 0;
                        resetLastDataSize();
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            Map<String, String> dataMap = StringManager.getFirstMap(object);
                            //当前url
                            final String currentNextkUrl = dataMap.get(SP_KEY_NEXTURL);
                            //当前数据有问题，直接return数据
                            ArrayList<Map<String, String>> listDatas = StringManager.getListMapByJson(dataMap.get("list"));
                            if (null != listDatas && listDatas.size() > 0) {

                                if (TextUtils.isEmpty(nextUrl)
                                        || !TextUtils.isEmpty(currentNextkUrl))
                                    nextUrl = currentNextkUrl;
                                //*******广告数据插入*******
                                loadCount = listDatas.size();
                                mData.addAll(listDatas);//顺序插入
                                //如果需要加广告，插入广告
                                if (mInsertADCallback != null) {
                                    Log.i(tag_yu, "mListData::222:" + mData.size() + "::" + upDataSize);
                                    mData = mInsertADCallback.insertAD(mData, false);
                                }
                                //提示刷新UI
                                safeNotifySetChanged();
                                //自动请求下一页数据
                                if (mData.size() <= 4) {//推荐列表：低于等5的数据自动请求数据
                                    Log.i("zhangyujian", "自动下次请求:::" + mData.size());
                                    if (mEntryptDataCallback != null)
                                        mEntryptDataCallback.onEntryptData(firstLoad);
                                }
                            } else {//置状态---刷新按钮
                                int size = mData.size();
                                if (size > 0) {//推荐列表:有数据置状态
                                    for (int i = 0; i < size; i++) {
                                        mData.get(i).put("refreshTime", "");
                                    }
                                    //提示刷新UI
                                    safeNotifySetChanged();
                                } else {//无数据时---请求下一页数据
                                    if (dataMap.containsKey(SP_KEY_NEXTURL)
                                            && isNextUrl) {
                                        nextUrl = currentNextkUrl;
                                        isNextUrl = false;
                                        if (mEntryptDataCallback != null)
                                            mEntryptDataCallback.onEntryptData(firstLoad);
                                    }
                                }
                            }
                            if (callback != null)
                                callback.onSuccess();
                        } else {
                            if (callback != null)
                                callback.onFailed();
                        }
                        if (callback != null)
                            callback.onAfter(flag, loadCount);
                    }
                });
    }

    private void safeNotifySetChanged() {
        if (mNotifyDataSetChangedCallback != null)
            mNotifyDataSetChangedCallback.notifyDataSetChanged();
    }

    /**
     * 刷新广告数据
     *
     * @param isForceRefresh 是否强制刷新广告
     */
    public void isNeedRefresh(boolean isForceRefresh) {
        if (mAdControl == null
                || mData == null)
            return;//条件过滤
        boolean state = mAdControl.isNeedRefresh();
        Log.i(tag_yu, "isNeedRefresh::::" + state + " :: 推荐 ; isForceRefresh = " + isForceRefresh);
        if (isForceRefresh)
            state = true;//强制刷新
        if (state) {
            //重新请求广告
            mAdControl.setAdDataCallBack((tag, nums) -> {
                if (tag >= 1 && nums > 0)
                    handlerMainThreadUIAD();
            });
            mAdControl.refreshData();
            //推荐首页
            mAdControl.setAdLoadNumberCallBack(Number -> {
                if (Number > 7)
                    handlerMainThreadUIAD();
            });
            //去掉全部的广告位置
            ArrayList<Map<String, String>> listTemp = new ArrayList<>();
            Stream.of(mData)
                    .filter(map -> map.containsKey("adstyle") && "ad".equals(map.get("adstyle")))
                    .forEach(listTemp::add);
            Log.i(tag_yu, "删除广告");
            if (listTemp.size() > 0) {
                mData.removeAll(listTemp);
            }
            safeNotifySetChanged();
        }
    }

    /** 处理广告在主线程中处理 */
    private void handlerMainThreadUIAD() {
        new Handler(Looper.getMainLooper()).post(() -> {
            mData = mAdControl.getNewAdData(mData, false);
            safeNotifySetChanged();
        });
    }

    public void clearData() {
        if (mData == null)
            return;
        mData.clear();
        if (mNotifyDataSetChangedCallback != null)
            mNotifyDataSetChangedCallback.notifyDataSetChanged();
    }

    //刷新广告index
    public void refreshADIndex() {
        if (mAdControl == null)
            return;
        mAdControl.refrush();
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public AdControlHomeDish getAdControl() {
        return mAdControl;
    }

    public ArrayList<Map<String, String>> getData() {
        return mData;
    }

    public int getDataSize(){
        return mData != null ? mData.size() : 0;
    }

    public void setData(ArrayList<Map<String, String>> data) {
        this.mData = data;
    }

    public HomeModuleBean getHomeModuleBean() {
        return mHomeModuleBean;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public int getUpDataSize() {
        return upDataSize;
    }

    public void setInsertADCallback(InsertADCallback insertADCallback) {
        mInsertADCallback = insertADCallback;
    }

    public void setNotifyDataSetChangedCallback(NotifyDataSetChangedCallback notifyDataSetChangedCallback) {
        mNotifyDataSetChangedCallback = notifyDataSetChangedCallback;
    }

    public void setEntryptDataCallback(EntryptDataCallback entryptDataCallback) {
        this.mEntryptDataCallback = entryptDataCallback;
    }

    @Override
    public void loadAdData(@NonNull ArrayList<String> listIds, @NonNull XHAllAdControl.XHBackIdsDataCallBack xhBackIdsDataCallBack, @NonNull Activity act, String StatisticKey) {
        if (ToolsDevice.isNetworkAvailable(act)) {
            mViewAdControl = new XHAllAdControl(listIds, (isRefresh, map) ->
            {xhBackIdsDataCallBack.callBack(isRefresh, map);},
                    XHActivityManager.getInstance().getCurrentActivity(),
                    StatisticKey);

        }
    }

    @Override
    public void autoRefreshSelfAD() {
        if(mAdControl != null){
            mAdControl.autoRefreshSelfAD();
        }
        if (mViewAdControl != null) {
            mViewAdControl.autoRefreshSelfAD();
        }
    }

    public void setRandom(int random) {
        mRandom = random;
    }

    public int getLastDataSize() {
        return mLastDataSize;
    }

    public void resetLastDataSize() {
        mLastDataSize = mData.size();
    }

    /*--------------------------------------------- Interface ---------------------------------------------*/

    public interface OnLoadDataCallback {
        void onPrepare();

        void onAfter(int flag, int loadCount);

        void onSuccess();

        void onFailed();
    }

    public interface InsertADCallback {
        ArrayList<Map<String, String>> insertAD(ArrayList<Map<String, String>> listDatas, boolean isBack);
    }

    public interface NotifyDataSetChangedCallback {
        void notifyDataSetChanged();
    }

    public interface EntryptDataCallback {
        void onEntryptData(boolean firstLoad);
    }

    public XHAllAdControl getAllAdController() {
        return mViewAdControl;
    }

}
