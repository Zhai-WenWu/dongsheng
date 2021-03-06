package amodule.article.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import third.ad.scrollerAd.XHAllAdControl;

import static amodule.article.adapter.ArticleDetailAdapter.TYPE_KEY;
import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;
import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_CONTENT_BOTTOM;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_RECM_1;
import static third.ad.tools.AdPlayIdConfig.ARTICLE_RECM_2;

/**
 * PackageName : amodule.article.tools
 * Created by MrTrying on 2017/6/19 18:44.
 * E_mail : ztanzeyu@gmail.com
 */

public class ArticleAdContrler {
    XHAllAdControl xhAllAdControlBootom, xhAllAdControlList;
    ArrayList<Map<String, String>> adRcomDataArray = new ArrayList<>();
    SparseArray<Boolean> adInsteredArray = new SparseArray<>();
    final int ARTICLE_BOTTOM = 101;
    //广告跟随相关推荐的数据位置
    private final int[] adPositionInList = {0, 2};
    //adPosition 的 index 值
    private final int ARTICLE_RECOMMEND_1 = 0;
    private final int ARTICLE_RECOMMEND_2 = 1;

    private String mAdType;

    protected Handler adHandler = new Handler(Looper.getMainLooper()) {
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
    protected Activity mActivity;
    public ArticleAdContrler(Activity activity){
        mActivity = activity;
    }

    protected void handlerArticleRecData(int index, Object obj) {
        if (obj != null) {
            Map<String, String> data = (Map<String, String>) obj;
            if(!data.isEmpty()){
                data.put("adFollowPosition", String.valueOf(adPositionInList[index]));
            }
            if (adInsteredArray.get(index) == null) {
                adRcomDataArray.add(data);
            }
            adInsteredArray.append(index, false);
            if (adRcomDataArray.size() == 2 && onListAdCallback != null)
                onListAdCallback.onListAdData(data);
        }
    }

    public void initADData() {
        //请求广告数据
        xhAllAdControlBootom = requestAdData(new String[]{ARTICLE_CONTENT_BOTTOM}, "wz_wz");
        xhAllAdControlBootom.registerRefreshCallback();
        xhAllAdControlList = requestAdData(new String[]{ARTICLE_RECM_1, ARTICLE_RECM_2}, "wz_list");
        xhAllAdControlList.registerRefreshCallback();
    }

    protected XHAllAdControl requestAdData(final String[] ads, String id) {
        ArrayList<String> adData = new ArrayList<>();
        for (String str : ads)
            adData.add(str);
        return new XHAllAdControl(adData, new XHAllAdControl.XHBackIdsDataCallBack() {
            @Override
            public void callBack(boolean isRefresh, Map<String, String> map) {
                if(isRefresh && "wz_list".equals(id)){
                    adRcomDataArray.clear();
                    adInsteredArray.clear();
                }
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
        }, mActivity, id);
    }

    protected void sendAdMessage(String adStr, int type) {
        Map<String, String> adDataMap = StringManager.getFirstMap(adStr);
        if (adDataMap != null) {
            Message message = adHandler.obtainMessage();
            message.obj = adDataMap;
            message.what = type;
            adHandler.sendMessage(message);
        }
    }
    boolean canAdBind = true;
    public void onBigAdBind(View adView) {
        if (Tools.inScreenAdView(adView)) {
            if (xhAllAdControlBootom != null && adView != null && canAdBind) {
                xhAllAdControlBootom.onAdBind(0, adView, "");
            }
            canAdBind = false;
        } else {
            canAdBind = true;
        }

    }

    public void onListAdClick(View view, int index, String s) {
        if (xhAllAdControlList != null) xhAllAdControlList.onAdClick(view, index, s);
    }

    public void onListAdBind(int index, View view, String s) {
        if (xhAllAdControlList != null) xhAllAdControlList.onAdBind(index, view, s);
    }

    public View getBigAdView(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.isEmpty())
            return null;
        if (XHActivityManager.getInstance().getCurrentActivity() == null || XHActivityManager.getInstance().getCurrentActivity().isFinishing()
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && XHActivityManager.getInstance().getCurrentActivity().isDestroyed()))
            return null;
        View adView = null;
        if ("1".equals(dataMap.get("isBigPic"))) {
            adView = LayoutInflater.from(XHActivityManager.getInstance().getCurrentActivity()).inflate(R.layout.a_article_detail_ad_small, null);
            RelativeLayout txtContainer = (RelativeLayout) adView.findViewById(R.id.txt_container);
            TextView titleTextView = (TextView) adView.findViewById(R.id.rec_title);
            TextView nameTextView = (TextView) adView.findViewById(R.id.rec_customer_name);
            TextView browseTextView = (TextView) adView.findViewById(R.id.rec_browse);
            browseTextView.setVisibility(View.GONE);
            ImageView imageView = (ImageView) adView.findViewById(R.id.rec_image);

            String desc = dataMap.get("desc");
            if (titleTextView != null && !TextUtils.isEmpty(desc)) {
                titleTextView.setText(desc);
                titleTextView.setMaxLines(Integer.MAX_VALUE);
                txtContainer.setMinimumHeight(adView.getContext().getResources().getDimensionPixelSize(R.dimen.dp_74_5));
                titleTextView.setLines(2);
            }
            String title = dataMap.get("title");
            if (nameTextView != null && !TextUtils.isEmpty(title)) {
                nameTextView.setText(title);
            }
            if (browseTextView != null) {
                browseTextView.setText(String.valueOf(Tools.getRandom(200, 6000)));
            }
            String imageUrl = dataMap.get("imgUrl");
            if (imageView != null && !TextUtils.isEmpty(imageUrl)) {
                Glide.with(XHActivityManager.getInstance().getCurrentActivity())
                        .load(dataMap.get("imgUrl"))
                        .asBitmap()
                        .centerCrop()
                        .into(imageView);
            }
            ViewGroup.MarginLayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            int dp_5 = Tools.getDimen(adView.getContext(), R.dimen.dp_5);
            int dp_20 = Tools.getDimen(adView.getContext(), R.dimen.dp_20);
            layoutParams.setMargins(dp_20, dp_5, dp_20, 0);
            adView.setLayoutParams(layoutParams);
        } else {
            adView = LayoutInflater.from(XHActivityManager.getInstance().getCurrentActivity()).inflate(R.layout.a_article_detail_ad, null);
            TextView titleTv = (TextView) adView.findViewById(R.id.ad_title);
            TextView nameTv = (TextView) adView.findViewById(R.id.user_name);
            RelativeLayout container = (RelativeLayout) adView.findViewById(R.id.container);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) container.getLayoutParams();
            String title = dataMap.get("title");
            params.topMargin = adView.getResources().getDimensionPixelSize(TextUtils.isEmpty(title) ? R.dimen.dp_15 : R.dimen.dp_6);
            //加载图片
            final ImageView imageView = (ImageView) adView.findViewById(R.id.img);
            final int width = ToolsDevice.getWindowPx(XHActivityManager.getInstance().getCurrentActivity()).widthPixels - Tools.getDimen(XHActivityManager.getInstance().getCurrentActivity(), R.dimen.dp_20) * 2;
            Glide.with(XHActivityManager.getInstance().getCurrentActivity())
                    .load(dataMap.get("imgUrl"))
                    .asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String s, Target<Bitmap> target, boolean b) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap bitmap, String s, Target<Bitmap> target, boolean b, boolean b1) {
                            int height = width * bitmap.getHeight() / bitmap.getWidth();
                            imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
                            return false;
                        }
                    }).into(imageView);
            //加载title
            if (TextUtils.isEmpty(title)) {
                titleTv.setVisibility(View.GONE);
                nameTv.setVisibility(View.GONE);
            } else {
                if (!TextUtils.isEmpty(dataMap.get("desc")))
                    titleTv.setText(dataMap.get("desc"));
                titleTv.setVisibility(View.VISIBLE);
                nameTv.setText(title);
                nameTv.setVisibility(View.VISIBLE);
            }
            ViewGroup.MarginLayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
            int dp_17 = Tools.getDimen(adView.getContext(), R.dimen.dp_17);
            int dp_20 = Tools.getDimen(adView.getContext(), R.dimen.dp_20);
            layoutParams.setMargins(dp_20, dp_17, dp_20, dp_17);
            adView.setLayoutParams(layoutParams);
        }
        View view = adView.findViewById(ID_AD_ICON_GDT);
        mAdType = dataMap.get("type");
        if (view != null) {
            view.setVisibility(ADKEY_GDT.equals(mAdType) ? View.VISIBLE : View.GONE);
        }
        //设置ad点击
        if (adView != null) {
            setAdClick(adView, dataMap);
        }
        return adView;
    }

    public String getAdType() {
        return mAdType;
    }

    public void setAdClick(final View adView, Map<String, String> dataMap) {
        adView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xhAllAdControlBootom.onAdClick(adView, 0, "0");
            }
        });
        final View adTag = adView.findViewById(R.id.ad_tag);
        if (adTag != null
                && dataMap != null
                && !"1".equals(dataMap.get("adType"))) {
            adTag.setVisibility(View.VISIBLE);
            adTag.setOnClickListener(v -> AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), adTag, xhAllAdControlBootom, 0, "0"));
        }
    }

    /**
     * @param allDataListMap 所有数据
     */
    public synchronized void handlerAdData(List<Map<String, String>> allDataListMap) {

        if (adRcomDataArray != null && !adRcomDataArray.isEmpty()
                && allDataListMap != null && !allDataListMap.isEmpty()) {
            for(int i = 0;i<allDataListMap.size();i++){
                if("2".equals(allDataListMap.get(i).get("isAd"))){
                    allDataListMap.remove(i);
                    i--;
                }
            }
            //循环ad数据
            for (int adIndex = 0, adLength = adRcomDataArray.size(); adIndex < adLength; adIndex++) {
                //验证是否已经插入
                if (adInsteredArray.get(adIndex) != null && adInsteredArray.get(adIndex)) continue;
                //获取广告map
                Map<String, String> adMap = getAdMap(adRcomDataArray.get(adIndex), adIndex);
                //遍历原始数据体插入数据
                for (int oriDataIndex = 0, recIndex = 0, allDataSize = allDataListMap.size(); oriDataIndex < allDataSize; oriDataIndex++) {
                    Map<String, String> oriData = allDataListMap.get(oriDataIndex);
                    if (String.valueOf(Type_recommed).equals(oriData.get(TYPE_KEY))) {
                        if ("1".equals(oriData.get("isAd")) && !adMap.isEmpty() && !TextUtils.isEmpty(adMap.get("adFollowPosition")) ) {
                            int adFollowPosiont = Integer.parseInt(adMap.get("adFollowPosition"));
                            int adInsertPosition = oriDataIndex + 1;
                            if (adFollowPosiont == recIndex) {
                                if (allDataListMap.size() > adInsertPosition
                                        && "1".equals(allDataListMap.get(adInsertPosition).get("isAd"))) {
                                    allDataListMap.add(adInsertPosition, adMap);
                                    adInsteredArray.put(adIndex, true);
                                    break;
                                } else if (allDataListMap.size() == adInsertPosition
                                        && "1".equals(allDataListMap.get(allDataListMap.size() - 1).get("isAd"))) {
                                    allDataListMap.add(adMap);
                                    adInsteredArray.put(adIndex, true);
                                    break;
                                }
                            }
                            recIndex++;
                        }
                    }
                }
            }
        }
    }

    protected Map<String, String> getAdMap(Map<String, String> adMap, int index) {
        Map<String, String> dataMap = new HashMap<>();
        if(adMap != null && !adMap.isEmpty()){
            try {
                dataMap.put(TYPE_KEY, String.valueOf(Type_recommed));
                dataMap.put("isAd", "2");
                dataMap.put("adPosition", String.valueOf(index));
                dataMap.put("adFollowPosition", adMap.get("adFollowPosition"));
                dataMap.put("title", adMap.get("desc"));//adMap.get("title") + " | " +
                dataMap.put("img", adMap.get("imgUrl"));
                dataMap.put("customer", new JSONObject().put("nickName", adMap.get("title")).toString());
    //            dataMap.put("clickAll", Tools.getRandom(200, 5000) + "浏览");
                dataMap.put("commentNumber", "");
                dataMap.put("adType", adMap.get("adType"));
                dataMap.put("ad", adMap.get("type"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
    protected OnListAdCallback onListAdCallback;

    public void setOnBigAdCallback(OnBigAdCallback onBigAdCallback) {
        this.onBigAdCallback = onBigAdCallback;
    }

    public void setOnListAdCallback(OnListAdCallback onListAdCallback) {
        this.onListAdCallback = onListAdCallback;
    }
}
