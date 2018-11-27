package amodule.article.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader;
import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.answer.activity.BaseEditActivity;
import amodule.article.adapter.ArticleVideoFolderAdapter;
import amodule.article.adapter.ArticleVideoSelectorAdapter;
import aplug.recordervideo.tools.FileToolsCammer;

/**
 * 选择视频列表页
 */
public class ArticleVideoSelectorActivity extends BaseActivity implements View.OnClickListener{
    public static final String EXTRA_UNSELECT_VIDEO = "extraUnselectVideo";
    private final int SELECTED_VIDEO = 1;

    private Button mCancelBtn;
    private ImageView mBackImg;
    private TextView mCategoryText;
    private TextView mTitle;

    private RecyclerView mVideoRecyclerView;
    private RecyclerViewHeader mRecyclerViewHeader;
    private RelativeLayout mVideoEmptyView;
    private RelativeLayout mGridViewLayout;
    private ArticleVideoSelectorAdapter mAdapter;

    private ListView mFolderListView;
    private ArticleVideoFolderAdapter mCategoryAdapter;
    private ArrayList<String> hadSelectedVideos = new ArrayList<>();

    private String mTjId;
    private String mTag;

    /**String:VideoParentPath, List<Map<String, String>>:VideoParentPath下的视频列表*/
    private Map<String, List<Map<String, String>>> mVideoParentFiles = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止页面黑一下
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("",2,0,0,R.layout.articlevideo_seletor_activity);

        mCategoryAdapter = new ArticleVideoFolderAdapter(this);
        initData();
        initView();
        addListener();
        contentLoad();
    }


    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.getStringArrayList(EXTRA_UNSELECT_VIDEO) != null)
                hadSelectedVideos.addAll(bundle.getStringArrayList(EXTRA_UNSELECT_VIDEO));
        }
        mTjId = getIntent().getStringExtra("tjId");
        mTag = getIntent().getStringExtra("tag");
    }

    private void addListener() {
        mCancelBtn.setOnClickListener(this);
        mBackImg.setOnClickListener(this);
        mCategoryText.setOnClickListener(this);
        mVideoRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int space = getResources().getDimensionPixelSize(R.dimen.dp_1_5);
                outRect.left = space;
                outRect.top = space;
                outRect.right = space;
                outRect.bottom = space;
            }
        });
        mVideoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (ArticleVideoSelectorActivity.this.isFinishing())
                    return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Glide.with(ArticleVideoSelectorActivity.this).resumeRequests();
                } else {
                    Glide.with(ArticleVideoSelectorActivity.this).pauseRequests();
                }
            }
        });
        mCategoryAdapter.setOnItemClickListener(new ArticleVideoFolderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                mCategoryAdapter.onItemSelected(position);
                if (mRecyclerViewHeader.getVisibility() != View.GONE) {
                    mRecyclerViewHeader.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideoRecyclerView.getLayoutParams();
                    params.topMargin = -(mRecyclerViewHeader.getHeight() - params.topMargin);
                    mVideoRecyclerView.setLayoutParams(params);
                }
                mVideoRecyclerView.smoothScrollToPosition(0);
                mAdapter.setData((ArrayList<Map<String, String>>) mCategoryAdapter.getItem(position));
                mVideoRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String parentPath = mCategoryAdapter.getParentPathByPos(position);
                        mTitle.setText(TextUtils.isEmpty(parentPath) ? "全部视频" : parentPath);
                        mCategoryText.setVisibility(View.GONE);
                        mCancelBtn.setVisibility(View.GONE);
                        mBackImg.setVisibility(View.VISIBLE);
                        closeCategoryList();
                    }
                }, 100);
            }
        });
    }

    private void initView() {
        mCategoryText = (TextView) findViewById(R.id.category_btn);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        mTitle = (TextView) findViewById(R.id.title);
        mBackImg = (ImageView) findViewById(R.id.btn_back);

        mVideoRecyclerView = (RecyclerView) findViewById(R.id.video_recyclerview);
        mVideoRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerViewHeader = (RecyclerViewHeader) findViewById(R.id.video_recyclerview_header);
        mRecyclerViewHeader.attachTo(mVideoRecyclerView, true);

        mGridViewLayout = (RelativeLayout) findViewById(R.id.grid_layout);
        mVideoEmptyView = (RelativeLayout) findViewById(R.id.video_emptyview);

        mFolderListView = (ListView) findViewById(R.id.category_list);
        mFolderListView.setAdapter(mCategoryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(this).resumeRequests();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Glide.with(this).pauseRequests();
    }

    /**
     *加载数据
     */
    private void contentLoad(){
        loadManager.showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> formats = new ArrayList<>();
                formats.add("mov");
                formats.add("mp4");
                ArrayList<Map<String, String>> videos = FileToolsCammer.getLocalMediasLimitFormats(formats);
                if (videos != null && videos.size() > 0) {
                    for (Map<String, String> map : videos) {
                        if (map != null) {
                            String videoPath = map.get(MediaStore.Video.Media.DATA);
                            File videoFile = new File(videoPath);
                            File parentFile = videoFile.getParentFile();
                            String parentName = parentFile.getName();
                            List<Map<String, String>> childs;
                            if (!mVideoParentFiles.containsKey(parentName)) {
                                childs = new ArrayList<>();
                                childs.add(map);
                                mVideoParentFiles.put(parentName, childs);
                            } else {
                                childs = mVideoParentFiles.get(parentName);
                                childs.add(map);
                            }
                        }
                    }
                }
                onDataReady(videos);
            }
        }).start();
    }

    /**
     * 数据加载完成
     * @param datas 数据
     */
    private void onDataReady(final ArrayList<Map<String, String>> datas) {
        ArticleVideoSelectorActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadManager.hideProgressBar();
                if (datas == null || datas.size() <= 0) {
                    mGridViewLayout.setVisibility(View.GONE);
                    mVideoEmptyView.setVisibility(View.VISIBLE);
                    Tools.showToast(ArticleVideoSelectorActivity.this, "本地没有视频哦");
                    return;
                }
                mGridViewLayout.setVisibility(View.VISIBLE);
                if (mAdapter == null) {
                    mAdapter = new ArticleVideoSelectorAdapter();
                    mAdapter.setOnItemClickListener(new ArticleVideoSelectorAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            Map<String, String> video = mAdapter.getData(position);
                            String duration = video.get(MediaStore.Video.Media.DURATION);
                            if (!TextUtils.isEmpty(duration)) {
                                String conditions = conditionsByTime(Long.parseLong(duration));
                                if (!TextUtils.isEmpty(conditions)) {
                                    Tools.showToast(ArticleVideoSelectorActivity.this, conditions);
                                    return;
                                }
                            }
                            Intent intent = new Intent(ArticleVideoSelectorActivity.this, VideoPreviewActivity.class);
                            intent.putExtra(MediaStore.Video.Media.DATA, video.get(MediaStore.Video.Media.DATA));
                            intent.putStringArrayListExtra(EXTRA_UNSELECT_VIDEO, hadSelectedVideos);
                            startActivityForResult(intent, SELECTED_VIDEO);
                        }
                    });
                }
                mAdapter.setData(datas);
                mVideoRecyclerView.setAdapter(mAdapter);
                mCategoryAdapter.setData(mVideoParentFiles);
            }
        });
    }

    private String conditionsByTime(long millis) {
        String ret = null;
        if (BaseEditActivity.TAG.equals(mTag)) {
            if (millis < 3*1000) {
                ret = "视频时长不能小于3秒";
                XHClick.mapStat(this, mTjId, "点击视频按钮", "选择视频小于3s");
            } else if (millis > 1000*60) {
                ret = "视频时长不能超过60s";
                XHClick.mapStat(this, mTjId, "点击视频按钮", "选择视频超过60s");
            }
        } else {
            if (millis < 1000)
                ret = "不能短于1秒";
            else if (millis > 1000 * 60 * 60)
                ret = "不能长于1小时";
        }
        return ret;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                if ("a_ask_publish".equals(mTjId)) {
                    XHClick.mapStat(this, mTjId, "点击视频按钮", "点击返回按钮");
                }
                onBackPressed();
                break;
            case R.id.btn_back:
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBackImg.setVisibility(View.GONE);
                        mCancelBtn.setVisibility(View.VISIBLE);
                        mTitle.setText("相册");
                    }
                }, 100);
                showCategoryPopup(false);
                break;
            case R.id.category_btn:
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTitle.setText("相册");
                        mCategoryText.setVisibility(View.GONE);
                    }
                }, 100);
                if (mVideoParentFiles.size() <= 0) {
                    Tools.showToast(this, "本地没有视频哦");
                    return;
                }
                showCategoryPopup(true);
                break;
        }
    }

    /**
     *显示视频相册
     * @param fromCategory
     */
    private void showCategoryPopup(boolean fromCategory) {
        if (mFolderListView == null)
            return;
        if (fromCategory)
            mCategoryAdapter.resetSelected();
        if(mFolderListView.getVisibility() == View.VISIBLE)
            closeCategoryList();
        else {
            openCategoryList();
        }
    }

    private boolean mIsOpenAnim = false;
    /**打开相册列表*/
    private void openCategoryList(){
        if (mFolderListView.getVisibility() == View.VISIBLE || mIsOpenAnim)
            return;
        excuteAnim(true);
    }

    private boolean mIsCloseAnim = false;
    /**关闭相册列表*/
    private void closeCategoryList() {
        if (mFolderListView.getVisibility() != View.VISIBLE || mIsCloseAnim)
            return;
        excuteAnim(false);
    }

    private void excuteAnim(final boolean isOpen) {
        AnimationSet animSet = new AnimationSet(true);
        animSet.setDuration(200);
        animSet.setRepeatCount(0);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isOpen) {
                    getWindow().setFormat(PixelFormat.UNKNOWN);
                    mFolderListView.setVisibility(View.VISIBLE);
                    mIsOpenAnim = true;
                } else
                    mIsCloseAnim = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isOpen)
                    mIsOpenAnim = false;
                else {
                    getWindow().setFormat(PixelFormat.TRANSLUCENT);
                    mFolderListView.setVisibility(View.GONE);
                    mIsCloseAnim = false;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation scaleAnim = new ScaleAnimation(isOpen ? 0.5f : 1.0f, isOpen ? 1.0f : 0.5f, isOpen ? 0.5f : 1.0f, isOpen ? 1.0f : 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnim = new AlphaAnimation(isOpen ? 0.0f : 1.0f, isOpen ? 1.0f : 0.0f);
        animSet.addAnimation(scaleAnim);
        animSet.addAnimation(alphaAnim);
        mFolderListView.clearAnimation();
        mFolderListView.startAnimation(animSet);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case SELECTED_VIDEO:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mFolderListView != null && mFolderListView.getVisibility() == View.VISIBLE) {
            mFolderListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTitle.setText("香哈视频");
                    mCategoryText.setVisibility(View.VISIBLE);
                }
            }, 100);
            closeCategoryList();
        }else{
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
}
