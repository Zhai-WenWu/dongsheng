package amodule.topic.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.RvStaggeredGridView;
import amodule._common.conf.GlobalAttentionModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.topic.adapter.TopicInfoStaggeredAdapter;
import amodule.topic.model.ImageModel;
import amodule.topic.model.LabelModel;
import amodule.topic.model.TopicItemModel;
import amodule.topic.model.VideoModel;
import amodule.topic.view.TopicHeaderView;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

public class TopicInfoActivity extends BaseAppCompatActivity {
    public static final String STA_ID = "a_topic_gather";

    public static final String TOPIC_CODE = "topicCode";

    private TextView mTitle;
    private ImageView mBackImg;
    private PtrClassicFrameLayout mRefreshLayout;
    private RvStaggeredGridView mStaggeredGridView;
    private FloatingActionButton mFloatingActionButton;
    private TopicHeaderView mTopicHeaderView;

    private String mTopicCode;

    private int mPage;
    private Map<String, String> mInfoMap;
    private Map<String, String> mAuthorMap;
    private boolean mTopicInfoLoading;
    private boolean mTopicInfoLoadStarted;
    private boolean mTopicListLoading;

    private TopicInfoStaggeredAdapter mTopicInfoStaggeredAdapter;
    private ArrayList<TopicItemModel> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.topic_info_layout);
        initView();
        initData();
        if (!checkCondition()) {
            Toast.makeText(this, "参数不正确", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        startLoadData();
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            mTopicCode = i.getStringExtra(TOPIC_CODE);
        }
        mDatas = new ArrayList<>();
        mTopicInfoStaggeredAdapter = new TopicInfoStaggeredAdapter(this, mDatas);
    }

    private void initView() {
        mTitle = findViewById(R.id.title);
        mBackImg = findViewById(R.id.back_img);
        mBackImg.setOnClickListener(v -> {
            TopicInfoActivity.this.finish();
        });
        mRefreshLayout = findViewById(R.id.refresh_list_view_frame);
        mRefreshLayout.disableWhenHorizontalMove(true);
        mRefreshLayout.setLoadingMinTime(300);
        mStaggeredGridView = findViewById(R.id.staggered_view);
        mStaggeredGridView.closeDefaultAnimator();
        mFloatingActionButton = findViewById(R.id.fab);


        mTopicHeaderView = new TopicHeaderView(this);
        mStaggeredGridView.addHeaderView(mTopicHeaderView);
        mStaggeredGridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                StaggeredGridLayoutManager.LayoutParams params =(StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
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
        loadManager.setLoading(mRefreshLayout, mStaggeredGridView, mTopicInfoStaggeredAdapter, true, v -> {
                    loadTopicInfo(true);
                    loadTopicList(true);
                }, v -> {
                    if (!mTopicInfoLoadStarted) {
                        loadTopicInfo(false);
                    }
                    loadTopicList(false);
                }
        );
    }

    private void loadTopicInfo(boolean refresh) {
        if (mTopicInfoLoading) {
            return;
        }
        mTopicInfoLoading = true;
        mTopicInfoLoadStarted = true;
        ReqEncyptInternet.in().doEncypt(StringManager.API_TOPIC_INFO, "code=" + mTopicCode, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                mTopicInfoLoading = false;
                if (!mTopicListLoading) {
                    if (loadManager != null) {
                        loadManager.hideProgressBar();
                    }
                    if (mRefreshLayout != null && refresh) {
                        mRefreshLayout.refreshComplete();
                    }
                }
                if (i >= ReqInternet.REQ_OK_STRING) {
                    mInfoMap = StringManager.getFirstMap(o);
                    String name = mInfoMap.get("name");
                    if (!TextUtils.isEmpty(name)) {
                        mTitle.setText(name);
                    }
                    mAuthorMap = StringManager.getFirstMap(mInfoMap.get("author"));
                    mTopicHeaderView.showUserImage(mAuthorMap.get("img"), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotoUser();
                            XHClick.mapStat(TopicInfoActivity.this, ShortVideoDetailActivity.STA_ID, "用户内容", "头像");

                        }
                    });
                    mTopicHeaderView.showTopicUser(mAuthorMap.get("nickName"), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotoUser();
                            XHClick.mapStat(TopicInfoActivity.this, ShortVideoDetailActivity.STA_ID, "用户内容", "昵称");
                        }
                    });
                    mTopicHeaderView.showTopicAttention(TextUtils.equals(mAuthorMap.get("isFollow"), "2"), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            XHClick.mapStat(TopicInfoActivity.this, ShortVideoDetailActivity.STA_ID, "用户内容", "关注");
                            if (!LoginManager.isLogin()) {
                                TopicInfoActivity.this.startActivity(new Intent(TopicInfoActivity.this, LoginByAccout.class));
                                return;
                            }
                            AppCommon.onAttentionClick(mAuthorMap.get("code"), "follow", new Runnable() {
                                @Override
                                public void run() {
                                    if (mTopicHeaderView != null)
                                        mTopicHeaderView.setAttentionEnable(false);
                                    GlobalAttentionModule module = new GlobalAttentionModule();
                                    module.setAttentionUserCode(mAuthorMap.get("code"));
                                    module.setAttention(true);
                                    GlobalVariableConfig.handleAttentionModule(module);
                                }
                            });
                        }
                    });
                    mTopicHeaderView.showTopicInfo(mInfoMap.get("content"));
                    mTopicHeaderView.showTopicNum(mInfoMap.get("num"));
                } else {
                    mInfoMap = null;
                    mTopicHeaderView.setVisibility(View.GONE);
                }
                if (mTopicInfoStaggeredAdapter != null) {
                    mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void loadTopicList(boolean refresh) {
        if (mTopicListLoading) {
            return;
        }
        mTopicListLoading = true;
        if (refresh) {
            mPage = 0;
        }
        ++mPage;
        loadManager.changeMoreBtn(mStaggeredGridView, ReqInternet.REQ_OK_STRING, -1, -1, 2, false);
        ReqEncyptInternet.in().doEncypt(StringManager.API_TOPIC_LIST, "code=" + mTopicCode + "&page=" + mPage, new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object o) {
                mTopicListLoading = false;
                int currentPageCount = -1;
                if (i >= ReqInternet.REQ_OK_STRING) {
                    if (refresh) {
                        mDatas.clear();
                        if (mTopicInfoStaggeredAdapter != null) {
                            mTopicInfoStaggeredAdapter.notifyDataSetChanged();
                        }
                    }
                    List<Map<String, String>> datas = StringManager.getListMapByJson(o);
                    currentPageCount = datas.size();
                    for (Map<String, String> data : datas){
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
                        mDatas.add(topicItemModel);
                    }
                    if (mTopicInfoStaggeredAdapter != null) {
                        mTopicInfoStaggeredAdapter.notifyItemRangeChanged(0, mDatas.size());
                    }
                } else {
                    --mPage;
                }
                if (loadManager != null) {
                    loadManager.changeMoreBtn(mStaggeredGridView, i, LoadManager.FOOTTIME_PAGE, refresh ? mDatas.size() : currentPageCount, 0, refresh);
                }
                if (!mTopicInfoLoading) {
                    loadManager.hideProgressBar();
                    if (mRefreshLayout != null && refresh) {
                        mRefreshLayout.refreshComplete();
                    }
                }
            }
        });
    }

    private void gotoUser() {
        if (mAuthorMap == null)
            return;
        String gotoUrl = mAuthorMap.get("url");
        if (!TextUtils.isEmpty(gotoUrl)) {
            AppCommon.openUrl(gotoUrl, true);
        }
    }
}
