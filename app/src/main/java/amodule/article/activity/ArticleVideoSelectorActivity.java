package amodule.article.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
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

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.article.adapter.ArticleVideoFolderAdapter;
import amodule.article.adapter.ArticleVideoSelectorAdapter;
import aplug.recordervideo.tools.FileToolsCammer;


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

    private PopupWindow mCategoryPopup;
    private ListView mFolderListView;
    private ArticleVideoFolderAdapter mCategoryAdapter;
    private ArrayList<String> hadSelectedVideos = new ArrayList<>();

    /**String:VideoParentPath, List<Map<String, String>>:VideoParentPath下的视频列表*/
    private Map<String, List<Map<String, String>>> mVideoParentFiles = new HashMap<String, List<Map<String, String>>>();

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
                        mCategoryPopup.dismiss();
                    }
                }, 100);
            }
        });
    }

    private void initView() {
        initTitle();
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

    }

    private void initTitle() {
//        if(Tools.isShowTitle()) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
//        if(Tools.isShowTitle()) {
//            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
//            int height = dp_45 + Tools.getStatusBarHeight(this);
//            RelativeLayout bar_title = (RelativeLayout)findViewById(R.id.title_rela_all);
//            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
//            bar_title.setLayoutParams(layout);
//            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     *加载数据
     */
    private void contentLoad(){
        loadManager.showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> formats = new ArrayList<String>();
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
                                childs = new ArrayList<Map<String, String>>();
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
     * @param datas
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
            }
        });
    }

    private String conditionsByTime(long millis) {
        String ret = null;
        if (millis < 1*1000)
            ret = "不能短于1秒";
        else if (millis > 1*1000*60*60)
            ret = "不能长于1小时";
        return ret;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED);
                finish();
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
                if (mCategoryPopup == null)
                    createPopupFolderList();
                showCategoryPopup(true);
                break;
        }
    }

    /**
     *显示视频相册
     * @param fromCategory
     */
    private void showCategoryPopup(boolean fromCategory) {
        if (mCategoryPopup == null)
            return;
        if (fromCategory)
            mCategoryAdapter.resetSelected();
        if(mCategoryPopup.isShowing())
            mCategoryPopup.dismiss();
        else {
            getWindow().setFormat(PixelFormat.UNKNOWN);
            mCategoryPopup.showAtLocation(mGridViewLayout, Gravity.BOTTOM, 0, 0);
        }
    }

    /** 初始化相册列表的PopuWindow */
    private void createPopupFolderList(){
        mCategoryPopup = new PopupWindow(this);
        View mView = LayoutInflater.from(this).inflate(R.layout.user_country_list, null);
        mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFolderListView = (ListView) mView.findViewById(R.id.country_list);
        mCategoryAdapter.setData(mVideoParentFiles);
        mFolderListView.setAdapter(mCategoryAdapter);
        mCategoryPopup.setContentView(mView);
        mCategoryPopup.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.common_bg)));
        mCategoryPopup.setWidth(mGridViewLayout.getWidth());
        mCategoryPopup.setHeight(mGridViewLayout.getHeight());
        mCategoryPopup.setAnimationStyle(R.style.PopupAnimation);
        mCategoryPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                getWindow().setFormat(PixelFormat.TRANSLUCENT);
            }
        });
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
}
