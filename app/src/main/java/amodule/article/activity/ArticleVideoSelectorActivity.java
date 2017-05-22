package amodule.article.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

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
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.tools.FileToolsCammer;

import static aplug.recordervideo.db.RecorderVideoSqlite.getInstans;

public class ArticleVideoSelectorActivity extends BaseActivity implements View.OnClickListener{

    private Button mCancelBtn;
    private ImageView mBackImg;
    private TextView mCategoryText;
    private TextView mTitle;

    private GridView mGridView;
    private RelativeLayout mVideoEmptyView;
    private RelativeLayout mGridViewLayout;
    private ArticleVideoSelectorAdapter mAdapter;

    private PopupWindow mCategoryPopup;
    private ListView mFolderListView;
    private ArticleVideoFolderAdapter mCategoryAdapter;

    private RelativeLayout mVideoContainer;
    private VideoView mVideoView;

    private ArrayList<Map<String, String>> mAllDatas;
    /**String:VideoParentPath, List<Map<String, String>>:VideoParentPath下的视频列表*/
    private Map<String, List<Map<String, String>>> mVideoParentFiles = new HashMap<String, List<Map<String, String>>>();

    private boolean mIsPreview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止页面黑一下
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("",2,0,0,R.layout.articlevideo_seletor_activity);

        mCategoryAdapter = new ArticleVideoFolderAdapter(this);
        initView();
        addListener();
        contentLoad(false, false);
    }

    private void addListener() {
        mVideoContainer.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        mBackImg.setOnClickListener(this);
        mCategoryText.setOnClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
                //根据滑动状态设置图片的加载状态
                if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
                    Glide.with(ArticleVideoSelectorActivity.this).resumeRequests();
                } else {
                    Glide.with(ArticleVideoSelectorActivity.this).pauseRequests();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO Item的点击事件
                Map<String, String> video = mAdapter.getItem(position);
                if (mIsPreview) {
                    showVideo(video);
                } else {
                    ArticleVideoSelectorActivity.this.setResult(RESULT_OK, getSingleResult(video));
                    ArticleVideoSelectorActivity.this.finish();
                }
            }
        });
    }

    private void initView() {
        initTitle();
        mCategoryText = (TextView) findViewById(R.id.category_btn);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        mTitle = (TextView) findViewById(R.id.title);
        mBackImg = (ImageView) findViewById(R.id.btn_back);

        mGridView = (GridView) findViewById(R.id.grid);
        mGridViewLayout = (RelativeLayout) findViewById(R.id.grid_layout);
        mVideoEmptyView = (RelativeLayout) findViewById(R.id.video_emptyview);

        mVideoContainer = (RelativeLayout) findViewById(R.id.video_container);
        mVideoView = (VideoView) findViewById(R.id.article_pre_videoview);
    }

    private void initTitle() {
        if(Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if(Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);
            RelativeLayout bar_title = (RelativeLayout)findViewById(R.id.title_rela_all);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }

    /**
     *
     * @param isDelete
     * @param isReload
     */
    private void contentLoad(final boolean isDelete,boolean isReload){
        loadManager.showProgressBar();
        if(!isReload && getInstans().getDataSize() == 0) isReload = true;
        FileToolsCammer.loadCammerAllData(new FileToolsCammer.OnCammerFileListener() {
            @Override
            public void loadOver(final ArrayList<Map<String, String>> orderArrayLis) {
                if (orderArrayLis != null && orderArrayLis.size() > 0) {
                    for (Map<String, String> map : orderArrayLis) {
                        if (map != null) {
                            String videoPath = map.get(RecorderVideoData.video_path);
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

                ArticleVideoSelectorActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDataReady(orderArrayLis);
                    }
                });
            }
        },isDelete,isReload);
    }

    /**
     * 数据加载完成
     * @param datas
     */
    private void onDataReady(ArrayList<Map<String, String>> datas) {
        loadManager.hideProgressBar();
        if (datas == null || datas.size() <= 0) {
            mGridViewLayout.setVisibility(View.GONE);
            mVideoEmptyView.setVisibility(View.VISIBLE);
            Tools.showToast(this, "本地没有视频哦");
            return;
        }
        mAllDatas = datas;
        mGridViewLayout.setVisibility(View.VISIBLE);
        if (mAdapter == null)
            mAdapter = new ArticleVideoSelectorAdapter();
        mAdapter.setData(datas);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_container:
                hideVideo();
                break;
            case R.id.article_pre_videoview:
                Map<String, String> videoData = (Map<String, String>) v.getTag();
                setResult(RESULT_OK, getSingleResult(videoData));
                finish();
                break;
            case R.id.btn_cancel:
                if (mCategoryPopup != null && mCategoryPopup.isShowing()) {
                    mCategoryText.setVisibility(View.VISIBLE);
                    mTitle.setText("香哈视频");

                    if (mAllDatas != null && mAllDatas.size() > 0) {
                        mAdapter.setData(mAllDatas);
                    } else {
                        mGridViewLayout.setVisibility(View.GONE);
                        mVideoEmptyView.setVisibility(View.VISIBLE);
                    }
                    mCategoryPopup.dismiss();
                    return;
                }
                //TODO 取消、清除数据
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btn_back:
                mIsPreview = false;
                mBackImg.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.VISIBLE);
                mTitle.setText("相册");
                showCategoryPopup();
                break;
            case R.id.category_btn:
                mTitle.setText("相册");
                mCategoryText.setVisibility(View.GONE);
                if (mVideoParentFiles.size() <= 0) {
                    Tools.showToast(this, "本地没有视频哦");
                    return;
                }
                if (mCategoryPopup == null)
                    createPopupFolderList();
                showCategoryPopup();
                break;
        }
    }

    private void showCategoryPopup() {
        if (mCategoryPopup == null)
            return;
        if(mCategoryPopup.isShowing())
            mCategoryPopup.dismiss();
        else
            mCategoryPopup.showAtLocation(mGridViewLayout, Gravity.BOTTOM, 0, 0);
    }

    /** 初始化相册列表的PopuWindow */
    private void createPopupFolderList(){
        mCategoryPopup = new PopupWindow(this);
        View mView = LayoutInflater.from(this).inflate(R.layout.user_country_list, null);
        mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFolderListView = (ListView) mView.findViewById(R.id.country_list);
        mCategoryAdapter.setData(mVideoParentFiles);
        mFolderListView.setAdapter(mCategoryAdapter);
        mFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCategoryPopup.dismiss();
                mIsPreview = true;
                String parentPath = mCategoryAdapter.getParentPathByPos(position);
                mTitle.setText(TextUtils.isEmpty(parentPath) ? "香哈视频" : parentPath);
                mCategoryText.setVisibility(View.GONE);
                mCancelBtn.setVisibility(View.GONE);
                mBackImg.setVisibility(View.VISIBLE);
                mAdapter.setData((ArrayList<Map<String, String>>) mCategoryAdapter.getItem(position));
                mGridView.smoothScrollToPosition(0);
            }
        });
        mCategoryPopup.setContentView(mView);
        mCategoryPopup.setWidth(mGridViewLayout.getWidth());
        mCategoryPopup.setHeight(mGridViewLayout.getHeight());
        mCategoryPopup.setAnimationStyle(R.style.PopupAnimation);
    }

    private Intent getSingleResult(Map<String, String> videoData) {
        Intent intent = new Intent();
        if (videoData != null && videoData.size() > 0) {
            intent.putExtra(RecorderVideoData.video_path, videoData.get(RecorderVideoData.video_path));
        }
        return intent;
    }

    private void showVideo(Map<String, String> videoData) {
        if (videoData == null || videoData.size() < 0 || mVideoView == null || mVideoContainer == null)
            return;
        String videoPath = videoData.get(RecorderVideoData.video_path);
        if (TextUtils.isEmpty(videoPath))
            return;
        mVideoView.setTag(videoData);
        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
        mVideoContainer.setVisibility(View.VISIBLE);
    }

    private void hideVideo() {
        if (mVideoView == null || mVideoContainer == null)
            return;
        mVideoView.pause();
        mVideoContainer.setVisibility(View.GONE);
    }
}
