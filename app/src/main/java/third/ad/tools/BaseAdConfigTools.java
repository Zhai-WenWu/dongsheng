package third.ad.tools;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.annimon.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * Date:2018/3/16.
 * Desc:
 * Author:SLL
 * Email:
 */

public class BaseAdConfigTools {

    private Handler mStatisticHandler;
    private Runnable mStatisticRun;
    private ArrayList<JSONObject> mShowCacheParams;
    private long mIntervalTime = 30 * 1000;
    private int mCacheSize = 5;
    private boolean isStart = false;

    BaseAdConfigTools() {
        init();
    }

    private void init() {
        mStatisticHandler = new Handler();
        mShowCacheParams = new ArrayList<>();
        mStatisticRun = () -> {
            if (mShowCacheParams == null)
                return;
            requestShowStatistics();
            startStatistics();
        };
    }

    private void requestShowStatistics() {
        if(mShowCacheParams != null && !mShowCacheParams.isEmpty()){
            JSONArray jsonArray = new JSONArray();
            LinkedHashMap<String,String> params = new LinkedHashMap<>();
            Stream.of(mShowCacheParams)
                    .filter(value -> value != null && value.length() > 0)
                    .forEach(jsonArray::put);
            params.put("log_json",jsonArray.toString());
            mShowCacheParams.clear();
            requestStatistics(StringManager.api_adsNumber, params);
        }
    }

    private void requestStatistics(String url, LinkedHashMap<String, String> params) {
        ReqInternet.in().doPost(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
            }
        });
    }

    /**
     * @param event：行为事件
     * @param gg_position_code：广告位id
     * @param gg_position_id：adPositionId
     * @param gg_business：广告商
     *
     * @param gg_business_id：自由广告id（只有自有才有该字段）
     */
    public void postStatistics(@NonNull String event, @NonNull String gg_position_code,@NonNull String gg_position_id,
                               @NonNull String gg_business, @NonNull String gg_business_id) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        //时间
        map.put("app_time", Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));
        //行为事件
        map.put("event", event);
        //广告位code
        if (!TextUtils.isEmpty(gg_position_code)) {
            map.put("gg_position_code", gg_position_code);
        }
        //广告位id
        if (!TextUtils.isEmpty(gg_position_id)) {
            map.put("gg_position_id", gg_position_id);
        }
        //广告商
        if (!TextUtils.isEmpty(gg_business)) {
            map.put("gg_business", gg_business);
        }
        //广告商id
        if (!TextUtils.isEmpty(gg_business_id)) {
            map.put("gg_business_id", gg_business_id);
        }
        JSONObject jsonObject = MapToJsonEncode(map);
        LinkedHashMap<String, String> params = new LinkedHashMap<>();

        Log.i("tongji", "postStatistics: params=" + params.toString());
//        requestStatistics(StringManager.api_monitoring_9,params);
        switch (event) {
            case "click":
            case "download":
                params.put("log_json", new JSONArray().put(jsonObject).toString());
                requestStatistics(StringManager.api_adsNumber, params);
                break;
            case "show":
                mShowCacheParams.add(jsonObject);
                if (!isStart){
                    isStart = true;
                    startStatistics();
                }
                if (checkSendSta()) {
                    startStatistics(0);
                }
                break;
        }
    }

    private boolean checkSendSta() {
        return mShowCacheParams != null && mShowCacheParams.size() >= mCacheSize;
    }

    private static JSONObject MapToJsonEncode(Map<String, String> maps) {
        JSONObject jsonObject = new JSONObject();
        if (maps == null || maps.size() <= 0)
            return jsonObject;
        Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
        try {
            while (enty.hasNext()) {
                Map.Entry<String, String> entry = enty.next();
                jsonObject.put(entry.getKey(), Uri.encode(entry.getValue()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 设置间隔时间
     * @param intervalTime 单位是s
     */
    public void setIntervalTime(long intervalTime) {
        if (intervalTime > 0)
            mIntervalTime = intervalTime * 1000;
    }

    public void setCacheSize(int cacheSize) {
        if (cacheSize != 0)
            mCacheSize = cacheSize;
    }

    protected void startStatistics(long intervalTime) {
        if (mStatisticRun != null && mStatisticHandler != null) {
            stopStatistics();
            mStatisticHandler.postDelayed(mStatisticRun, intervalTime);
        }
    }

    public void startStatistics() {
        startStatistics(mIntervalTime);
    }

    public void stopStatistics() {
        mStatisticHandler.removeCallbacks(mStatisticRun);
    }

    public void destroyStatistics() {
        stopStatistics();
        mStatisticRun = null;
        mStatisticHandler = null;
        mShowCacheParams = null;
    }

    public void recreateStatistics() {
        init();
    }
}
