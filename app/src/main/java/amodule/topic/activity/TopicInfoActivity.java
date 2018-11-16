package amodule.topic.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.RvStaggeredGridView;
import amodule.topic.adapter.TopicInfoStaggeredAdapter;
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
    private RvStaggeredGridView mStaggeredGridView;
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
    private LinearLayout mTitleLayout;
    private String title;

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
        mTopicInfoStaggeredAdapter = new TopicInfoStaggeredAdapter(this, mHotDatas);
        loadTopicList();
    }

    private void initView() {
        mTitle = findViewById(R.id.title);
        mBackImg = findViewById(R.id.back_img);
        mBackImg.setOnClickListener(v -> {
            finish();
        });
        mTopicHeaderView = findViewById(R.id.view_topic_header);
        mStaggeredGridView = findViewById(R.id.staggered_view);
        mStaggeredGridView.closeDefaultAnimator();
        mFloatingButton = findViewById(R.id.floating_btn);
        FrameLayout mHotView = findViewById(R.id.fl_hot);
        FrameLayout mNewView = findViewById(R.id.fl_new);
        TextView mHotTabTv = findViewById(R.id.tv_hot_tab);
        View mHotTabBottomView = findViewById(R.id.view_hot_tab_bottom);
        TextView mNewTabTV = findViewById(R.id.tv_new_tab);
        View mNewTabBottom = findViewById(R.id.view_new_tab_bottom);
        mTitleLayout = findViewById(R.id.title_container);

        mHotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(getResources().getColor(R.color.white));
                mHotTabBottomView.setVisibility(View.VISIBLE);
                mNewTabTV.setTextColor(getResources().getColor(R.color.c_777777));
                mNewTabBottom.setVisibility(View.INVISIBLE);

                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最热"));

                mTab = HOT;

                mStaggeredGridView.scrollToPosition(0);
                mTopicInfoStaggeredAdapter.setData(mHotDatas);
                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
            }
        });

        mNewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(getResources().getColor(R.color.c_777777));
                mHotTabBottomView.setVisibility(View.INVISIBLE);
                mNewTabTV.setTextColor(getResources().getColor(R.color.white));
                mNewTabBottom.setVisibility(View.VISIBLE);

                StatisticsManager.saveData(StatModel.createBtnClickDetailModel("TopicInfoActivity", "TopicInfoActivity", "new_topic_gather", title, "最新"));

                mTab = NEW;

                if (mNewDatas.size() == 0) {
                    loadTopicList();
                }

                mStaggeredGridView.scrollToPosition(0);
                mTopicInfoStaggeredAdapter.setData(mNewDatas);
                mTopicInfoStaggeredAdapter.notifyDataSetChanged();
            }
        });

        mStaggeredGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
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
                    mAuthorMap = StringManager.getFirstMap(mInfoMap.get("author"));
                    mTopicHeaderView.showTopicData(mInfoMap.get("activityType"), mTopicCode, mInfoMap);
                    mTopicHeaderView.setVisibility(View.VISIBLE);
                } else {
                    mInfoMap = null;
                    mTopicHeaderView.setVisibility(View.GONE);
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


        AppBarLayout mAppBarLayout = findViewById(R.id.app_bar);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                mStaggeredGridView.getLocationOnScreen(location1);
                if (location1[1] <= H) {
                    mFloatingButton.setVisibility(View.VISIBLE);
                } else {
                    mFloatingButton.setVisibility(View.INVISIBLE);
                }
            }
        });

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
                    if (mTopicInfoStaggeredAdapter != null && mTab == tab) {
                        switch (tab) {
                            case HOT:
                                mTopicInfoStaggeredAdapter.notifyItemRangeChanged(0, mHotDatas.size());
                                break;
                            case NEW:
                                mTopicInfoStaggeredAdapter.notifyItemRangeChanged(0, mNewDatas.size());
                                break;
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
