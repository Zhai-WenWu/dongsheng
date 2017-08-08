package amodule.dish.video.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.container.VDVideoControlBottomRelativeContainer;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.sina.sinavideo.sdk.widgets.VDVideoFullScreenButton;
import com.sina.sinavideo.sdk.widgets.VDVideoPlaySeekBar;
import com.sina.sinavideo.sdk.widgets.VDVideoTimeTextView;
import com.xianghatest.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.CPUTool;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishListActivity;
import amodule.dish.business.StepVideoPosCompute;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.view.DishVideoImageView;
import third.video.VideoApplication;

/**
 * 视频预览页面
 */
public class MediaPreviewActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout surfaceVideoView;
    private RelativeLayout video_rela;
    private String path;//预览视频路径
    private int id;
    private String time="";//合成时间戳
    private String coverPath="";//当前大图路径
    public static WeakReference<Activity> mediaPreWeakRe;
    private VDVideoViewController controller;

    private VDVideoView mVDVideoView;
    private VDVideoInfo videoInfo;
    private DishVideoImageView dishView;
    private VDVideoControlBottomRelativeContainer container_bottom;
    private VDVideoPlaySeekBar playerseek2;
    private List<Float> videoPoints;
    private float totalTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        initVideoView();
    }


    private void initData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            path = bundle.getString("path");
            id = bundle.getInt("id");
            time = bundle.getString("time");
            coverPath = bundle.getString("coverPath");
            videoPoints = new ArrayList<Float>();
            getPosList();
            getVideoTime();
        }
        mediaPreWeakRe = new WeakReference<Activity>(this);
    }


    private void initView() {

        initActivity("", 5, 0, 0, R.layout.a_media_preview);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.video_upload).setOnClickListener(this);

        video_rela = (RelativeLayout) findViewById(R.id.video_rela);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) video_rela.getLayoutParams();
        params.height = Tools.getPhoneWidth() * 9 / 16;
        params.width = Tools.getPhoneWidth();
        video_rela.setLayoutParams(params);

        surfaceVideoView = (LinearLayout) findViewById(R.id.surfaceVideoView);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) surfaceVideoView.getLayoutParams();
        params2.height = Tools.getPhoneWidth() * 9 / 16;
        params2.width = Tools.getPhoneWidth();
        surfaceVideoView.setLayoutParams(params2);

        initTitle();
    }



    private void initVideoView() {

        if (ToolsDevice.isNetworkAvailable(this)) {
            try {
                VideoApplication.getInstence().initialize(this);
            } catch (Exception e) {
                statisticsInitVideoError(this);
            }
        }
        mVDVideoView = new VDVideoView(this);
        mVDVideoView.setLayers(R.array.my_videoview_layers);

        mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
                //
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        surfaceVideoView.addView(mVDVideoView, layoutParams);
        mVDVideoView.setVDVideoViewContainer(surfaceVideoView);

        dishView = new DishVideoImageView(this);
        dishView.setData(getCoverPath(), "");
        video_rela.addView(dishView);

        if(TextUtils.isEmpty(path)){
            Toast.makeText(this,"视频路径为空",Toast.LENGTH_SHORT).show();
            return;
        }

        if(new File(path).length() < 1){
            Toast.makeText(this,"视频文件大小为0",Toast.LENGTH_SHORT).show();
            return;
        }
        path = path.replace("storage/emulated/0", "sdcard");
        videoInfo = new VDVideoInfo(path);
        videoInfo.mTitle = "";

        dishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void modifyPlayerView() {

        container_bottom = (VDVideoControlBottomRelativeContainer) findViewById(R.id.container_bottom);
        playerseek2 = (VDVideoPlaySeekBar) findViewById(R.id.playerseek2);

        //设置滑动条位置
        RelativeLayout.LayoutParams playerseekParam = (RelativeLayout.LayoutParams) playerseek2.getLayoutParams();
        playerseekParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        //去掉全屏按钮
        VDVideoFullScreenButton fullscreen1 = (VDVideoFullScreenButton) mVDVideoView.findViewById(R.id.fullscreen1);
        fullscreen1.setVisibility(View.GONE);

        //设置时间位置
        VDVideoTimeTextView timeTextView = (VDVideoTimeTextView) findViewById(R.id.timeTextView);
        RelativeLayout.LayoutParams timeTextViewParam = (RelativeLayout.LayoutParams) timeTextView.getLayoutParams();
        timeTextViewParam.addRule(RelativeLayout.ALIGN_RIGHT, R.id.playerseek2);
        timeTextViewParam.setMargins(0,0,0,3);
        timeTextView.setVisibility(View.VISIBLE);

        LinearLayout pointsLine = new LinearLayout(this);
        pointsLine.setOrientation(LinearLayout.HORIZONTAL);
        pointsLine.setBackgroundColor(getResources().getColor(R.color.transparent));
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(this, 2));
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.playerseek2);
        params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.playerseek2);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        pointsLine.setWeightSum(totalTime);
        float prePointTime = 0f;
        if (totalTime == 0f || videoPoints == null || videoPoints.size() < 1)
            return;
        for (Float pointTime : videoPoints) {
            fillLinView(pointsLine, pointTime - prePointTime);
            prePointTime = pointTime;
        }

        fillLinView(pointsLine, totalTime - prePointTime);
        container_bottom.addView(pointsLine, params);

    }

    private void addPointToView(LinearLayout view) {
        LinearLayout pointView = new LinearLayout(this);
        pointView.setBackgroundColor(getResources().getColor(R.color.quan_green_bg_bartitle_text));
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ToolsDevice.dp2px(this, 1), ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        view.addView(pointView,params);
    }


    private void fillLinView(LinearLayout pointsLine, Float pointTime) {
        LinearLayout view = new LinearLayout(this);
        LinearLayout.LayoutParams params1 =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, pointTime);
        view.setOrientation(LinearLayout.HORIZONTAL);
        addPointToView(view);
        pointsLine.addView(view, params1);
    }


    private void startPlay() {

        if (!ToolsDevice.isNetworkAvailable(this)) {
            Tools.showToast(this, "网络故障，请检查网络是否可用");
        }

        if (VideoApplication.getInstence().isInitSuccess()) {
            try {
                mVDVideoView.open(this, videoInfo);
                mVDVideoView.play(0);
            } catch (Exception e) {
                Tools.showToast(this, "视频解码库加载失败，请重试");
                FileManager.delDirectoryOrFile(Environment.getDataDirectory() + "/data/com.xianghatest/libs/");
                VideoApplication.getInstence().initialize(this);
                return;
            }
            video_rela.removeView(dishView);
        } else {
            Tools.showToast(this, "加载视频解码库中...");
            VideoApplication.getInstence().initialize(this);
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        modifyPlayerView();
        if (mVDVideoView != null)
            mVDVideoView.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mVDVideoView != null)
            mVDVideoView.onStart();
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);

            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
//                MediaHandleControl.delAllMediaHandlerData(id);
                this.finish();
                break;
            case R.id.video_upload://发布点击
                XHClick.mapStat(this, "a_video_preview", "发布", "");
                Intent intent = new Intent(this, UploadDishListActivity.class);
                intent.putExtra("draftId", id);
                intent.putExtra("coverPath", coverPath);
                intent.putExtra("time", time);
                intent.putExtra("finalVideoPath", path);
                startActivity(intent);
                MediaPreviewActivity.this.finish();
                break;
        }
    }

    /**
     * 展示dialog
     */
    private void showDialog() {
        final Dialog dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.a_mall_alipa_dialog);
        Window window = dialog.getWindow();
        window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
        TextView dialog_message = (TextView) window.findViewById(R.id.dialog_message);
        dialog_message.setText("你确认不发布本视频吗?");
        TextView dialog_cancel = (TextView) window.findViewById(R.id.dialog_cancel);
        TextView dialog_sure = (TextView) window.findViewById(R.id.dialog_sure);
        dialog_cancel.setText("取消");
        dialog_sure.setText("确定");
        dialog_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                XHClick.mapStat(MediaPreviewActivity.this, "a_video_preview", "发布视频", "取消");
                dialog.cancel();
            }
        });
        dialog_sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                XHClick.mapStat(MediaPreviewActivity.this, "a_video_preview", "发布视频", "确定");
                dialog.cancel();
                MediaPreviewActivity.this.finish();
            }
        });
        dialog.show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVDVideoView != null) {
            return mVDVideoView.onVDKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVDVideoView != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mVDVideoView.setIsFullScreen(true);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mVDVideoView.setIsFullScreen(false);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVDVideoView != null)
            mVDVideoView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVDVideoView != null)
            mVDVideoView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVDVideoView != null)
            mVDVideoView.release(false);
    }

    //统计视频初始化错误
    private void statisticsInitVideoError(Context context) {
//		Tools.showToast(context, "您的手机暂时不支持播放视频");
        XHClick.mapStat(context, "init_video_error", "CPU型号", "" + CPUTool.getCpuName());
        XHClick.mapStat(context, "init_video_error", "手机型号", android.os.Build.BRAND + "_" + android.os.Build.MODEL);
    }

    private String getCoverPath() {
        String path = "";
        UploadDishSqlite dishSqlite = new UploadDishSqlite(XHApplication.in().getApplicationContext());
        UploadDishData uploadDishData = dishSqlite.selectById(id);
        if (uploadDishData != null) {
            path = uploadDishData.getCover();
        }
        return path;
    }

    private void getPosList() {
        Map<String, ArrayList> posPointMap = new StepVideoPosCompute().computeStepPos(id);
        ArrayList<List<Float>> lists = posPointMap.get("stepPos");
        if (lists == null || lists.size() < 1)
            return;
        int size = lists.size();
        for(int i = 0; i< size; i++){
            List<Float> poins = lists.get(i);
            videoPoints.add(poins.get(0));
            if(i == size -1)
                videoPoints.add(poins.get(1));
        }
    }

    private void getVideoTime() {
            if(Tools.isFileExists(path)){
//                MediaInfo mediaInfo = new MediaInfo(path);
//                if(mediaInfo!=null){
//                    mediaInfo.prepare();
//                }
//                if(mediaInfo!=null){
//                    totalTime = mediaInfo.vDuration;
//                }
            }
    }

    @Override
    public void onBackPressed() {
//        MediaHandleControl.delAllMediaHandlerData(id);
        super.onBackPressed();

    }
}
