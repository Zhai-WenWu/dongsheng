package amodule.main.view.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.activity.MainHome;
import amodule.main.adapter.AdapterHome;
import amodule.main.bean.HomeModuleBean;
import amodule.main.view.home.HomeFragment;
import aplug.basic.SubBitmapTarget;
import aplug.web.FullScreenWeb;
import aplug.web.ShowWeb;
import third.ad.control.AdControlParent;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.mall.activity.CommodDetailActivity;

/**
 * 首页内容列表的父Item
 * Created by sll on 2017/4/18.
 */

public class HomeItem extends BaseItemView implements BaseItemView.OnItemClickListener {

    //用户信息和置顶view
    private ImageView mTopTag;
    private ImageView mUserGourmet;
    private TextView mUserName;
    protected View mDot;
    protected TextView mTopTxt;

    protected LinearLayout mNumInfoLayout;
    protected View mTimeTagContainer;
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

    protected HomeItemBottomView mHomeItemBottomView;
    private ImageView mAdTag;

    public HomeItem(Context context, int layoutId) {
        this(context, null, layoutId);
    }

    public HomeItem(Context context, AttributeSet attrs, int layoutId) {
        this(context, attrs, 0, layoutId);
    }

    public HomeItem(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(layoutId, this, true);
        initView();
    }

    protected void initView() {
        this.setBackgroundColor(Color.parseColor("#ffffff"));
        mAdTag = (ImageView) findViewById(R.id.ad_tag);
        mLineTop = findViewById(R.id.line_top);
        mDot = findViewById(R.id.dot);
        mTopTxt = (TextView) findViewById(R.id.top_txt);
        mTimeTag = (TextView) findViewById(R.id.time_tag);
        mTopTag = (ImageView) findViewById(R.id.top_tag);
        mUserGourmet = (ImageView) findViewById(R.id.gourmet_icon);
        mUserName = (TextView) findViewById(R.id.user_name);
        mNameGourmet = (LinearLayout) findViewById(R.id.name_gourmet);
        mNumInfoLayout = (LinearLayout) findViewById(R.id.numInfoLayout);
        mHomeItemBottomView = new HomeItemBottomView(getContext());
        if (mNumInfoLayout != null)
            mNumInfoLayout.addView(mHomeItemBottomView);
        mHomeItemBottomView.setVisibility(View.GONE);

        mTimeTagContainer = findViewById(R.id.time_tag_container);

    }

    private void addInnerListener() {
        if (mAdTag != null)
            mAdTag.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickEvent(v);
                }
            });
    }

    /**
     * 点击事件，由外界或者item内部的view点击事件调用，此写法是解决有些手机偶尔出现点击事件不响应的问题。
     *
     * @param v 点击的view
     */
    public void onClickEvent(View v) {
        if (mIsAd) {
            if (v == mAdTag) {
                onAdHintClick();
            } else if (v == this) {
                if (mAdControlParent != null) {
                    mAdControlParent.onAdClick(mDataMap);
                }
            }
            return;
        }
        if (!handleClickEvent(v)) {
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), mTransferUrl, true);
            onItemClick();
        }
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

    @Override
    protected SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                if (v.getTag(TAG_ID) == null || !v.getTag(TAG_ID).equals(url))
                    return;
                if (mCallback != null) {
                    if (mCallback != null)
                        mCallback.callback(bitmap);
                    return;
                }
                if (bitmap != null) {
                    v.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onLoadFailed(final Exception e, Drawable drawable) {
                super.onLoadFailed(e, drawable);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BuglyLog.i("image", "url = " + url + "  netStatus = " + ToolsDevice.getNetWorkSimpleType(getContext()));
                        CrashReport.postCatchedException(e);
                    }
                }).start();
            }
        };
    }

    public void setRefreshTag(AdapterHome.ViewClickCallBack callBack) {
        this.mRefreshCallBack = callBack;
    }

    /**
     * 处理Item点击事件外的其他事件。
     *
     * @param view 点击的Item
     *
     * @return 如果返回false，表示外界不处理点击事件，将会自己处理点击事件；
     */
    private boolean handleClickEvent(View view) {
        if (!TextUtils.isEmpty(mTransferUrl)) {
            if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                if (!mTransferUrl.contains("data_type=") && !mTransferUrl.contains("module_type=")) {
                    if (!mTransferUrl.startsWith("http")) {
                        if (mTransferUrl.contains("?"))
                            mTransferUrl += "&data_type=" + mDataMap.get("type");
                        else
                            mTransferUrl += "?data_type=" + mDataMap.get("type");
                        mTransferUrl += "&module_type=" + (isTopTypeView() ? "top_info" : "info");
                    }
                }
                Log.i("zhangyujian", "点击：" + mDataMap.get("code") + ":::" + mTransferUrl);
                XHClick.saveStatictisFile("home", getModleViewType(), mDataMap.get("type"), mDataMap.get("code"), "", "click", "", "", String.valueOf(mPosition + 1), "", "");
            }
            if (mTransferUrl.contains("dishInfo.app")
                    && !TextUtils.isEmpty(mDataMap.get("type"))
                    && !"2".equals(mDataMap.get("type"))) {
                mTransferUrl += "&img=" + mDataMap.get("img");
            }
            String params = mTransferUrl.substring(mTransferUrl.indexOf("?") + 1, mTransferUrl.length());
            Log.i("zhangyujian", "mTransferUrl:::" + params);
            Map<String, String> map = StringManager.getMapByString(params, "&", "=");
            Class c = null;
            Intent intent = new Intent();
            if (mTransferUrl.startsWith("http")) {//我的香豆、我的会员页面
                if (mTransferUrl.contains("fullScreen=2")) {
                    c = FullScreenWeb.class;
                    intent.putExtra("url", mTransferUrl);
                    intent.putExtra("code", map.containsKey("code") ? map.get("code") : "");
                } else {
                    c = ShowWeb.class;
                    intent.putExtra("url", mTransferUrl);
                    intent.putExtra("code", map.get("code"));
                }
            } else if (mTransferUrl.contains("xhds.product.info.app?")) {//商品详情页，原生
                c = CommodDetailActivity.class;
                for(String key : map.keySet()){//取全部参数。
                    intent.putExtra(key, map.get(key));
                }
            } else if (mTransferUrl.contains("nousInfo.app")) {
                c = ShowWeb.class;
                intent.putExtra("url", StringManager.api_nouseInfo);
                intent.putExtra("code", map.get("code"));
            }
            if (c != null) {
                intent.putExtra("data_type", mDataMap.get("type"));
                intent.putExtra("module_type", isTopTypeView() ? "top_info" : "info");
                intent.setClass(getContext(), c);
                XHActivityManager.getInstance().getCurrentActivity().startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public interface ADImageLoadCallback {
        void callback(Bitmap bitmap);
    }

    /**
     * 设置广告控制器，在setData前设置。
     *
     * @param adControlParent 广告控制器
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
        //设置监听
        if (mTimeTagContainer != null)
            mTimeTagContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRefreshCallBack != null)
                        mRefreshCallBack.viewOnClick(true);
                    if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType()))
                        XHClick.mapStat(getContext(), "a_recommend", "刷新效果", "点击【点击刷新】按钮");
                }
            });
        addInnerListener();
        //设置数据
        if (mDataMap != null) {
            initData();
            //统计---只展示一次，isShow 2已经显示
            if (mModuleBean != null
                    && MainHome.recommedType.equals(mModuleBean.getType())
                    && !TextUtils.isEmpty(mDataMap.get("code"))
                    && (!mDataMap.containsKey("isShowStatistic") || "1".equals(mDataMap.get("isShowStatistic")))) {//保证推荐模块类型
                Log.i("zhangyujian", "展示曝光数据::" + mDataMap.get("name") + "::::" + mDataMap.get("type") + "::position:::" + position);
                XHClick.saveStatictisFile("home", getModleViewType(), mDataMap.get("type"), mDataMap.get("code"), "", "show", "", "", String.valueOf(mPosition + 1), "", "");
                mDataMap.put("isShowStatistic", "2");
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
        if (mIsAd) {
            if (mAdControlParent != null && !mDataMap.containsKey("isADShow")) {
                mAdControlParent.onAdShow(mDataMap, this);
                mDataMap.put("isADShow", "1");
            }
            if (mAdTag != null && (!mDataMap.containsKey("adType") || !"1".equals(mDataMap.get("adType"))))
                mAdTag.setVisibility(View.VISIBLE);
            else if (mAdTag != null)
                mAdTag.setVisibility(View.GONE);
        } else {
            if (mAdTag != null)
                mAdTag.setVisibility(View.GONE);
        }
        if (mDataMap.containsKey("type")) {
            mType = mDataMap.get("type");
        }
        if (mDataMap.containsKey("url"))
            mTransferUrl = mDataMap.get("url");
        mHomeItemBottomView.setData(StringManager.getListMapByJson(mDataMap.get("numInfo")));
    }

    protected void resetView() {
        if (mTimeTagContainer != null)
            mTimeTagContainer.setVisibility(View.GONE);
        if (mUserName != null)
            mUserName.setVisibility(View.GONE);
        if (mUserGourmet != null)
            mUserGourmet.setVisibility(View.GONE);
        if (mNameGourmet != null)
            mNameGourmet.setVisibility(View.GONE);
        if (mTopTag != null)
            mTopTag.setVisibility(View.GONE);
        if (mDot != null)
            mDot.setVisibility(View.GONE);
        if (mTopTxt != null)
            mTopTxt.setVisibility(View.GONE);
        if (mLineTop != null) {
            mLineTop.setVisibility(mPosition > 0 ? View.VISIBLE : View.GONE);
        }
    }

    protected void resetData() {
        mIsAd = false;
        mIsTop = false;
        mTransferUrl = null;
        mType = "";
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
                    case MainHome.recommedType:
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
                    XHClick.mapStat(getContext(), eventId, twoLevel, "点击" + (mPosition + 1) + "位置");
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
                    case MainHome.recommedType:
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
                    mAdControlParent.onAdHintClick((Activity) getContext(), mDataMap, eventId, "点击" + mModuleBean.getTitle() + "【广告】icon");
            }
        }
    }

    /**
     * 获取当前数据类型：用于统计
     *
     * @return view类型
     */
    public String getModleViewType() {
        return isTopTypeView() ? "top" : MainHome.recommedType_statictus;
    }

    /**
     * 是否是置顶数据类型
     *
     * @return true 是置顶数据 ：false不是
     */
    public boolean isTopTypeView() {
        return !TextUtils.isEmpty(viewType) && HomeFragment.MODULETOPTYPE.equals(viewType);
    }

    /**
     * 获取当前Item的数据体类型
     *
     * @return 当前Item的数据体类型
     */
    public String getDataType() {
        return mType;
    }

    /**
     * 获取广告图片的大小。
     *
     * @param size  一个两个整数的数组
     * @param style 广告样式 适用于大图样式1和任意图样式6的广告
     */
    protected void getADImgSize(int[] size, String style) {
        if (mDataMap == null
                || (!"1".equals(mDataMap.get("style")) && !"6".equals(mDataMap.get("style"))))
            return;
        Map<String, String> mapSize = XHScrollerAdParent.getAdImageSize(mDataMap.get("adClass"), mDataMap.get("stype"), "1");
        if (mapSize == null || mapSize.isEmpty())
            return;
        try {
            size[0] = Integer.parseInt(mapSize.get("width"));
            size[1] = Integer.parseInt(mapSize.get("height"));
            int fixedWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - getResources().getDimensionPixelSize(R.dimen.dp_40);
            size[1] = size[1] * fixedWidth / size[0];
            size[0] = fixedWidth;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
