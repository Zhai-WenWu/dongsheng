package amodule.topic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.XHApplication;
import acore.logic.stat.intefaces.OnItemClickListenerRvStat;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvGridView;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.topic.adapter.TopicInfoStaggeredAdapter;
import amodule.topic.adapter.TopicTabHolder;
import amodule.topic.model.ImageModel;
import amodule.topic.model.LabelModel;
import amodule.topic.model.TopicItemModel;
import amodule.topic.model.VideoModel;
import amodule.topic.view.TopicHeaderView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.aliyun.work.AliyunCommon;

public class TopicInfoActivity extends BaseAppCompatActivity {
    public static final String STA_ID = "a_topic_gather";

    private static final String TOPIC_CODE = "topicCode";
    private static final String ACTIVIT_TYPE = "activityType";

    private final String HOT = "hot";
    private final String NEW = "new";
    private String mTab = "hot";

    private TextView mTitle;
    private ImageView mBackImg;
    private RvGridView mStaggeredGridView;
    private ImageView mFloatingButton;
    private TopicHeaderView mTopicHeaderView;

    private String mTopicCode;
    private String mActivityType;

    private int mPage;
    private int mHotPage;
    private int mNewPage;
    private Map<String, String> mInfoMap;
    private boolean mTopicListLoading;

    private TopicInfoStaggeredAdapter mTopicInfoStaggeredAdapter;
    private ArrayList<TopicItemModel> mHotDatas;
    private ArrayList<TopicItemModel> mNewDatas;
    private ArrayList<TopicItemModel> mDatas;
    private LinearLayout mTitleLayout;
    private String title;
    private TopicItemModel topicItemModelTab;
    private int tabPosition;
    private int mDistance;
    private int imageHeight;
    private String activityType;
    private int headerHeight;
    private FrameLayout mHotView;
    private FrameLayout mNewView;
    private TextView mHotTabTv;
    private View mHotTabBottomView;
    private TextView mNewTabTV;
    private View mNewTabBottomView;
    private TopicTabHolder topicTabHolder;
    private View mTabLayout;
    private boolean topImgIsReal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initActivity("", 2, 0, 0, R.layout.topic_info_layout);
        setContentView(R.layout.topic_info_layout);
        level = 2;
        setCommonStyle();
//        initStatusBar();
        initTitle();
        initView();
        initData();
        if (!checkCondition()) {
            Toast.makeText(this, "参数不正确", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadTopicInfo();
        startLoadData();

    }

    private void initTitle() {
        if(Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
            int statusBarHeight = Tools.getStatusBarHeight(this);

            RelativeLayout rela_bar_title = findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, topbarHeight + statusBarHeight);
            rela_bar_title.setLayoutParams(layout);
            RelativeLayout bar_title = findViewById(R.id.title_layout);
            layout = (RelativeLayout.LayoutParams) bar_title.getLayoutParams();
            layout.setMargins(0, statusBarHeight, 0, 0);
            bar_title.setLayoutParams(layout);
        }
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            mTopicCode = i.getStringExtra(TOPIC_CODE);
        }

        mHotDatas = new ArrayList<>();
        mNewDatas = new ArrayList<>();
        mDatas = new ArrayList<>();
        mTopicInfoStaggeredAdapter = new TopicInfoStaggeredAdapter(TopicInfoActivity.this, mDatas);
        mTopicInfoStaggeredAdapter.setOnTabClick(this::setTabClick);

        tabPosition = 0;
    }

    private void initView() {
        mTitle = findViewById(R.id.title);
        mTabLayout = findViewById(R.id.tab_layout);
        mHotView = mTabLayout.findViewById(R.id.fl_hot);
        mNewView = mTabLayout.findViewById(R.id.fl_new);
        mHotTabTv = mTabLayout.findViewById(R.id.tv_hot_tab);
        mHotTabBottomView = mTabLayout.findViewById(R.id.view_hot_tab_bottom);
        mNewTabTV = mTabLayout.findViewById(R.id.tv_new_tab);
        mNewTabBottomView = mTabLayout.findViewById(R.id.view_new_tab_bottom);
        mBackImg = findViewById(R.id.back_img);
        mBackImg.setOnClickListener(v -> {
            finish();
        });
        mStaggeredGridView = findViewById(R.id.staggered_view);
        mStaggeredGridView.closeDefaultAnimator();
        mStaggeredGridView.setIsFillRowCallback(new RvGridView.IsFillRowCallback() {
            @Override
            public boolean isFillRowCallback(int position) {
                int itemViewType = mTopicInfoStaggeredAdapter.getItemViewType(position);
                return mTopicInfoStaggeredAdapter != null
                        && (itemViewType == TopicInfoStaggeredAdapter.ITEM_TAB || itemViewType == TopicInfoStaggeredAdapter.ITEM_ACTIVITY_IMG);
            }
        });
        mFloatingButton = findViewById(R.id.floating_btn);
        mTitleLayout = findViewById(R.id.title_container);

        mHotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabClick(TopicItemModel.TAB_HOT);
                topicTabHolder = mTopicInfoStaggeredAdapter.getTopicTabHolder();
                if (topicTabHolder != null) {
                    topicTabHolder.setHotClick();
                }
            }
        });

        mNewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTabClick(TopicItemModel.TAB_NEW);
                topicTabHolder = mTopicInfoStaggeredAdapter.getTopicTabHolder();
                if (topicTabHolder != null) {
                    topicTabHolder.setNewClick();
                }
            }
        });

        mStaggeredGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                if (params.getSpanSize() == mStaggeredGridView.getSpanCount()) {
                    super.getItemOffsets(outRect, view, parent, state);
                    return;
                }
                switch (params.getSpanIndex()) {
                    case 0:
                        outRect.set(0, dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5));
                        break;
                    case 1:
                        outRect.set(dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5));
                        break;
                    case 2:
                        outRect.set(dp2px(R.dimen.dp_0_5), dp2px(R.dimen.dp_0_5), 0, dp2px(R.dimen.dp_0_5));
                        break;
                    default:
                        super.getItemOffsets(outRect, view, parent, state);
                        break;
                }
            }
        });
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mTopicCode) || mInfoMap == null ||
                        mInfoMap.isEmpty() || TextUtils.isEmpty(mInfoMap.get("name")))
                    return;
                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "参与按钮"));
                AliyunCommon.getInstance().startRecord(TopicInfoActivity.this, mTopicCode, mInfoMap.get("name"));
            }
        });


        int[] locationDong = new int[2];
        int[] locationJing = new int[2];
        mDistance = 0;

        View titleBg = findViewById(R.id.title_bg);
        int screenW = ToolsDevice.getWindowPx(TopicInfoActivity.this).widthPixels;
        int screenH = ToolsDevice.getWindowPx(TopicInfoActivity.this).heightPixels;
        int itemW = screenW / 3;
        int itemH = itemW * 165 / 124;

        mStaggeredGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private boolean ismHiddenActionstart;
            private View tabItemView;
            private int lastItemPosition;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mDistance += dy;
                if (!mStaggeredGridView.canScrollVertically(-1)) {
                    mDistance = 0;
                }
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                //判断是当前layoutManager是否为LinearLayoutManager
                // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    //获取最后一个可见view的位置
                    lastItemPosition = linearManager.findLastVisibleItemPosition();
                }

                //tab可见
                topicTabHolder = mTopicInfoStaggeredAdapter.getTopicTabHolder();
                if (topicTabHolder != null) {
                    tabItemView = topicTabHolder.itemView;
                }


//                if (tabPosition < lastItemPosition && tabItemView != null) {
//                    tabItemView.getLocationOnScreen(locationDong);
//                    mTabLayout.getLocationOnScreen(locationJing);
//                    if (locationDong[1] <= locationJing[1]) {
//                        mTabLayout.setVisibility(View.VISIBLE);
//                    } else {
//                        mTabLayout.setVisibility(View.GONE);
//                    }
//                } else {
//                    mTabLayout.setVisibility(View.GONE);
//                }

                int screenW = ToolsDevice.getWindowPx(TopicInfoActivity.this).widthPixels;
                if (activityType != null && topImgIsReal && headerHeight == 0) {
                    if (activityType.equals("2")) {
                        Map<String, String> activityInfo = StringManager.getFirstMap(mInfoMap.get("activityInfo"));
                        String url = activityInfo.get("url");
                        String imageWidth = activityInfo.get("imageWidth");
                        String imageHeight = activityInfo.get("imageHeight");
                        int w = Integer.parseInt(imageWidth);
                        int h = Integer.parseInt(imageHeight);
                        float f = (float) screenW / w;
                        headerHeight = (int) (f * h);
                    } else {
                        headerHeight = mTopicHeaderView.getHeight();
                    }
                }
                int offsetHeight = headerHeight - Tools.getDimen(TopicInfoActivity.this,R.dimen.dp_49) - Tools.getStatusBarHeight(TopicInfoActivity.this);
                Log.i("tzy", "onScrolled: offsetHeight = " + offsetHeight);
                //title渐变
                float alpha = offsetHeight > 0 ? (mDistance <= offsetHeight ? (float) mDistance / offsetHeight : 1) : 0;
                Log.i("tzy", "onScrolled: alpha = " + alpha);
                titleBg.setAlpha(alpha);
                mTabLayout.setVisibility(alpha == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                ismHiddenActionstart = false;

                if (mDistance < (headerHeight + mTabLayout.getHeight() + itemH - screenH)) {
                    return;
                }

                if (newState == 1) {
                    if (mFloatingButton.getVisibility() != View.VISIBLE)
                        return;
                    if (ismHiddenActionstart)
                        return;
                    TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                            0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
                    mHiddenAction.setDuration(250);
                    mFloatingButton.clearAnimation();
                    mFloatingButton.setAnimation(mHiddenAction);
                    mHiddenAction.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            ismHiddenActionstart = true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mFloatingButton.setVisibility(View.GONE);
                            ismHiddenActionstart = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else if (newState == 0) {
                    if (mFloatingButton.getVisibility() == View.VISIBLE)
                        return;
                    mFloatingButton.setVisibility(View.VISIBLE);
                    TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                            1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                    mShowAction.setDuration(250);
                    mFloatingButton.clearAnimation();
                    mFloatingButton.setAnimation(mShowAction);
                }


            }
        });

        mStaggeredGridView.setOnItemClickListener(new OnItemClickListenerRvStat() {
            @Override
            public void onItemClicked(View view, RecyclerView.ViewHolder holder, int position) {
                TopicItemModel topicItemModel = mTopicInfoStaggeredAdapter.getData().get(position);
                int itemType = topicItemModel.getItemType();
                if (itemType == TopicInfoStaggeredAdapter.ITEM_TAB) {
                    setTabClick(topicItemModel);
                } else if (itemType == TopicInfoStaggeredAdapter.ITEM_TOPIC_VID) {
                    TopicItemModel model = mDatas.get(position);
                    if (model != null) {
                        AppCommon.openUrl(model.getGotoUrl(), true);
                        XHClick.mapStat(view.getContext(), ShortVideoDetailActivity.STA_ID, "用户内容", "内容详情点击量");
                    }
                }
            }

            @Override
            protected void onStat(int position, String statJsonStr) {
                //统计网格列表中显示的位置
                super.onStat(position - (tabPosition + 1), statJsonStr);
            }

            @Override
            protected String getStatData(int position) {//实际对应的数据
                return mTopicInfoStaggeredAdapter.getData().get(position).getStatJson();
            }
        });
    }

    public void setTabClick(TopicItemModel topicItemModel) {
        onTabClick(topicItemModel.getTabTag());
    }

    public void onTabClick(int tabTag) {
        switch (tabTag) {
            case TopicItemModel.TAB_HOT:
                mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
                mHotTabBottomView.setVisibility(View.VISIBLE);
                mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
                mNewTabBottomView.setVisibility(View.INVISIBLE);
                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最热"));
                mTab = HOT;
                if (mHotDatas.isEmpty()) {
                    loadTopicList(true);
                } else {
                    onTabChanged(mTab);
                }
                break;
            case TopicItemModel.TAB_NEW:
                mHotTabTv.setTextColor(XHApplication.in().getResources().getColor(R.color.c_777777));
                mHotTabBottomView.setVisibility(View.INVISIBLE);
                mNewTabTV.setTextColor(XHApplication.in().getResources().getColor(R.color.white));
                mNewTabBottomView.setVisibility(View.VISIBLE);
                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最新"));
                mTab = NEW;
                if (mNewDatas.isEmpty()) {
                    loadTopicList(true);
                } else {
                    onTabChanged(mTab);
                }
                break;
        }
    }

    private int dp2px(int dimenId) {
        return getResources().getDimensionPixelSize(dimenId);
    }

    private boolean checkCondition() {
        boolean ret = false;
        if (!TextUtils.isEmpty(mTopicCode))
            ret = true;
        return ret;
    }

    private void startLoadData() {
        TopicItemModel model = new TopicItemModel();
        model.setItemType(TopicInfoStaggeredAdapter.ITEM_TAB);
        mDatas.add(model);
        mTopicInfoStaggeredAdapter.notifyDataSetChanged();
        loadManager.setLoading(mStaggeredGridView, mTopicInfoStaggeredAdapter, true, v -> loadTopicList(false));
    }

    boolean infoIsOver = false;

    private void loadTopicInfo() {

        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_INFOV1, "code=" + mTopicCode, new InternetCallback() {

            @Override
            public void loaded(int i, String s, Object o) {
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mInfoMap = StringManager.getFirstMap(o);
                    title = mInfoMap.get("name");
                    if (!TextUtils.isEmpty(title)) {
                        mTitle.setText(title);
                    } else {
                        mTitleLayout.setVisibility(View.GONE);
                    }
                    activityType = mInfoMap.get("activityType");
                    if (activityType != null) {
                        switch (activityType) {
                            case "0":
                            case "1":
                                mTopicHeaderView = new TopicHeaderView(TopicInfoActivity.this);
                                mStaggeredGridView.addHeaderView(mTopicHeaderView);
                                mTopicHeaderView.showTopicData(activityType, mTopicCode, mInfoMap);
                                mTopicHeaderView.setVisibility(View.VISIBLE);
                                mTopicInfoStaggeredAdapter.setTabIndex(tabPosition);
                                break;
                            case "2":
                                Map<String, String> activityInfoMap = StringManager.getFirstMap(mInfoMap.get("activityInfo"));
                                if (!activityInfoMap.isEmpty()) {
                                    String url = activityInfoMap.get("url");
                                    if (TextUtils.isEmpty(url)) {
                                        return;
                                    }
                                    int imageWidth = Tools.parseIntOfThrow(activityInfoMap.get("imageWidth"), 100);
                                    imageHeight = Tools.parseIntOfThrow(activityInfoMap.get("imageHeight"), 100);
                                    final int size = imageHeight / 400 + (imageHeight % 400 > 0 ? 1 : 0);
                                    tabPosition = size;
                                    mTopicInfoStaggeredAdapter.setTabIndex(tabPosition);
                                    final int realImageWidth = ToolsDevice.getWindowPx(TopicInfoActivity.this).widthPixels;
                                    final int realImageHieght = (int) (realImageWidth / imageWidth * 400f);
                                    for (int index = 0; index < size; index++) {
                                        TopicItemModel model = new TopicItemModel();
                                        model.setBitmap(null);
                                        model.setImageWidth(realImageWidth);
                                        model.setImageHieght(realImageHieght);
                                        model.setItemType(TopicInfoStaggeredAdapter.ITEM_ACTIVITY_IMG);
                                        mDatas.add(index, model);
                                    }
                                    if (mTopicInfoStaggeredAdapter != null) {
                                        mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                                    }
                                    ImgManager.tailorImageByUrl(TopicInfoActivity.this, url, imageWidth, imageHeight, 400, new ImgManager.OnResourceCallback() {
                                        @Override
                                        public void onResource(ArrayList<Bitmap> bitmaps) {
                                            TopicInfoActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (bitmaps != null && mDatas != null) {
                                                        for (int i = size - 1; i >= 0; i--) {
                                                            mDatas.remove(i);
                                                        }
                                                        tabPosition = bitmaps.size();
                                                        mTopicInfoStaggeredAdapter.setTabIndex(tabPosition);
                                                        for (int i = 0; i < bitmaps.size(); i++) {
                                                            Bitmap bitmap = bitmaps.get(i);
                                                            TopicItemModel model = new TopicItemModel();
                                                            model.setBitmap(bitmap);
                                                            model.setItemType(TopicInfoStaggeredAdapter.ITEM_ACTIVITY_IMG);
                                                            mDatas.add(i, model);
                                                        }
                                                        if (mTopicInfoStaggeredAdapter != null) {
                                                            mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                                break;
                        }
                    }
                    infoIsOver = true;
                    loadTopicList(false);
                }
                if (mTopicInfoStaggeredAdapter != null) {
                    mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void loadTopicList(final boolean isChangeTab) {
        if (!infoIsOver) {
            return;
        }
//        mTopicListLoading = true;
        switch (mTab) {
            case HOT:
                ++mHotPage;
                mPage = mHotPage;
                break;
            case NEW:
                ++mNewPage;
                mPage = mNewPage;
                break;
        }

        if (mHotDatas.size() > 0) {
            loadManager.loading(mStaggeredGridView, false);
        } else {
            loadManager.loading(mStaggeredGridView, true);
        }
        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_LIST, "code=" + mTopicCode + "&page=" + mPage + "&tab=" + mTab, new InternetCallback() {
            final String tab = mTab;
            final int currentPage = mPage;
            final boolean isCurrentChangeTab = isChangeTab;

            @Override
            public void loaded(int i, String s, Object o) {
//                mTopicListLoading = false;
                int currentPageCount = -1;
                loadManager.loaded(mStaggeredGridView);
                if (i >= ReqInternet.REQ_OK_STRING) {
                    List<Map<String, String>> datas = StringManager.getListMapByJson(o);
                    currentPageCount = datas.size();
                    ArrayList<TopicItemModel> tmepData = new ArrayList<>();
                    for (int j = 0; j < datas.size(); j++) {
                        Map<String, String> data = datas.get(j);
                        //只处理了当前用到的数据，其他数据未处理，如有需要再添加设置
                        TopicItemModel topicItemModel = new TopicItemModel();
                        topicItemModel.setVideoCode(data.get("code"));
                        topicItemModel.setGotoUrl(data.get("url"));
                        Map<String, String> videoMap = StringManager.getFirstMap(data.get("video"));
                        VideoModel videoModel = new VideoModel();
                        videoModel.setVideoImg(videoMap.get("videoImg"));
                        videoModel.setVideoGif(videoMap.get("videoGif"));
                        videoModel.setVideoUrlMap(StringManager.getFirstMap(videoMap.get("videoUrl")));
                        topicItemModel.setVideoModel(videoModel);
                        Map<String, String> imgMap = StringManager.getFirstMap(data.get("image"));
                        ImageModel imageModel = new ImageModel();
                        imageModel.setImageW(imgMap.get("width"));
                        imageModel.setImageH(imgMap.get("height"));
                        imageModel.setImageUrl(imgMap.get("url"));
                        topicItemModel.setImageModel(imageModel);
                        Map<String, String> labelMap = StringManager.getFirstMap(data.get("label"));
                        LabelModel labelModel = new LabelModel();
                        labelModel.setTitle(labelMap.get("title"));
                        labelModel.setColor(labelMap.get("color"));
                        labelModel.setBgColor(labelMap.get("bgColor"));
                        topicItemModel.setLabelModel(labelModel);
                        topicItemModel.setItemType(TopicInfoStaggeredAdapter.ITEM_TOPIC_VID);
                        topicItemModel.setStatJson(data.get("statJson"));
                        switch (tab) {
                            case HOT:
                                topicItemModel.setIsHot(true);
                                topicItemModel.setHotNo(currentPage == 1 && j < 3 ? j + 1 : 0);
                                break;
                            case NEW:
                                topicItemModel.setIsHot(false);
                                break;
                        }
                        tmepData.add(topicItemModel);
                    }
                    updateData(isCurrentChangeTab,tab, tmepData);
                } else {
                    switch (tab) {
                        case HOT:
                            --mHotPage;
                            break;
                        case NEW:
                            --mNewPage;
                            break;
                    }
                }
                if (loadManager != null) {
                    loadManager.loadOver(i, mStaggeredGridView, currentPageCount);
                }
            }
        });
    }

    private void updateData(boolean isCurrentChangeTab,String tab, @NonNull ArrayList<TopicItemModel> tmepData) {
        if (TextUtils.isEmpty(tab))
            return;
        if (mDatas != null) {
            switch (tab) {
                case HOT:
                    if (mNewDatas != null && isCurrentChangeTab) {
                        mDatas.removeAll(mNewDatas);
                    }
                    mHotDatas.addAll(tmepData);
                    break;
                case NEW:
                    if (mHotDatas != null && isCurrentChangeTab) {
                        mDatas.removeAll(mHotDatas);
                    }
                    mNewDatas.addAll(tmepData);
                    break;
            }
            if (mTopicInfoStaggeredAdapter != null && TextUtils.equals(mTab, tab)) {
                mDatas.addAll(tmepData);
                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
            }
        }
    }

    private void onTabChanged(String currentTab) {
        if (TextUtils.isEmpty(currentTab))
            return;
        if (mDatas != null) {
            switch (currentTab) {
                case HOT:
                    if (mNewDatas != null) {
                        mDatas.removeAll(mNewDatas);
                    }
                    mDatas.addAll(mHotDatas);
                    if(mHotDatas.isEmpty()){
                        mHotTabBottomView.setVisibility(View.GONE);
                        mFloatingButton.setVisibility(View.GONE);
                    }
                    break;
                case NEW:
                    if (mHotDatas != null) {
                        mDatas.removeAll(mHotDatas);
                    }
                    mDatas.addAll(mNewDatas);
                    if(mNewDatas.isEmpty()){
                        mHotTabBottomView.setVisibility(View.GONE);
                        mFloatingButton.setVisibility(View.GONE);
                    }
                    break;
            }
            if (mTopicInfoStaggeredAdapter != null && TextUtils.equals(mTab, currentTab)) {
                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
            }
        }

        if (mTopicHeaderView != null) {
            mStaggeredGridView.smoothScrollToPosition(1);
        } else {
            mStaggeredGridView.smoothScrollToPosition(tabPosition);
        }
    }
}
