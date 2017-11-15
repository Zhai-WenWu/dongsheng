package amodule.home;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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

    public HomeDataControler(MainHomePage activity) {
        this.mActivity = activity;
        mHomeModuleBean = new HomeModuleControler().getHomeModuleByType(activity, null);
        CACHE_PATH = FileManager.getSDCacheDir() + "homeDataCache";
    }

    //读取缓存数据
    public void loadCacheHomeData(InternetCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper(),
                msg -> {
                    callback.loaded(ReqEncyptInternet.REQ_OK_STRING, "", msg.obj);
                    return false;
                });
        new Thread(() -> {
            String hoemDataStr = FileManager.readFile(CACHE_PATH).trim();
            if (!TextUtils.isEmpty(hoemDataStr)) {
                Message msg = handler.obtainMessage(0, hoemDataStr);
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void saveCacheHomeData(String data) {
        Log.i("tzy","saveCacheHomeData");
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
        StringBuffer params = new StringBuffer();
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
                        compelClearData=false;//强制刷新只能使用一次，一次数据后被置回去
                        if(callback != null){
                            callback.onAfter(refresh,flag,loadCount);
                        }
                    }
                });
    }

    public boolean EqualsData(Object o1,Object o2){
        if(null == o1 && null == o2)
            return true;
        return null != o1 ? o1.equals(o2) : o2.equals(o1);
    }

    public void clearData() {
        if (mData == null)
            return;
        mData.clear();
        if(mNotifyDataSetChangedCallback != null){
            mNotifyDataSetChangedCallback.notifyDataSetChanged();
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

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

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public int getUpDataSize() {
        return upDataSize;
    }

    public void setUpDataSize(int upDataSize) {
        this.upDataSize = upDataSize;
    }

    public InsertADCallback getInsertADCallback() {
        return mInsertADCallback;
    }

    public void setInsertADCallback(InsertADCallback insertADCallback) {
        mInsertADCallback = insertADCallback;
    }

    public NotifyDataSetChangedCallback getNotifyDataSetChangedCallback() {
        return mNotifyDataSetChangedCallback;
    }

    public void setNotifyDataSetChangedCallback(NotifyDataSetChangedCallback notifyDataSetChangedCallback) {
        mNotifyDataSetChangedCallback = notifyDataSetChangedCallback;
    }

    public EntryptDataCallback getEntryptDataCallback() {
        return mEntryptDataCallback;
    }

    public void setEntryptDataCallback(EntryptDataCallback entryptDataCallback) {
        mEntryptDataCallback = entryptDataCallback;
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
