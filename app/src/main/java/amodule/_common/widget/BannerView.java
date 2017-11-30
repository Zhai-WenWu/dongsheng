package amodule._common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.banner.Banner;
import acore.widget.banner.BannerAdapter;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.IStatictusData;
import amodule._common.helper.WidgetDataHelper;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.tools.AdPlayIdConfig.HOME_BANNEER_1;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerView extends Banner implements IBindMap, IStatictusData,ISaveStatistic,IHandlerClickEvent {
    public static final String KEY_ALREADY_SHOW = "alreadyshow";
    private LayoutInflater mInflater;
    private XHAllAdControl mAdControl;
    private ArrayList<String> mAdIDArray = new ArrayList<>();
    private int showMinH,showMaxH;

    private View adView = null;
    private Map<String,String> adMap = new HashMap<>();
    public static final int TAG_ID = R.string.tag;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int paddingBottom = getResources().getDimensionPixelSize(R.dimen.dp_10);
        setPadding(0, 0, 0, paddingBottom);
        mInflater = LayoutInflater.from(context);
        int height = (int) (ToolsDevice.getWindowPx(context).widthPixels * 320 / 750f) + paddingBottom;
        Log.i("tzy","width = " + ToolsDevice.getWindowPx(context).widthPixels + " , height = " + height);
        post(() -> {
            getLayoutParams().height = height;
            setMinimumHeight(height);
        });
        setVisibility(GONE);
        mAdIDArray.add(HOME_BANNEER_1);
        showMinH = Tools.getStatusBarHeight(context) + Tools.getDimen(context,R.dimen.dp_45) - height;
        showMaxH = ToolsDevice.getWindowPx(getContext()).heightPixels - Tools.getDimen(context,R.dimen.dp_50);
    }

    ArrayList<Map<String, String>> arrayList = new ArrayList<>();
    @Override
    public void setData(Map<String, String> data) {
        initAdData();
        if (null == data || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(data.get(WidgetDataHelper.KEY_DATA));
        arrayList.clear();
        arrayList = StringManager.getListMapByJson(dataMap.get(WidgetDataHelper.KEY_LIST));
        if (arrayList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        //添加数据
        Stream.of(arrayList).forEach(map -> {
            map.put("isAd", "1");
            map.put("title", "");
        });
        //创建数据适配器
        BannerAdapter<Map<String, String>> bannerAdapter = new BannerAdapter<Map<String, String>>(arrayList) {
            @Override
            protected void bindTips(TextView tv, Map<String, String> stringStringMap) {
            }

            @Override
            public void bindView(View view, Map<String, String> data) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                Object tagValue = imageView.getTag(TAG_ID);
                if(tagValue != null && tagValue.equals(data.get("img"))){
                    return;
                }
                imageView.setTag(TAG_ID,data.get("img"));
                Glide.with(getContext())
                        .load(data.get("img"))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(new SubBitmapTarget() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                if("2".equals(data.get("isAd"))){
                    adView = view;
                    TextView textView = (TextView) view.findViewById(R.id.title);
                    textView.setText(TextUtils.isEmpty(data.get("title")) ? "" : data.get("title"));
                    ImageView icon = (ImageView) view.findViewById(R.id.ad_icon);
                    icon.setOnClickListener(v ->
                            AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), v, mAdControl, 0, "")
                    );
                    icon.setVisibility(VISIBLE );
                }else{
                    view.findViewById(R.id.ad_layout).setVisibility(GONE);
                }
            }

            @SuppressLint("InflateParams")
            @Override
            public View getView(int position) {
                return mInflater.inflate(R.layout.widget_banner_item, null, true);
            }
        };
        if(!arrayList.isEmpty()
                && !adMap.isEmpty()
                && !arrayList.contains(adMap)
                && LoginManager.isShowAd()){
            arrayList.add(adMap);
        }
        setBannerAdapter(bannerAdapter);
        notifyDataHasChanged();
        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int[] location = new int[2];
                getLocationInWindow(location);
                if(location[1] > showMinH && location[1] < showMaxH
                        && arrayList.contains(adMap)
                        && arrayList.indexOf(adMap) == position
                        && !adMap.isEmpty()
                        && !adMap.containsKey(KEY_ALREADY_SHOW)){
                    adMap.put(KEY_ALREADY_SHOW,"2");
                    mAdControl.onAdBind(0,adView,"");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setOnBannerItemClickListener(position -> {
            if (position < 0 || position >= arrayList.size()) return;
            Map<String, String> dataMapTemp = arrayList.get(position);
            if ("2".equals(dataMapTemp.get("isAd"))) {
                mAdControl.onAdClick(adView,0,"");
                return;
            }
            String url = arrayList.get(position).get("url");
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                XHClick.mapStat(getContext(), id, twoLevel, threeLevel + position);
            }
        });
        setPageChangeDuration(5 * 1000);
        setRandomItem(arrayList);
        setVisibility(VISIBLE);
    }

    public void initAdData(){
        if(mAdControl == null && ToolsDevice.isNetworkAvailable(getContext())){
            mAdControl = new XHAllAdControl(mAdIDArray,
                    (final Map<String, String> map) ->
                            Stream.of(mAdIDArray).forEach(key -> {
                                String adStr = map.get(key);
                                sendAdMessage(adStr);
                            }),
                    XHActivityManager.getInstance().getCurrentActivity(),
                    "wz_wz");
        }
    }

    protected void sendAdMessage(String adStr) {
        Map<String, String> adDataMap = StringManager.getFirstMap(adStr);
        post(() -> {
            String imageUrl = adDataMap.get("imgUrl");
            if(TextUtils.isEmpty(imageUrl)) return;
            adMap = new HashMap<>();
            adMap.put("isAd","2");
            adMap.put("img",imageUrl);
            adMap.put("title",adDataMap.get("title"));
            if(!arrayList.isEmpty()
                    && !adMap.isEmpty()
                    && !arrayList.contains(adMap)
                    && LoginManager.isShowAd()){
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
        weightSum = 0;
        Log.i("tzy","setRandomItem");
        weightArray = new int[arrayList.size()];
        for (int index = 0; index < weightArray.length; index++) {
            Map<String, String> map = arrayList.get(index);
            if(map.equals(adMap)){
                weightArray[index] = 0;
                continue;
            }
            try {
                String weightStr = map.get("weight");
                if(TextUtils.isEmpty(weightStr) || "null".equals(weightStr)){
                    weightStr = "0";
                }else if(weightStr.indexOf(".") > 0){
                    weightStr = weightStr.substring(0,weightStr.indexOf("."));
                }
                int currentWeight = Integer.parseInt(weightStr);
                weightSum += currentWeight;
                weightArray[index] = weightSum;
            }catch (Exception ignored){
                weightArray[index] = weightSum;
                ignored.printStackTrace();
            }
        }
        //随机权重
        final int randomWeight = Tools.getRandom(0, weightSum);
        for (int index = 0; index < weightArray.length; index++) {
            if (randomWeight < weightArray[index]) {
                setCurrentItem(index);
                Log.i("tzy","setCurrentItem::" + index);
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

    @Override
    public void saveStatisticData() {

    }

    @Override
    public boolean handlerClickEvent(String url, String moduleType, String dataType, int position) {
        return false;
    }

}
