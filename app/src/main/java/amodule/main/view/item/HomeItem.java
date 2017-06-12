package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.activity.MainHome;
import amodule.main.adapter.AdapterHome;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.home.HomeFragment;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.control.AdControlParent;
import xh.basic.tool.UtilImage;

/**
 * 首页内容列表的父Item
 * Created by sll on 2017/4/18.
 */

public class HomeItem extends BaseItemView implements BaseItemView.OnItemClickListener {

    private final int TAG_ID = R.string.tag;
    private int mImgResource = R.drawable.i_nopic;
    private int mRoundImgPixels = 0, mImgWidth = 0, mImgHeight = 0,// 以像素为单位
            mRoundType = 1; // 1为全圆角，2上半部分圆角
    private boolean mImgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    private String mImgLevel = FileManager.save_cache; // 图片保存等级
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;
    private boolean mIsAnimate = false;// 控制图片渐渐显示

    //用户信息和置顶view
    private ImageView mTopTag;
    private ImageView mUserGourmet;
    private TextView mUserName;
    protected View mDot;
    protected TextView mTopTxt;

    protected LinearLayout mTimeTagContainer;
    protected TextView mTimeTag;
    protected View mLineTop;

    protected LinearLayout mNameGourmet;

    protected AdControlParent mAdControlParent;

    protected boolean mIsAd;
    protected boolean mIsTop;

    protected String mTransferUrl;
    protected String mType;

    protected HomeModuleBean mModuleBean;
    private AdapterHome.ViewClickCallBack mRefreshCallBack;

    public HomeItem(Context context, int layoutId) {
        super(context);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    public HomeItem(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    public HomeItem(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    protected void initView() {
        this.setBackgroundColor(Color.parseColor("#ffffff"));
        mLineTop = findViewById(R.id.line_top);
        mDot = findViewById(R.id.dot);
        mTopTxt = (TextView) findViewById(R.id.top_txt);
        mTimeTag = (TextView) findViewById(R.id.time_tag);
        mTopTag = (ImageView) findViewById(R.id.top_tag);
        mUserGourmet = (ImageView) findViewById(R.id.gourmet_icon);
        mUserName = (TextView) findViewById(R.id.user_name);
        mNameGourmet = (LinearLayout) findViewById(R.id.name_gourmet);

        mTimeTagContainer = (LinearLayout) findViewById(R.id.time_tag_container);
        if (mTimeTagContainer != null)
            mTimeTagContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRefreshCallBack != null)
                        mRefreshCallBack.viewOnClick(true);
                    if (mModuleBean != null && "recom".equals(mModuleBean.getType()))
                        XHClick.mapStat((Activity) getContext(), "a_recommend", "刷新效果", "点击【点击刷新】按钮");
                }
            });
    }

    private ADImageLoadCallback mCallback;
    protected void loadImage(String url, ImageView view, ADImageLoadCallback callback) {
        mCallback = callback;
        loadImage(url, view);
    }

    protected void loadImage(String url, ImageView view) {
        if (view == null || TextUtils.isEmpty(url))
            return;
        setViewImage(view, url);
    }

    public void setRefreshTag(AdapterHome.ViewClickCallBack callBack) {
        this.mRefreshCallBack = callBack;
    }

    private void setViewImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                mRoundImgPixels = ToolsDevice.dp2px(v.getContext(), 500);
                v.setImageResource(R.drawable.bg_round_user_icon);
            } else {
                v.setImageResource(mImgResource);
            }
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (value.length() < 10)
                return;
            v.setTag(TAG_ID, value);
            if (v.getContext() == null) return;
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, mRoundType, mRoundImgPixels);
            UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
        }
        // 隐藏
        else if (value.equals("hide") || value.length() == 0)
            v.setVisibility(View.GONE);
            // 直接加载本地图片
        else if (!value.equals("ignore")) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setImageResource(mImgResource);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(v.getContext())
                    .load(value)
                    .setSaveType(mImgLevel)
                    .build();
            if (bitmapRequest != null)
                bitmapRequest.into(getTarget(v, value));
        }
        // 如果为ignore,则忽略图片
    }

    private SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (mCallback != null) {
                    if (mCallback != null)
                        mCallback.callback(bitmap);
                    return;
                }
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应auther_userImg
                    if (v.getId() == R.id.iv_userImg || v.getId() == R.id.auther_userImg) {
                        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
//						bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, 1, ToolsDevice.dp2px(context, 500));
//                        v.setImageBitmap(UtilImage.makeRoundCorner(bitmap));
                        v.setImageBitmap(UtilImage.toRoundCorner(v.getResources(),bitmap,1,500));
                    } else {
                        v.setScaleType(mScaleType);
                        UtilImage.setImgViewByWH(v, bitmap, mImgWidth, mImgHeight, mImgZoom);
                        if (mIsAnimate) {
//							AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
//							alphaAnimation.setDuration(300);
//							v.setAnimation(alphaAnimation);
                        }
                    }
                }
            }
        };
    }

    public interface ADImageLoadCallback {
        void callback(Bitmap bitmap);
    }

    /**
     * 设置广告控制器，在setData前设置。
     * @param adControlParent
     */
    public void setAdControl(AdControlParent adControlParent) {
        this.mAdControlParent = adControlParent;
    }

    public void setData(Map<String, String> dataMap) {
        this.setData(dataMap, -1);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap != null) {
            initData();
            //统计---只展示一次，isShow 2已经显示
            if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())&&!TextUtils.isEmpty(mDataMap.get("code"))&&(!mDataMap.containsKey("isShowStatistic")||"1".equals(mDataMap.get("isShowStatistic")))) {//保证推荐模块类型
                Log.i("zhangyujian","展示曝光数据::"+mDataMap.get("name")+"::::"+mDataMap.get("type")+"::position:::"+position);
                XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","show","","",String.valueOf(mPosition+1),"","");
                mDataMap.put("isShowStatistic","2");
            }
        }
    }

    private void initData() {
        resetData();
        resetView();
        //时间标签

        if (mDataMap.containsKey("refreshTime")) {
            String refreshTime = mDataMap.get("refreshTime");
            if (!TextUtils.isEmpty(refreshTime) && mTimeTag != null) {
                mTimeTag.setText(refreshTime + "看到这里,点击刷新");
                mTimeTag.setVisibility(View.VISIBLE);
                if (mTimeTagContainer != null)
                    mTimeTagContainer.setVisibility(View.VISIBLE);
                if (mLineTop != null)
                    mLineTop.setVisibility(View.GONE);
            }
        }
        if (mDataMap.containsKey("isTop")) {
            String isTop = mDataMap.get("isTop");
            if (!TextUtils.isEmpty(isTop) && isTop.equals("2")) {
                mIsTop = true;
                if (mTopTag != null)
                    mTopTag.setVisibility(View.VISIBLE);
                else if (mTopTxt != null) {
                    mTopTxt.setVisibility(View.VISIBLE);
                    if (mDot != null)
                        mDot.setVisibility(View.VISIBLE);
                }
            }
        }
        //用户信息
        if (mDataMap.containsKey("customer")) {
            String customer = mDataMap.get("customer");
            if (!TextUtils.isEmpty(customer)) {
                ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(customer);
                for (Map<String, String> map : maps) {
                    boolean showNameGourmet = false;
                    if (map != null) {
                        if (map.containsKey("nickName")) {
                            String nickName = map.get("nickName");
                            if (!TextUtils.isEmpty(nickName) && mUserName != null) {
                                mUserName.setText(nickName);
                                mUserName.setVisibility(View.VISIBLE);
                                showNameGourmet = true;
                            }
                        }
                        if (map.containsKey("isGourmet")) {
                            String isGourmet = map.get("isGourmet");
                            if (!TextUtils.isEmpty(isGourmet) && mUserGourmet != null && Integer.parseInt(isGourmet) == 2) {
                                mUserGourmet.setVisibility(View.VISIBLE);
                                showNameGourmet = true;
                            }
                        }
                        if (showNameGourmet && mNameGourmet != null)
                            mNameGourmet.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if (mDataMap.containsKey("adstyle")) {
            String adStyle = mDataMap.get("adstyle");
            if (!TextUtils.isEmpty(adStyle) && adStyle.equals("ad")) {
                mIsAd = true;
            }
        }
        if (mDataMap.containsKey("type")) {
            mType = mDataMap.get("type");
        }
        if (mDataMap.containsKey("url"))
            mTransferUrl = mDataMap.get("url");
    }

    protected void resetView() {
        if (viewIsVisible(mTimeTagContainer))
            mTimeTagContainer.setVisibility(View.GONE);
        if (viewIsVisible(mUserName))
            mUserName.setVisibility(View.GONE);
        if (viewIsVisible(mUserGourmet))
            mUserGourmet.setVisibility(View.GONE);
        if (viewIsVisible(mNameGourmet))
            mNameGourmet.setVisibility(View.GONE);
        if (viewIsVisible(mTopTag))
            mTopTag.setVisibility(View.GONE);
        if (viewIsVisible(mDot))
            mDot.setVisibility(View.GONE);
        if (viewIsVisible(mTopTxt))
            mTopTxt.setVisibility(View.GONE);
        if (mLineTop != null) {
            mLineTop.setVisibility(mPosition > 0 ? View.VISIBLE : View.GONE);
        }
    }

    protected void resetData() {
        mIsAd = false;
        mIsTop = false;
        mTransferUrl = null;
        mType = null;
    }

    public void setHomeModuleBean(HomeModuleBean bean) {
        mModuleBean = bean;
    }

    @Override
    public void onItemClick() {
        commonStatistics();
    }

    /**
     * 普通点击位置的统计
     */
    private void commonStatistics() {
        if (mModuleBean != null && !mIsAd) {
            String type = mModuleBean.getType();
            if (!TextUtils.isEmpty(type)) {
                String eventId = "";
                String twoLevel = "";
                switch (type) {
                    case "recom":
                        eventId = "a_recommend";
                        twoLevel = "点击列表内容";
                        break;
                    case "video":
                        eventId = "a_video";
                        twoLevel = "视频列表内容";
                        break;
                    case "article":
                        eventId = "a_article";
                        twoLevel = "文章列表内容";
                        break;
                    case "day":
                        eventId = "a_meals_recommend";
                        String twoType = mModuleBean.getTwoType();
                        if (!TextUtils.isEmpty(twoType)) {
                            switch (twoType) {
                                case "1":
                                    twoLevel = "早餐列表内容";
                                    break;
                                case "2":
                                    twoLevel = "午餐列表内容";
                                    break;
                                case "3":
                                    twoLevel = "晚餐列表内容";
                                    break;
                            }
                        }

                        break;
                    case "dish":
                        eventId = "a_Exproduct";
                        twoLevel = "本周佳作列表内容";
                        break;
                }
                if (!TextUtils.isEmpty(eventId) && !TextUtils.isEmpty(twoLevel))
                    XHClick.mapStat((Activity) getContext(), eventId, twoLevel, "点击" + (mPosition + 1) + "位置");
            }
        }
    }

    /**
     * 广告标签的点击统计
     */
    protected void onAdHintClick() {
        if (mModuleBean != null) {
            String type = mModuleBean.getType();
            if (!TextUtils.isEmpty(type)) {
                String eventId = "";
                switch (type) {
                    case "recom":
                        eventId = "a_recommend_adv";
                        break;
                    case "video":
                        eventId = "a_video";
                        break;
                    case "article":
                        eventId = "a_article";
                        break;
                    case "day":
                        eventId = "a_meals_recommend";
                        break;
                    case "dish":
                        eventId = "a_Exproduct";
                        break;
                }
                if (mAdControlParent != null)
                    mAdControlParent.onAdHintClick((Activity)getContext(), mDataMap, eventId, "点击" + mModuleBean.getTitle() + "【广告】icon");
            }
        }
    }

    /**
     * 获取当前数据类型：用于统计
     * @return
     */
    public String getModleViewType(){
        if(!TextUtils.isEmpty(viewType)&& HomeFragment.MODULETOPTYPE.equals(viewType)){
            return "top";
        }
        return "recom";
    }

    /**
     * 是否是置顶数据类型
     * @return
     */
    public boolean isTopTypeView(){
        if(!TextUtils.isEmpty(viewType)&& HomeFragment.MODULETOPTYPE.equals(viewType)){
            return true;
        }
        return false;
    }
}
