package amodule.article.tools;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.ad.scrollerAd.XHAllAdControl;

import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_CONTENT_BOTTOM;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_RECM_1;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_RECM_2;

/**
 * PackageName : amodule.article.tools
 * Created by MrTrying on 2017/6/19 18:44.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleAdContrler {
    private XHAllAdControl xhAllAdControlBootom, xhAllAdControlList;
    private ArrayList<Map<String, String>> adRcomDataArray = new ArrayList<>();
    private SparseArray<Boolean> adInsteredArray = new SparseArray<>();
    public final int ARTICLE_BOTTOM = 101;
    private final int[] adPositionInList = {1, 4};
    //adPosition 的 index 值
    public final int ARTICLE_RECOMMEND_1 = 0;
    public final int ARTICLE_RECOMMEND_2 = 1;
    private Handler adHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ARTICLE_BOTTOM:
                    if (onBigAdCallback != null)
                        onBigAdCallback.onBigAdData((Map<String, String>) msg.obj);
                    break;
                case ARTICLE_RECOMMEND_1:
                case ARTICLE_RECOMMEND_2:
                    handlerArticleRecData(msg.what, msg.obj);
                    break;
            }
        }
    };

    private void handlerArticleRecData(int index, Object obj) {
        if (obj != null) {
            Map<String, String> data = (Map<String, String>) obj;
            data.put("adPosition", String.valueOf(adPositionInList[index]));
            adInsteredArray.append(index, false);
            adRcomDataArray.add(data);
            if (onListAdCallback != null)
                onListAdCallback.onListAdData(data);
        }
    }

    public void initADData() {
        //请求广告数据
        xhAllAdControlBootom = requestAdData(new String[]{ARTICLE_CONTENT_BOTTOM}, "wz_wz");
        xhAllAdControlList = requestAdData(new String[]{ARTICLE_RECM_1, ARTICLE_RECM_2}, "wz_list");
    }

    private XHAllAdControl requestAdData(final String[] ads, String id) {
        ArrayList<String> adData = new ArrayList<>();
        for (String str : ads)
            adData.add(str);
        return new XHAllAdControl(adData, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(Map<String, String> map) {
                for (String key : ads) {
                    String adStr = map.get(key);
                    switch (key) {
                        case ARTICLE_CONTENT_BOTTOM:
                            sendAdMessage(adStr, ARTICLE_BOTTOM);
                            break;
                        case ARTICLE_RECM_1:
                            sendAdMessage(adStr, ARTICLE_RECOMMEND_1);
                            break;
                        case ARTICLE_RECM_2:
                            sendAdMessage(adStr, ARTICLE_RECOMMEND_2);
                            break;
                    }
                }
            }
        }, XHActivityManager.getInstance().getCurrentActivity(), id);
    }

    private void sendAdMessage(String adStr, int type) {
        Map<String, String> adDataMap = StringManager.getFirstMap(adStr);
        if (adDataMap != null && adDataMap.size() > 0) {
            Message message = adHandler.obtainMessage();
            message.obj = adDataMap;
            message.what = type;
            adHandler.sendMessage(message);
        }
    }

    public void onBigAdBind(View adView) {
        if (xhAllAdControlBootom != null && adView != null)
            xhAllAdControlBootom.onAdBind(0, adView, "0");
    }

    public void onListAdClick(View view, int index, String s) {
        if (xhAllAdControlList != null) xhAllAdControlList.onAdClick(view, index, s);
    }

    public void onListAdBind(int index, View view, String s) {
        if (xhAllAdControlList != null) xhAllAdControlList.onAdBind(index, view, s);
    }

    public View getBigAdView(Map<String, String> dataMap) {
        final View adView = LayoutInflater.from(XHActivityManager.getInstance().getCurrentActivity()).inflate(R.layout.a_article_detail_ad, null);
        //加载图片
        ImageView imageView = (ImageView) adView.findViewById(R.id.img);
        int width = ToolsDevice.getWindowPx(XHActivityManager.getInstance().getCurrentActivity()).widthPixels - Tools.getDimen(XHActivityManager.getInstance().getCurrentActivity(), R.dimen.dp_20) * 2;
        int height = width * 312 / 670;//312 670
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        Glide.with(XHActivityManager.getInstance().getCurrentActivity()).load(dataMap.get("imgUrl")).centerCrop().into(imageView);
        //加载title
        TextView adTitle = (TextView) adView.findViewById(R.id.title);
        adTitle.setText(new StringBuilder().append(dataMap.get("title")).append(" | ").append(dataMap.get("desc")));
        //设置ad点击
        adView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControlBootom.onAdClick(adView, 0, "0");
            }
        });
        adView.findViewById(R.id.ad_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), adView.findViewById(R.id.ad_tag), xhAllAdControlBootom, 0, "0");
            }
        });
        return adView;
    }

    public void handlerAdData(List<Map<String, String>> allDataListMap) {
        if (adRcomDataArray != null && allDataListMap.size() > 1) {
            final int lastIndex = adPositionInList[adPositionInList.length - 1];
            for (int index = 0, length = adRcomDataArray.size(); index < length; index++) {
                //
                if (adInsteredArray.get(index) != null && adInsteredArray.get(index)) continue;

                Map<String, String> adMap = adRcomDataArray.get(index);
                final int adPosition = Integer.parseInt(adMap.get("adPosition")) + 1;
                Map<String, String> dataMap = getAdMap(adMap, index);
                if (!dataMap.isEmpty()) {
                    if (allDataListMap.size() > adPosition) {
                        allDataListMap.add(adPosition, dataMap);
                        adInsteredArray.put(index, true);
                        //特殊处理最后一个广告
                    } else if (allDataListMap.size() - adPosition <= 1 && adPosition == lastIndex + 1) {
                        allDataListMap.add(dataMap);
                        adInsteredArray.put(index, true);
                        break;
                    }
                }
                Log.i("tzy", "ADmap = " + dataMap.toString());
            }
        }
    }

    private Map<String, String> getAdMap(Map<String, String> adMap, int index) {
        Map<String, String> dataMap = new HashMap<>();
        try {
            dataMap.put("datatype", String.valueOf(Type_recommed));
            dataMap.put("isAd", "2");
            dataMap.put("adPosition", String.valueOf(index));
            dataMap.put("title", adMap.get("desc"));//adMap.get("title") + " | " +
            dataMap.put("img", adMap.get("imgUrl"));
            dataMap.put("customer", new JSONObject().put("nickName", adMap.get("title")).toString());
            dataMap.put("clickAll", Tools.getRandom(1000, 60000) + "浏览");
            dataMap.put("commentNumber", "广告");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataMap;
    }

    public interface OnBigAdCallback {
        public void onBigAdData(Map<String, String> adDataMap);
    }

    public interface OnListAdCallback {
        public void onListAdData(Map<String, String> adDataMap);
    }

    private OnBigAdCallback onBigAdCallback;
    private OnListAdCallback onListAdCallback;

    public void setOnBigAdCallback(OnBigAdCallback onBigAdCallback) {
        this.onBigAdCallback = onBigAdCallback;
    }

    public void setOnListAdCallback(OnListAdCallback onListAdCallback) {
        this.onListAdCallback = onListAdCallback;
    }
}
