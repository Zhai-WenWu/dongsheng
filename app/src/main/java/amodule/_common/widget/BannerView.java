package amodule._common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
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
import amodule._common.delegate.ISetAdController;
import amodule._common.delegate.ISetAdID;
import amodule._common.delegate.ISetShowIndex;
import amodule._common.delegate.ISetStatisticPage;
import amodule._common.delegate.IStatictusData;
import amodule._common.delegate.IStatisticCallback;
import amodule._common.delegate.IUpdatePadding;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.internet.img.transformation.RoundTransformation;
import xh.basic.tool.UtilImage;

/**
 * Description :
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerView extends Banner implements IBindMap, IStatictusData, ISaveStatistic, IHandlerClickEvent,ISetAdID
        ,IStatisticCallback,ISetStatisticPage, ISetAdController, ISetShowIndex, IUpdatePadding {
    protected LayoutInflater mInflater;
    private ArrayList<String> mAdIDArray = new ArrayList<>();
    private int showMinH, showMaxH;
    private XHAllAdControl mAdControl;
    private Map<Integer, View> mAdViews = new HashMap<>();
    public static final int TAG_ID = R.string.tag;
    int imageHeight = 0, imageWidth = 0;
    private StatisticCallback mStatisticCallback;
    private int mShowIndex = -1;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setViewSize(context);
        setDefault();
    }

    private void setDefault() {
        setPageChangeListener();
        setPageChangeDuration(5 * 1000);
    }

    private void setViewSize(Context context) {
        int paddingBottom = getResources().getDimensionPixelSize(R.dimen.dp_10);
        updatePadding(0, 0, 0, paddingBottom);
        mInflater = LayoutInflater.from(context);
        imageWidth = ToolsDevice.getWindowPx(context).widthPixels;
        imageHeight = (int) (imageWidth * 280 / 750f);
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
        if (data == null || data.isEmpty()) {
            mArrayList.clear();
            setVisibility(View.GONE);
            return;
        }
        //设置adapter
        setAdapter();
        setOnBannerItemClickListener(position -> {
            if (position < 0 || position >= mArrayList.size()) return;
            Map<String, String> dataMapTemp = mArrayList.get(position);
            if (dataMapTemp.containsKey("adPosId")) {
                int index = -1;
                try{
                    index = Integer.parseInt(dataMapTemp.get("realIndex"));
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (mAdControl != null)
                    mAdControl.onAdClick(mAdViews.get(position), index, String.valueOf(index + 1));
                return;
            }
            String url = dataMapTemp.get("url");
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
            if(!TextUtils.isEmpty(page)){
                XHClick.saveStatictisFile(page, "homeBannerScroll", dataMapTemp.get("type"), "", "",
                        "click", "", "", "", "", "");
            }
            statistic(position);
        });

        int paddingTop = (mShowIndex != -1 && mShowIndex != 0) ? Tools.getDimen(getContext(),R.dimen
                .dp_10)
                : 0;
        updatePadding(getPaddingLeft(),paddingTop,getPaddingRight(),getPaddingBottom());
        setTargetHeight(imageHeight + paddingTop + getPaddingBottom());

        Map<String, String> dataMap = StringManager.getFirstMap(data.get(WidgetDataHelper.KEY_DATA));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get(WidgetDataHelper.KEY_LIST));

        if (arrayList.isEmpty()) {
            setVisibility(View.GONE);
            mArrayList.clear();
            return;
        }
        if (arrayList.equals(mArrayList)) {
            return;
        }
        //重新添加数据
        mArrayList.clear();
        mArrayList.addAll(arrayList);
        notifyDataHasChanged();
        setRandomItem(arrayList);
        setVisibility(VISIBLE);
    }

    private void statistic(int position) {
        if(mStatisticCallback != null){
            mStatisticCallback.onStatistic(id,twoLevel,threeLevel+(position + 1),position);
        }else{
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)) {
                if(TextUtils.isEmpty(threeLevel))
                    XHClick.mapStat(getContext(),id,twoLevel + (position + 1),"");
                else
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel+(position + 1));
            }
        }
    }

    //创建数据适配器
    private void setAdapter() {
        mAdViews.clear();
        BannerAdapter<Map<String, String>> bannerAdapter = new BannerAdapter<Map<String, String>>(mArrayList) {
            @Override
            protected void bindTips(TextView tv, Map<String, String> stringStringMap) {
            }

            @Override
            public void bindView(View view, Map<String, String> data) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                Object tagValue = imageView.getTag(TAG_ID);
                String img = data.get("img");
                if (tagValue != null && tagValue.equals(img)) {
                    return;
                }
                imageView.setTag(TAG_ID, img);
                loadImage(img, imageView);
            }

            @SuppressLint("InflateParams")
            @Override
            public View getView(int position) {
                return BannerView.this.getView(position);
            }
        };
        setBannerAdapter(bannerAdapter);
    }

    protected View getView(int position) {
        return mInflater.inflate(R.layout.widget_banner_item, null, true);
    }

    private ViewPager.OnPageChangeListener pageChangeListener;

    private void setPageChangeListener() {
        if (pageChangeListener != null) return;
        pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int[] location = new int[2];
                getLocationInWindow(location);
                Map<String, String> adMap = isAdByPos(position);
                if (location[1] > showMinH && location[1] < showMaxH
                        && adMap != null) {
                    int index = -1;
                    try{
                        index = Integer.parseInt(adMap.get("realIndex"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (mAdControl != null)
                        mAdControl.onAdBind(index, mAdViews.get(position), String.valueOf(index + 1));
                }
            }
        };
        addOnPageChangeListener(pageChangeListener);
    }

    private Map<String, String> isAdByPos(int position) {
        Map<String, String> ret = null;
        if (mArrayList == null || mArrayList.isEmpty() || mArrayList.size() - 1 < position)
            return ret;
        ret = mArrayList.get(position);
        if (ret.containsKey("adPosId"))
            return ret;
        return null;
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl) || null == imageView)
            return;
        imageView.setTag(TAG_ID, imageUrl);
        LoadImage.with(getContext())
                .load(imageUrl)
                .setSaveType(LoadImage.SAVE_LONG)
                .setPlaceholderId(0)
                .build()
                .dontAnimate()
                .dontTransform()
                .into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (imageView != null && imageUrl.equals(imageView.getTag(TAG_ID)) && bitmap != null) {
                            float radi = getResources().getDimensionPixelSize(R.dimen.dp_5);
                            float[] radii = new float[] {radi, radi, radi, radi, radi, radi, radi, radi};
                            Bitmap result = genBitmap(bitmap, radii);
                            if (result != null) {
                                imageView.setImageBitmap(result);
                            }


//                            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//                            Canvas c = new Canvas(bm);
//                            Path path = new Path();
//                            path.addRoundRect(new RectF(0, 0, w, h), radiusArray, Path.Direction.CW);
//                            Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                            bitmapPaint.setColor(Color.GREEN); // 颜色随意，不要有透明度。
//                            c.drawPath(path, bitmapPaint);
                        }
                    }
                });
    }

    private Bitmap genBitmap(Bitmap bitmap, float[] radii) {
        Bitmap target = null;
        Path path = new Path();
        float[] radiis = new float[]{radii[0], radii[0], radii[1], radii[1], radii[2], radii[2], radii[3], radii[3]};
        try {
            if(bitmap == null)
                return null;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            target = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(target);
            RectF rectf = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawARGB(0, 0, 0, 0);
            path.reset();
            if(radii[0] == 0 && radii[1] == 0 && radii[2] == 0 && radii[3] == 0)
                path.addRect(rectf, Path.Direction.CW);
            else
                path.addRoundRect(rectf, radiis, Path.Direction.CW);
            canvas.drawPath(path, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, 0, 0, paint);
        } catch(OutOfMemoryError e) {}
        return target;
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
            if (TextUtils.equals("2", map.get("isAd"))) {
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
    public void saveStatisticData(String page) {

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

    String page = "";
    @Override
    public void setStatisticPage(String page) {
        this.page = page;
    }

    @Override
    public void setAdController(XHAllAdControl controller) {
        mAdControl = controller;
    }

    @Override
    public void setShowIndex(int showIndex) {
        mShowIndex = showIndex;
    }

    @Override
    public void updatePadding(int l, int t, int r, int b) {
        setPadding(l, t, r, b);
    }
}
