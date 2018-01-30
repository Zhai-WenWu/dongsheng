package amodule._common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.banner.Banner;
import acore.widget.banner.BannerAdapter;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IHandlerClickEvent;
import amodule._common.delegate.ISaveStatistic;
import amodule._common.delegate.ISetAdID;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerView extends Banner implements IBindMap, IStatictusData, ISaveStatistic, IHandlerClickEvent,ISetAdID ,IStatisticCallback {
    public static final String KEY_ALREADY_SHOW = "alreadyshow";
    private LayoutInflater mInflater;
    private XHAllAdControl mAdControl;
    private ArrayList<String> mAdIDArray = new ArrayList<>();
    private int showMinH, showMaxH;

    private View adView = null;
    private Map<String, String> adMap = new HashMap<>();
    public static final int TAG_ID = R.string.tag;
    int imageHeight = 0, imageWidth = 0;
    boolean bgLoadOver = false;
    private String bgKey = "";
    private StatisticCallback mStatisticCallback;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setViewSize(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        bgKey = typedArray.getString(R.styleable.BannerView_bgKey);
        typedArray.recycle();
        if(!TextUtils.isEmpty(bgKey)){
//            Log.i("tzy", "BannerView: bgKey = " + bgKey);
            //同步设置bg图片
            String firstImageUrl = FileManager.loadShared(getContext(), FileManager.xmlFile_appInfo, bgKey).toString();
            setBackImageView(imageView -> ImgManager.loadLongImage(imageView, firstImageUrl));
        }
    }

    private void setViewSize(Context context) {
        int paddingBottom = getResources().getDimensionPixelSize(R.dimen.dp_10);
        setPadding(0, 0, 0, paddingBottom);
        mInflater = LayoutInflater.from(context);
        imageWidth = ToolsDevice.getWindowPx(context).widthPixels;
        imageHeight = (int) (imageWidth * 320 / 750f);
        int height = imageHeight + paddingBottom;
//        Log.i("tzy", "width = " + ToolsDevice.getWindowPx(context).widthPixels + " , height = " + height);
        setTargetHeight(height);
        setVisibility(VISIBLE);
        showMinH = Tools.getStatusBarHeight(context) + Tools.getDimen(context, R.dimen.topbar_height) - height;
        showMaxH = ToolsDevice.getWindowPx(getContext()).heightPixels - Tools.getDimen(context, R.dimen.dp_50);
    }

    private void setTargetHeight(int height) {
        post(() -> {
            if (getLayoutParams() != null)
                getLayoutParams().height = height;
            setMinimumHeight(height);
        });
    }

    ArrayList<Map<String, String>> mArrayList = new ArrayList<>();

    @Override
    public void setData(Map<String, String> data) {
        initAdData();
        if (null == data || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        Map<String, String> dataMap = StringManager.getFirstMap(data.get(WidgetDataHelper.KEY_DATA));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get(WidgetDataHelper.KEY_LIST));
        if (arrayList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        //处理通用数据
        Stream.of(arrayList).forEach(map -> {
            map.put("isAd", "1");
            map.put("title", "");
        });
        //添加广告数据
        if (!arrayList.isEmpty()
                && !adMap.isEmpty()
                && !arrayList.contains(adMap)
                && LoginManager.isShowAd()) {
            arrayList.add(adMap);
        }
        //判断数据是否改变
        if (mArrayList.equals(arrayList)) {
            return;
        }
        //重新添加数据
        mArrayList.clear();
        mArrayList.addAll(arrayList);
        final String firstImageUrl = mArrayList.get(0).get("img");
        if(!TextUtils.isEmpty(bgKey)){
            FileManager.scynSaveSharePreference(getContext(), FileManager.xmlFile_appInfo, bgKey, firstImageUrl);
        }
        //设置默认BG
        if (bgLoadOver) {
            postDelayed(() -> setBackImageView(imageView -> loadBgImage(firstImageUrl, imageView)), 800);
        } else {
            bgLoadOver = true;
//            setBackImageView(imageView -> loadBgImage(firstImageUrl, imageView));
        }
        //设置adapter
        setAdapter();
        notifyDataHasChanged();
        setPageChangeListener();
        setOnBannerItemClickListener(position -> {
            if (position < 0 || position >= mArrayList.size()) return;
            Map<String, String> dataMapTemp = mArrayList.get(position);
            if ("2".equals(dataMapTemp.get("isAd"))) {
                mAdControl.onAdClick(adView, 0, "");
                return;
            }
            String url = arrayList.get(position).get("url");
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
            statistic(position);
        });
        setPageChangeDuration(5 * 1000);
        setRandomItem(arrayList);
        String sort = data.get(WidgetDataHelper.KEY_SORT);
        int paddingTop = (!TextUtils.isEmpty(sort) && !"1".equals(sort)) ? Tools.getDimen(getContext(),R.dimen.dp_10)  : 0;
        setPadding(getPaddingLeft(),paddingTop,getPaddingRight(),getPaddingBottom());
        setTargetHeight(imageHeight + paddingTop + getPaddingBottom());
        setVisibility(VISIBLE);
    }

    private void statistic(int position) {
        if(mStatisticCallback != null){
            mStatisticCallback.onStatistic(id,twoLevel,threeLevel,position);
        }else{
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                if(TextUtils.isEmpty(threeLevel))
                    XHClick.mapStat(getContext(),id,twoLevel + position,"");
                else
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel+position);
            }
        }
    }

    //创建数据适配器
    private void setAdapter() {
        BannerAdapter<Map<String, String>> bannerAdapter = new BannerAdapter<Map<String, String>>(mArrayList) {
            @Override
            protected void bindTips(TextView tv, Map<String, String> stringStringMap) {
            }

            @Override
            public void bindView(View view, Map<String, String> data) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                Object tagValue = imageView.getTag(TAG_ID);
                if (tagValue != null && tagValue.equals(data.get("img"))) {
                    return;
                }
                RelativeLayout adlayout = (RelativeLayout) view.findViewById(R.id.ad_layout);
                adlayout.setPadding(
                        adlayout.getPaddingLeft(),
                        adlayout.getPaddingTop(),
                        mPointContainerLl.getMeasuredWidth() + Tools.getDimen(getContext(), R.dimen.dp_16),
                        adlayout.getPaddingBottom()
                );
                imageView.setTag(TAG_ID, data.get("img"));
                loadImage(data.get("img"), imageView);
                if ("2".equals(data.get("isAd"))) {
                    adView = view;
                    TextView textView = (TextView) view.findViewById(R.id.title);
                    textView.setText(TextUtils.isEmpty(data.get("title")) ? "" : data.get("title"));
                    ImageView icon = (ImageView) view.findViewById(R.id.ad_icon);
                    icon.setOnClickListener(v ->
                            AppCommon.setAdHintClick(XHActivityManager.getInstance().getCurrentActivity(), v, mAdControl, 0, "")
                    );
                    icon.setVisibility(VISIBLE);
                    View gdtIcon = view.findViewById(ID_AD_ICON_GDT);
                    if(gdtIcon != null){
                        gdtIcon.setVisibility(ADKEY_GDT.equals(data.get("type"))?VISIBLE:GONE);
                    }
                } else {
                    view.findViewById(R.id.ad_layout).setVisibility(GONE);
                }
            }

            @SuppressLint("InflateParams")
            @Override
            public View getView(int position) {
                return mInflater.inflate(R.layout.widget_banner_item, null, true);
            }
        };
        setBannerAdapter(bannerAdapter);
    }

    private ViewPager.OnPageChangeListener pageChangeListener;

    private void setPageChangeListener() {
        if (pageChangeListener != null) return;
        pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int[] location = new int[2];
                getLocationInWindow(location);
                if (location[1] > showMinH && location[1] < showMaxH
                        && mArrayList.contains(adMap)
                        && mArrayList.indexOf(adMap) == position
                        && !adMap.isEmpty()
                        && !adMap.containsKey(KEY_ALREADY_SHOW)) {
                    adMap.put(KEY_ALREADY_SHOW, "2");
                    mAdControl.onAdBind(0, adView, "");
                }
            }
        };
        addOnPageChangeListener(pageChangeListener);
    }

    private void loadBgImage(final String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl) || null == imageView)
            return;
        LoadImage.with(getContext())
                .load(imageUrl)
                .setSaveType(LoadImage.SAVE_LONG)
                .setPlaceholderId(0)
                .build()
                .dontAnimate()
                .dontTransform()
                .listener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, GlideUrl glideUrl, Target<Bitmap> target, boolean b) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bitmap, GlideUrl glideUrl, Target<Bitmap> target, boolean b, boolean b1) {
                        ImgManager.saveImg(imageUrl, LoadImage.SAVE_LONG);
                        return false;
                    }
                })
                .into(imageView);
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl) || null == imageView)
            return;
        LoadImage.with(getContext())
                .load(imageUrl)
                .setSaveType(LoadImage.SAVE_LONG)
                .setPlaceholderId(0)
                .build()
                .dontAnimate()
                .dontTransform()
                .into(imageView);
    }

    public void initAdData() {
        if (mAdControl == null && ToolsDevice.isNetworkAvailable(getContext())) {
            mAdControl = new XHAllAdControl(mAdIDArray,
                    (final Map<String, String> map) ->
                            Stream.of(mAdIDArray).forEach(key -> {
                                String adStr = map.get(key);
                                sendAdMessage(adStr);
                            }),
                    XHActivityManager.getInstance().getCurrentActivity(),
                    "sy_banner");
        }
    }

    protected void sendAdMessage(String adStr) {
        Map<String, String> adDataMap = StringManager.getFirstMap(adStr);
        post(() -> {
            String imageUrl = adDataMap.get("imgUrl");
            if (TextUtils.isEmpty(imageUrl)) return;
            adMap = new HashMap<>();
            adMap.put("isAd", "2");
            adMap.put("img", imageUrl);
            String title = adDataMap.get("title");
            String desc = adDataMap.get("desc");
            String text = TextUtils.equals(title, desc) ? desc : title + " | " + desc;
            adMap.put("title", text);
            adMap.put("type", adDataMap.get("type"));
            if (!mArrayList.isEmpty()
                    && !adMap.isEmpty()
                    && !mArrayList.contains(adMap)
                    && LoginManager.isShowAd()) {
                mArrayList.add(adMap);
                notifyDataHasChanged();
            }
        });
    }

    int weightSum = 0;
    int[] weightArray;

    private void setRandomItem(ArrayList<Map<String, String>> arrayList) {
        if (null == arrayList || arrayList.size() <= 1) {
            return;
        }
        weightSum = 0;
//        Log.i("tzy","setRandomItem");
        weightArray = new int[arrayList.size()];
        for (int index = 0; index < weightArray.length; index++) {
            Map<String, String> map = arrayList.get(index);
            if (map.equals(adMap)) {
                weightArray[index] = 0;
                continue;
            }
            try {
                String weightStr = map.get("weight");
                if (TextUtils.isEmpty(weightStr) || "null".equals(weightStr)) {
                    weightStr = "0";
                } else if (weightStr.indexOf(".") > 0) {
                    weightStr = weightStr.substring(0, weightStr.indexOf("."));
                }
                int currentWeight = Integer.parseInt(weightStr);
                weightSum += currentWeight;
                weightArray[index] = weightSum;
            } catch (Exception ignored) {
                weightArray[index] = weightSum;
                ignored.printStackTrace();
            }
        }
        //随机权重
        final int randomWeight = Tools.getRandom(0, weightSum);
        for (int index = 0; index < weightArray.length; index++) {
            if (randomWeight < weightArray[index]) {
                setCurrentItem(index);
//                Log.i("tzy","setCurrentItem::" + index);
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

    private int lastX = -1;
    private int lastY = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        int dealtX = 0;
        int dealtY = 0;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dealtX = 0;
                dealtY = 0;
                // 保证子View能够接收到Action_move事件
//                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                dealtX += Math.abs(x - lastX);
                dealtY += Math.abs(y - lastY);
//                Log.i("dispatchTouchEvent", "dealtX:=" + dealtX);
//                Log.i("dispatchTouchEvent", "dealtY:=" + dealtY);
                // 这里是够拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截
                if (dealtX >= dealtY) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {

                }
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

//    private void loadAdImage(String imgUrl, ImageView imageView) {
//        Glide.with(getContext())
//                .load(imgUrl)
//                .asBitmap()
//                .dontAnimate()
//                .dontTransform()
//                .into(new SubBitmapTarget() {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
//                        float width = bitmap.getWidth();
//                        float height = bitmap.getHeight();
//                        float scaleWidth = width;
//                        float scaleHeight = scaleWidth / imageWidth * height;
//                        int offsetY = (int) (height - Math.abs(height - scaleHeight));
//                        Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, offsetY, (int) scaleWidth, ((int) height - offsetY));
//                        imageView.setImageBitmap(resultBitmap);
//                    }
//                });
//    }


    @Override
    public void setAdID(List<String> adIDs) {
        if(adIDs != null){
            mAdIDArray.addAll(adIDs);
        }
    }

    @Override
    public void setStatisticCallback(StatisticCallback statisticCallback) {
        mStatisticCallback = statisticCallback;
    }
}
