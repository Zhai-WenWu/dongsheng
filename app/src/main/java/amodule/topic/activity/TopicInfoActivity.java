package amodule.topic.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import org.eclipse.jetty.util.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
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
import third.aliyun.work.AliyunCommon;

public class TopicInfoActivity extends BaseAppCompatActivity {
    public static final String STA_ID = "a_topic_gather";

    public static final String TOPIC_CODE = "topicCode";
    public static final String ACTIVIT_TYPE = "activityType";

    private TextView mTitle;
    private ImageView mBackImg;
    private RvStaggeredGridView mStaggeredGridView;
    private ImageView mFloatingButton;
    private TopicHeaderView mTopicHeaderView;

    private String mTopicCode;
    private String mActivityType;

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
        initStatusBar();
        initView();
        initData();
        if (!checkCondition()) {
            Toast.makeText(this, "参数不正确", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        startLoadData();
        loadTopicInfo(false);
    }

    private void initStatusBar() {
        String colors = Tools.getColorStr(this, R.color.ysf_black_333333);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
    }

    private void initData() {
        Intent i = getIntent();
        if (i != null) {
            mTopicCode = i.getStringExtra(TOPIC_CODE);
            mActivityType = i.getStringExtra(ACTIVIT_TYPE);
        }

        mDatas = new ArrayList<>();
        mTopicInfoStaggeredAdapter = new TopicInfoStaggeredAdapter(this, mDatas);
        mStaggeredGridView.setAdapter(mTopicInfoStaggeredAdapter);
        loadTopicList(false);
    }

    private void initView() {
        mTitle = findViewById(R.id.title);
        mBackImg = findViewById(R.id.back_img);
        mBackImg.setOnClickListener(v -> {
//            TopicInfoActivity.this.finish();
            startActivity(new Intent(TopicInfoActivity.this,SearchTopicActivity.class));
        });
//        mRefreshLayout = findViewById(R.id.refresh_list_view_frame);
        mTopicHeaderView = findViewById(R.id.view_topic_header);
//        mRefreshLayout.disableWhenHorizontalMove(true);
//        mRefreshLayout.setLoadingMinTime(300);
        mStaggeredGridView = findViewById(R.id.staggered_view);
        mStaggeredGridView.closeDefaultAnimator();
        mFloatingButton = findViewById(R.id.floating_btn);
        FrameLayout mHotView = findViewById(R.id.fl_hot);
        FrameLayout mNewView = findViewById(R.id.fl_new);
        TextView mHotTabTv = findViewById(R.id.tv_hot_tab);
        View mHotTabBottomView = findViewById(R.id.view_hot_tab_bottom);
        TextView mNewTabTV = findViewById(R.id.tv_new_tab);
        View mNewTabBottom = findViewById(R.id.view_new_tab_bottom);

        mHotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(getResources().getColor(R.color.white));
                mHotTabBottomView.setVisibility(View.VISIBLE);
                mNewTabTV.setTextColor(getResources().getColor(R.color.c_777777));
                mNewTabBottom.setVisibility(View.INVISIBLE);
            }
        });

        mNewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHotTabTv.setTextColor(getResources().getColor(R.color.c_777777));
                mHotTabBottomView.setVisibility(View.INVISIBLE);
                mNewTabTV.setTextColor(getResources().getColor(R.color.white));
                mNewTabBottom.setVisibility(View.VISIBLE);
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
//        loadManager.setLoading( mStaggeredGridView, mTopicInfoStaggeredAdapter, true, v -> {
//                    loadTopicInfo(true);
//                    loadTopicList(true);
//                }, v -> {
//                    if (!mTopicInfoLoadStarted) {
//                        loadTopicInfo(false);
//                    }
//                }
//        );

    }

    private void loadTopicInfo(boolean refresh) {
        if (mTopicInfoLoading) {
            return;
        }
        mTopicInfoLoading = true;
        mTopicInfoLoadStarted = true;
        String json = "{\n" +
                "    \"code\": \"5050\",\n" +
                "    \"name\": \"木有活动\",\n" +
                "    \"num\": \"0\",\n" +
                "    \"content\": \"\",\n" +
                "    \"activityInfo\": [],\n" +
                "    \"users\": {\n" +
                "      \"text\": \"社交达人\",\n" +
                "      \"info\": [\n" +
                "        {\n" +
                "          \"code\": \"10191\",\n" +
                "          \"nickName\": \"古月云X\",\n" +
                "          \"url\": \"userIndex.app?code=10191&type=video\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"code\": \"20070\",\n" +
                "          \"nickName\": \"thlfdwoghakky\",\n" +
                "          \"url\": \"userIndex.app?code=20070&type=video\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"code\": \"29949\",\n" +
                "          \"nickName\": \"阿杰阿杰3阿杰3阿杰33\",\n" +
                "          \"url\": \"userIndex.app?code=29949&type=video\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"link\": {\n" +
                "      \"text\": \"点击此查看详情>>\",\n" +
                "      \"url\": \"www.xiangha.com\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"append\": [],\n" +
                "  \"power\": {},\n" +
                "  \"extra\": {\n" +
                "    \"execTime\": \"0.0451\",\n" +
                "    \"serverTime\": 1541579444,\n" +
                "    \"params\": {\n" +
                "      \"ss\": \"/Main8/shortVideo/topicInfoV1\",\n" +
                "      \"code\": \"5050\",\n" +
                "      \"debug\": \"4d5c01842f37d90651f9693783c6564279fed6f4\"\n" +
                "    }\n" +
                "  }";

        mInfoMap = StringManager.getFirstMap(json);
        String name = mInfoMap.get("name");
        if (!TextUtils.isEmpty(name)) {
            mTitle.setText(name);
        }
        mTopicHeaderView.showTopicData("0",mInfoMap);
        mTopicHeaderView.setVisibility(View.VISIBLE);

//        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_INFOV1, "code=" + mTopicCode, new InternetCallback() {
//            @Override
//            public void loaded(int i, String s, Object o) {
//                mTopicInfoLoading = false;
//                if (!mTopicListLoading) {
//                    if (loadManager != null) {
//                        loadManager.hideProgressBar();
//                    }
////                    if (mRefreshLayout != null && refresh) {
////                        mRefreshLayout.refreshComplete();
////                    }
//                }
//                if (i >= ReqInternet.REQ_OK_STRING) {
//                    mInfoMap = StringManager.getFirstMap(o);
//                    String name = mInfoMap.get("name");
//                    if (!TextUtils.isEmpty(name)) {
//                        mTitle.setText(name);
//                    }
////                    mAuthorMap = StringManager.getFirstMap(mInfoMap.get("author"));
//                    mTopicHeaderView.showTopicData(mActivityType,mInfoMap);
//                    mTopicHeaderView.setVisibility(View.VISIBLE);
//                } else {
//                    mInfoMap = null;
//                    mTopicHeaderView.setVisibility(View.GONE);
//                }
//                if (mTopicInfoStaggeredAdapter != null) {
//                    mTopicInfoStaggeredAdapter.notifyDataSetChanged();
//                }
//            }
//        });
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
//        loadManager.loading(mStaggeredGridView, false);
        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_LIST, "code=" + mTopicCode + "&page=" + mPage, new InternetCallback() {
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
                        mDatas.add(topicItemModel);
                    }
                    if (mTopicInfoStaggeredAdapter != null) {
                        mTopicInfoStaggeredAdapter.notifyItemRangeChanged(0, mDatas.size());
                    }
                } else {
                    --mPage;
                }
                if (loadManager != null) {
                    loadManager.loadOver(i, mStaggeredGridView, currentPageCount);
                }
                if (!mTopicInfoLoading) {
//                    loadManager.hideProgressBar();
//                    if (mRefreshLayout != null && refresh) {
//                        mRefreshLayout.refreshComplete();
//                    }
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
