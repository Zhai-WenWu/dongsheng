package amodule.home;

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

import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.main.activity.MainHomePage;
import amodule.main.bean.HomeModuleBean;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.ad.control.AdControlHomeDish;

import static third.ad.control.AdControlHomeDish.tag_yu;

/**
 * Description :
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 14:51.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeDataControler {

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
    private boolean mNeedRefCurrData = false;
    //强制清楚数据
    private boolean compelClearData= false;
    //执行数据有问题时，数据请求，只执行一次。
    private boolean isNextUrl=true;
    //广告控制器
    private AdControlHomeDish mAdControl;

    public HomeDataControler(MainHomePage activity) {
        this.mActivity = activity;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(activity, null);
        CACHE_PATH = FileManager.getSDCacheDir() + "homeDataCache";
        mAdControl = AdControlHomeDish.getInstance().getTwoLoadAdData();
    }

    //读取缓存数据
    public void loadCacheHomeData(InternetCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String hoemDataStr = FileManager.readFile(CACHE_PATH).trim();
            if (!TextUtils.isEmpty(hoemDataStr)) {
                handler.post(() -> callback.loaded(ReqEncyptInternet.REQ_OK_STRING, "", hoemDataStr));
            }
        }).start();
    }

    public void saveCacheHomeData(String data) {
        if(TextUtils.isEmpty(data) || data.trim().length() < 10) return;
        FileManager.saveFileToCompletePath(CACHE_PATH, data, false);
    }

    //获取服务端首页数据
    public void loadServiceHomeData(@Nullable InternetCallback callback) {
        String url = StringManager.API_HOMEPAGE_6_0;
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        ReqEncyptInternet.in().doEncypt(url, params, callback);
    }

    public void loadServiceTopData(InternetCallback callback) {
        String url = StringManager.API_RECOMMEND_TOP;
        ReqEncyptInternet.in().doEncyptAEC(url, "", callback);
    }

    //获取服务端Feed流数据
    public void loadServiceFeedData(boolean refresh, @NonNull OnLoadDataCallback callback) {
        StringBuilder params = new StringBuilder();
        if (refresh) {
            if (!TextUtils.isEmpty(backUrl)) {
                params.append(backUrl);
            } else {
                String localBackUrl = (String) FileManager.loadShared(mActivity, mHomeModuleBean.getType(), SP_KEY_BACKURL);
                params.append(TextUtils.isEmpty(localBackUrl) ? "type=" + mHomeModuleBean.getType() : localBackUrl);
            }
        } else {
            params.append(TextUtils.isEmpty(nextUrl) ? "type=" + mHomeModuleBean.getType() : nextUrl);
        }
        Log.i("tzy", "refresh::" + refresh + "::data:" + params.toString());
        //准备请求
        if (callback != null) {
            callback.onPrepare();
        }
        ReqEncyptInternet.in().doEncyptAEC(StringManager.API_RECOMMEND, params.toString(),
                new InternetCallback(mActivity) {
                    @Override
                    public void loaded(int flag, String url, Object object) {
                        int loadCount = 0;
                        if(flag>= ReqInternet.REQ_OK_STRING){
                            Map<String,String> dataMap = StringManager.getFirstMap(object);
                            //当前url
                            final String currentBackUrl = dataMap.get(SP_KEY_BACKURL);
                            final String currentNextkUrl = dataMap.get(SP_KEY_NEXTURL);
                            //当前数据有问题，直接return数据
                            if(!(!dataMap.containsKey("list")
                                    ||StringManager.getListMapByJson(dataMap.get("list")).size() <= 0)){
                                //存储当前backurl
                                if (!TextUtils.isEmpty(backUrl) && refresh) {
                                    FileManager.saveShared(mActivity, mHomeModuleBean.getType(), SP_KEY_BACKURL, backUrl);
                                }
                                //上拉数据，下拉数据
                                if (TextUtils.isEmpty(backUrl) || (!TextUtils.isEmpty(currentBackUrl) && refresh))
                                    backUrl = currentBackUrl;
                                if (TextUtils.isEmpty(nextUrl) || !TextUtils.isEmpty(currentNextkUrl) && !refresh)
                                    nextUrl = currentNextkUrl;
                                //当前只有向上刷新，并且服务端确认可以刷新数据
                                final String resetValue = dataMap.get("reset");
                                if (compelClearData || (refresh && "2".equals(resetValue))) {
                                    mData.clear();
                                    Log.i("zyj","刷新数据：清集合");
                                    isNeedRefresh(true);
                                    //强制刷新，重置数据
                                    if(!TextUtils.isEmpty(currentBackUrl))
                                        backUrl = currentBackUrl;
                                    if(!TextUtils.isEmpty(currentNextkUrl))
                                        nextUrl = currentNextkUrl;
                                }
                                ArrayList<Map<String, String>> listDatas = StringManager.getListMapByJson(dataMap.get("list"));
                                if (listDatas != null && listDatas.size() > 0) {
                                    loadCount=listDatas.size();
                                    if (refresh && mData.size() > 0) {
                                        //如果需要加广告，插入广告
                                        if(mInsertADCallback != null){
                                            listDatas = mInsertADCallback.insertAD(listDatas,true);
                                            upDataSize += listDatas.size();
                                            Log.i(tag_yu,"listDatas::111:"+listDatas.size());
                                        }
                                        mData.addAll(0, listDatas);//插入到第一个位置
                                    } else {
                                        mData.addAll(listDatas);//顺序插入
                                        //如果需要加广告，插入广告
                                        if(mInsertADCallback != null){
                                            Log.i(tag_yu,"mListData::222:"+mData.size()+"::"+upDataSize);
                                            mData = mInsertADCallback.insertAD(mData,false);
                                        }
                                    }
                                }
                                //读取历史记录
//                                String historyUrl = (String) FileManager.loadShared(mActivity, mHomeModuleBean.getType(), mHomeModuleBean.getType());
//                                if (!TextUtils.isEmpty(historyUrl)) {
//                                    Map<String, String> map = StringManager.getMapByString(historyUrl, "&", "=");
//                                    final String markValue = map.get("mark");
//                                    final String timeValue = map.get("reset_time");
//                                    final int length = mData.size();
//                                    Map<String, String> backMap = StringManager.getMapByString(backUrl, "&", "=");
//                                    String nowTime = backMap.get("reset_time");
//                                    //设置显示参数
//                                    for(int index = 0 ; index < length ; index ++){
//                                        Map<String,String> tempMap = mData.get(index);
//                                        if(index != 0
//                                                && EqualsData(tempMap.get("mark"),markValue)
//                                                && !TextUtils.isEmpty(nowTime)
//                                                && !TextUtils.isEmpty(timeValue)){
//                                            Log.i("zhangyujian", "mak:" + tempMap.get("mark") + "::;" + tempMap.get("name"));
//                                            tempMap.put("refreshTime", Tools.getTimeDifferent(Long.parseLong(nowTime), Long.parseLong(timeValue)));
//                                        }else{
//                                            tempMap.put("refreshTime", "");
//                                        }
//                                    }
//                                }
                                //提示刷新UI
                                if(mNotifyDataSetChangedCallback != null)
                                    mNotifyDataSetChangedCallback.notifyDataSetChanged();
                                //自动请求下一页数据
                                if(mData.size() <= 4){//推荐列表：低于等5的数据自动请求数据
                                    Log.i("zhangyujian","自动下次请求:::"+mData.size());
                                    if(mEntryptDataCallback != null)
                                        mEntryptDataCallback.onEntryptData(false);
                                }
                            }else{//置状态---刷新按钮
                                int size = mData.size();
                                if(size>0){//推荐列表:有数据置状态
                                    for (int i = 0; i < size; i++) {
                                        mData.get(i).put("refreshTime", "");
                                    }
                                    //提示刷新UI
                                    if(mNotifyDataSetChangedCallback != null)
                                        mNotifyDataSetChangedCallback.notifyDataSetChanged();
                                }else{//无数据时---请求下一页数据
                                    if(dataMap.containsKey(SP_KEY_NEXTURL)
                                            && isNextUrl){
                                        nextUrl = currentNextkUrl;
                                        isNextUrl=false;
                                        if(mEntryptDataCallback != null)
                                            mEntryptDataCallback.onEntryptData(false);
                                    }
                                }
                            }
                        }else{
                            if(callback != null){
                                callback.onFailed();
                            }
                        }
                        compelClearData = false;//强制刷新只能使用一次，一次数据后被置回去
                        if(callback != null){
                            callback.onAfter(refresh,flag,loadCount);
                        }
                    }
                });
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
                if (tag >= 1 && nums > 0) {
                    handlerMainThreadUIAD();
                }
            });
            mAdControl.refreshData();
            //推荐首页
            mAdControl.setAdLoadNumberCallBack(Number -> {
                if (Number > 7) {
                    handlerMainThreadUIAD();
                }
            });
            //去掉全部的广告位置
            ArrayList<Map<String, String>> listTemp = new ArrayList<>();
            Stream.of(mData).forEach(map -> {
                if (map.containsKey("adstyle")
                        && "ad".equals(map.get("adstyle"))) {
                    listTemp.add(map);
                }
            });
            Log.i(tag_yu, "删除广告");
            if (listTemp.size() > 0) {
                mData.removeAll(listTemp);
            }
            if(mNotifyDataSetChangedCallback != null){
                mNotifyDataSetChangedCallback.notifyDataSetChanged();
            }
        }
    }

    /** 处理广告在主线程中处理 */
    private void handlerMainThreadUIAD() {
        new Handler(Looper.getMainLooper()).post(() -> {
            mData = mAdControl.getNewAdData(mData, false);
            if(mNotifyDataSetChangedCallback != null){
                mNotifyDataSetChangedCallback.notifyDataSetChanged();
            }
        });
    }

    public void clearData() {
        if (mData == null)
            return;
        mData.clear();
        if(mNotifyDataSetChangedCallback != null){
            mNotifyDataSetChangedCallback.notifyDataSetChanged();
        }
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

    public void setData(ArrayList<Map<String, String>> data) {
        this.mData = data;
    }

    public HomeModuleBean getHomeModuleBean() {
        return mHomeModuleBean;
    }

    public boolean isNeedRefCurrData() {
        return mNeedRefCurrData;
    }

    public void setNeedRefCurrData(boolean needRefCurrData) {
        mNeedRefCurrData = needRefCurrData;
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

    /*--------------------------------------------- Interface ---------------------------------------------*/

    public interface OnLoadDataCallback {
        void onPrepare();
        void onAfter(boolean refersh,int flag,int loadCount);
        void onSuccess();

        void onFailed();
    }

    public interface InsertADCallback{
        ArrayList<Map<String, String>> insertAD(ArrayList<Map<String, String>> listDatas,boolean isBack);
    }

    public interface NotifyDataSetChangedCallback{
        void notifyDataSetChanged();
    }

    public interface EntryptDataCallback{
        void onEntryptData(boolean refersh);
    }

}
