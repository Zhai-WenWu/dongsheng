package amodule._common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.banner.Banner;
import acore.widget.banner.BannerAdapter;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IStatictusData;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.tools.AdPlayIdConfig.ARTICLE_CONTENT_BOTTOM;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerView extends Banner implements IBindMap, IStatictusData {

    private OnBannerItemClickCallback mOnBannerItemClickCallback;
    private LayoutInflater mInflater;
    private XHAllAdControl mAdControl;
    private ArrayList<String> mAdIDArray = new ArrayList<>();

    private Map<String,String> adMap = new HashMap<>();

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        int height = (int) (ToolsDevice.getWindowPx(context).widthPixels * 32 / 75f);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        setVisibility(GONE);
    }

    ArrayList<Map<String, String>> arrayList = new ArrayList<>();
    @Override
    public void setData(Map<String, String> data) {
        initAdData();
//        Log.i("tzy","banner data = " + data.toString());
        if (null == data || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(data.get("data"));
        arrayList = StringManager.getListMapByJson(dataMap.get("list"));
        if (arrayList.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        Stream.of(arrayList).forEach(map -> {
            map.put("isAd", "1");
            map.put("title", "");
        });

        BannerAdapter<Map<String, String>> bannerAdapter = new BannerAdapter<Map<String, String>>(arrayList) {
            @Override
            protected void bindTips(TextView tv, Map<String, String> stringStringMap) {
            }

            @Override
            public void bindView(View view, Map<String, String> data) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                Glide.with(getContext())
                        .load(data.get("img"))
                        .placeholder(R.drawable.i_nopic)
                        .error(R.drawable.i_nopic)
                        .into(imageView);
                if("2".equals(data.get("isAd"))){
                    TextView textView = (TextView) view.findViewById(R.id.title);
                    textView.setText(TextUtils.isEmpty(data.get("title")) ? "" : data.get("title"));
                    ImageView icon = (ImageView) view.findViewById(R.id.ad_icon);
                    icon.setOnClickListener(v ->
                            AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), v, mAdControl, 0, "")
                    );
                    icon.setVisibility(VISIBLE );
                    mAdControl.onAdBind(0,view,"");
                }else{
                    view.findViewById(R.id.ad_layout).setVisibility(GONE);
                }
            }

            @Override
            public View getView(int position) {
                return mInflater.inflate(R.layout.widget_banner_item, null, true);
            }
        };
        if(!arrayList.isEmpty()
                && !adMap.isEmpty()
                && !arrayList.contains(adMap)){
            arrayList.add(adMap);
        }
        setBannerAdapter(bannerAdapter);
        notifyDataHasChanged();
        setOnBannerItemClickListener(position -> {
            if (position < 0 || position >= arrayList.size()) return;
            Map<String, String> dataMapTemp = arrayList.get(position);
            if ("2".equals(dataMapTemp.get("isAd"))) {
                mAdControl.onAdClick(0,"0");
                return;
            }
            if (mOnBannerItemClickCallback != null) {
                mOnBannerItemClickCallback.onBannerItemClick(position, arrayList.get(position));
            } else {
                String url = arrayList.get(position).get("url");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
            }
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                XHClick.mapStat(getContext(), id, twoLevel, threeLevel + position);
            }
        });
        setPageChangeDuration(6 * 1000);
        setRandomItem(arrayList);
        setVisibility(VISIBLE);
    }

    public void initAdData(){
        if(mAdControl == null && ToolsDevice.isNetworkAvailable(getContext())){
            mAdIDArray.add(ARTICLE_CONTENT_BOTTOM);
            mAdControl = new XHAllAdControl(mAdIDArray,
                    (final Map<String, String> map) ->
                            Stream.of(mAdIDArray).forEach(key -> {
                                String adStr = map.get(key);
                                sendAdMessage(adStr,mAdIDArray.indexOf(key));
                            })
                    ,
                    XHActivityManager.getInstance().getCurrentActivity(),
                    "");
            mAdControl.getAllAdData();
        }
    }

    protected void sendAdMessage(String adStr, int type) {
        Map<String, String> adDataMap = StringManager.getFirstMap(adStr);
        post(() -> {
            String imageUrl = adDataMap.get("imgUrl");
            if(TextUtils.isEmpty(imageUrl)) return;
            adMap = new HashMap<>();
            adMap.put("isAd","2");
            adMap.put("img",imageUrl);
            adMap.put("title",adDataMap.get("title") + adDataMap.get("title"));
            if(!arrayList.isEmpty()
                    && !adMap.isEmpty()
                    && !arrayList.contains(adMap)){
                arrayList.add(adMap);
                notifyDataHasChanged();
            }
        });
    }

    int weightSum = 0;
    int[] weightArray;

    private void setRandomItem(ArrayList<Map<String, String>> arrayList) {
        if (null == arrayList || arrayList.isEmpty()) {
            return;
        }
        weightArray = new int[arrayList.size()];
        for (int index = 0; index < weightArray.length; index++) {
            Map<String, String> map = arrayList.get(index);
            if(map.equals(adMap)){
                weightArray[index] = 0;
                continue;
            }
            String weightStr = map.get("weight");
            int currentWeight = TextUtils.isEmpty(weightStr) || "null".equals(weightStr) ? 0 : Integer.parseInt(weightStr);
            weightSum += currentWeight;
            weightArray[index] = weightSum;
        }
        //TODO
//        setCurrentItem(weightArray.length-1);
//        Log.i("tzy","index = " + (weightArray.length-1));
        //随机权重
        final int randomWeight = Tools.getRandom(0, weightSum);
        for (int index = 0; index < weightArray.length; index++) {
            if (randomWeight < weightArray[index]) {
                setCurrentItem(index);
                break;
            }
        }

    }

    String id, twoLevel, threeLevel;

    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }

    public interface OnBannerItemClickCallback {
        void onBannerItemClick(int position, Map<String, String> map);
    }

    public void setOnBannerItemClickCallback(OnBannerItemClickCallback onBannerItemClickCallback) {
        mOnBannerItemClickCallback = onBannerItemClickCallback;
    }
}
