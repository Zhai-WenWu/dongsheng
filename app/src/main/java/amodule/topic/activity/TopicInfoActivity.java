package amodule.topic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import org.eclipse.jetty.util.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.ImgManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvGridView;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.RvStaggeredGridView;
import amodule.topic.adapter.TopicInfoStaggeredAdapter;
import amodule.topic.model.ImageModel;
import amodule.topic.model.LabelModel;
import amodule.topic.model.TopicItemModel;
import amodule.topic.model.TopicModel;
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
    private Map<String, String> mAuthorMap;
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
    private int headerHeight = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.topic_info_layout);
        initStatusBar();
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

    private void initStatusBar() {
        String colors = Tools.getColorStr(this, R.color.ysf_black_333333);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            mTopicCode = i.getStringExtra(TOPIC_CODE);
        }

        mHotDatas = new ArrayList<>();
        mNewDatas = new ArrayList<>();
        mDatas = new ArrayList<>();
        TopicItemModel model = new TopicItemModel();
        model.setItemType(TopicInfoStaggeredAdapter.ITEM_TAB);
        mDatas.add(model);
        mTopicInfoStaggeredAdapter = new TopicInfoStaggeredAdapter(TopicInfoActivity.this, mDatas);
        mStaggeredGridView.setAdapter(mTopicInfoStaggeredAdapter);
        mTopicInfoStaggeredAdapter.notifyDataSetChanged();
        tabPosition = 0;
        loadTopicList();
    }

    private void initView() {
        mTitle = findViewById(R.id.title);
        View mTabLayout = findViewById(R.id.tab_layout);
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

//        mHotView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mHotTabTv.setTextColor(getResources().getColor(R.color.white));
//                mHotTabBottomView.setVisibility(View.VISIBLE);
//                mNewTabTV.setTextColor(getResources().getColor(R.color.c_777777));
//                mNewTabBottomView.setVisibility(View.INVISIBLE);
//
//                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最热"));
//
//                mTab = HOT;
//
//                mStaggeredGridView.scrollToPosition(0);
//                mTopicInfoStaggeredAdapter.setData(mHotDatas);
//                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
//            }
//        });
//
//        mNewView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mHotTabTv.setTextColor(getResources().getColor(R.color.c_777777));
//                mHotTabBottomView.setVisibility(View.INVISIBLE);
//                mNewTabTV.setTextColor(getResources().getColor(R.color.white));
//                mNewTabBottomView.setVisibility(View.VISIBLE);
//
//                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最新"));
//
//                mTab = NEW;
//
//                if (mNewDatas.size() == 0) {
//                    loadTopicList();
//                }
//
//                mStaggeredGridView.scrollToPosition(0);
//                mTopicInfoStaggeredAdapter.setData(mNewDatas);
//                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
//            }
//        });

        mStaggeredGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                int position = parent.getChildAdapterPosition(view);
                int viewType = parent.getAdapter().getItemViewType(position);
                switch (viewType) {
                    case RvListView.VIEW_TYPE_EMPTY:
                    case RvListView.VIEW_TYPE_FOOTER:
                    case RvListView.VIEW_TYPE_HEADER:
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
                        outRect.set(0, 0, 0, 0);
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

        mStaggeredGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View tabItemView = mTopicInfoStaggeredAdapter.getTabItemView();
                if (tabItemView != null) {
                    tabItemView.getLocationOnScreen(locationDong);
                }
                mTabLayout.getLocationOnScreen(locationJing);

                if (locationDong[1] <= locationJing[1]) {
                    mTabLayout.setVisibility(View.VISIBLE);
                } else {
                    mTabLayout.setVisibility(View.GONE);
                }

                if (activityType != null) {
                    if (activityType.equals("2")) {
                        headerHeight = imageHeight;
                    } else {
                        headerHeight = mTopicHeaderView.getHeight();
                    }
                }

                mDistance += dy;
                float alpha = (float) mDistance / headerHeight;
                if (alpha > 0 && alpha < 1) {
                    titleBg.setAlpha((float) alpha);
                }
                if (alpha == 0) {
                    titleBg.setAlpha(0);
                }
                if (alpha >= 1) {
                    titleBg.setAlpha(1);
                }
            }
        });

        mStaggeredGridView.setOnItemClickListener(new RvListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                TopicItemModel topicItemModel = mTopicInfoStaggeredAdapter.getData().get(position);
                int itemType = topicItemModel.getItemType();
                if (itemType == TopicInfoStaggeredAdapter.ITEM_TAB) {
                    setTabClick(topicItemModel);
                } else if (itemType == TopicInfoStaggeredAdapter.ITEM_TOPIC_VID) {

                }
            }
        });
    }

    public void setTabClick(TopicItemModel topicItemModel) {
        if (topicItemModel.getTabTag() == topicItemModel.TAB_HOT) {
            mTopicInfoStaggeredAdapter.notifyItemRangeChanged(2, mHotDatas.size());
        } else if (topicItemModel.getTabTag() == topicItemModel.TAB_HOT) {
            if (mNewDatas.size() == 0) {
                loadTopicList();
            } else {
                mTopicInfoStaggeredAdapter.notifyItemRangeChanged(2, mNewDatas.size());
            }
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
        loadManager.setLoading(mStaggeredGridView, mTopicInfoStaggeredAdapter, true, v -> {
                    loadTopicList();
                }
        );
    }

    private void loadTopicInfo() {

        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_INFOV1, "code=" + mTopicCode, new InternetCallback() {

            @Override
            public void loaded(int i, String s, Object o) {
//                if (loadManager != null) {
//                    loadManager.hideProgressBar();
//                }
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
                                    ImgManager.tailorImageByUrl(TopicInfoActivity.this, url, imageWidth, imageHeight, 400, new ImgManager.OnResourceCallback() {
                                        @Override
                                        public void onResource(ArrayList<Bitmap> bitmaps) {
                                            TopicInfoActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (bitmaps != null && mDatas != null) {
                                                        for (int i = 0; i < bitmaps.size(); i ++) {
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
                    mAuthorMap = StringManager.getFirstMap(mInfoMap.get("author"));
                }
                setJoinBtnVisible();
                if (mTopicInfoStaggeredAdapter != null) {
                    mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setJoinBtnVisible() {
        //控制参与按钮的显示隐藏
        final int[] location1 = new int[2];
//        int screenW = ToolsDevice.getWindowPx(this).widthPixels;
//        int screenH = ToolsDevice.getWindowPx(this).heightPixels;
//        int itemW = screenW / 3;
//        int itemH = itemW * 165 / 124;
//        int H = screenH - itemH;


        int originalW = 124;
        int originalH = 165;
        int screenW = ToolsDevice.getWindowPx(this).widthPixels;
        int screenH = ToolsDevice.getWindowPx(this).heightPixels;
        int newW = (screenW - this.getResources().getDimensionPixelSize(R.dimen.dp_2)) / 3;
        int newH = newW * originalH / originalW;
        int H = screenH - newH;


//
//        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                mStaggeredGridView.getLocationOnScreen(location1);
//                if (location1[1] <= H) {
//                    mFloatingButton.setVisibility(View.VISIBLE);
//                } else {
//                    mFloatingButton.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

    }

    private void loadTopicList() {
//        if (mTopicListLoading) {
//            return;
//        }
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

            @Override
            public void loaded(int i, String s, Object o) {
//                mTopicListLoading = false;
                int currentPageCount = -1;
                loadManager.loaded(mStaggeredGridView);
                if (i >= ReqInternet.REQ_OK_STRING) {
                    List<Map<String, String>> datas = StringManager.getListMapByJson(o);
                    currentPageCount = datas.size();
                    for (Map<String, String> data : datas) {
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
                                mHotDatas.add(topicItemModel);
                                break;
                            case NEW:
                                topicItemModel.setIsHot(false);
                                mNewDatas.add(topicItemModel);
                                break;
                        }
                    }
                    if (mDatas != null) {
                        switch (tab) {
                            case HOT:
                                if (mNewDatas != null) {
                                    mDatas.removeAll(mNewDatas);
                                }
                                mDatas.addAll(mHotDatas);
                                break;
                            case NEW:
                                if (mHotDatas != null) {
                                    mDatas.removeAll(mHotDatas);
                                }
                                mDatas.addAll(mNewDatas);
                                break;
                        }
                        if (mTopicInfoStaggeredAdapter != null && mTab == tab) {
                            mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                        }
                    }
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
}
