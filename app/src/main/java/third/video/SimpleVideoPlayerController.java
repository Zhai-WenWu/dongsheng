package third.video;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.xianghatest.R;
import com.example.gsyvideoplayer.listener.SampleListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;

/**
 * Created by sll on 2017/6/12.
 */
public class SimpleVideoPlayerController extends VideoPlayerController {
    private Activity activity;


    /**
     * 初始化操作：
     *
     * @param context
     * @param viewGroup ---布局容器
     * @param imgUrl    ---图片路径
     */
    public SimpleVideoPlayerController(Activity context, ViewGroup viewGroup, String imgUrl) {
        super(context, viewGroup, imgUrl);
    }

    public SimpleVideoPlayerController(Activity context) {
        super(context);
        this.activity = context;

    }

    public void setViewGroup(ViewGroup parent) {
        mPraentViewGroup = parent;
    }

    public void setImgUrl(String imgUrl) {
        mImgUrl = imgUrl;
    }

    public void initView() {
        //初始化网络断开链接
        isNetworkDisconnect = false;

        if(videoPlayer == null){
            videoPlayer = new StandardGSYVideoPlayer(mContext);
        }else{
            onDestroy();
        }
        //设置旋转
        orientationUtils = new OrientationUtils(activity, videoPlayer);
        orientationUtils.setEnable(false);
        videoPlayer.setShowFullAnimation(true);
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                videoPlayer.startWindowFullscreen(mContext, true, true);
            }
        });
        videoPlayer.setNeedShowWifiTip(true);
        videoPlayer.setStandardVideoAllCallBack(new SampleListener(){
            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                if(onPlayingCompletionListener != null){
                    onPlayingCompletionListener.onPlayingCompletion();
                    videoPlayer.hideAllWidget();
                }
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                if(url.startsWith("http"))
                    setNetworkCallback();
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                if(!isPortrait)
                    orientationUtils.resolveByClick();
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                orientationUtils.backToProtVideo();
            }
        });

        Resources resources = mContext.getResources();
        videoPlayer.setBottomProgressBarDrawable(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setDialogVolumeProgressBar(resources.getDrawable(R.drawable.video_new_volume_progress_bg));
        videoPlayer.setDialogProgressBar(resources.getDrawable(R.drawable.video_new_progress));
        videoPlayer.setBottomShowProgressBarDrawable(resources.getDrawable(R.drawable.video_new_seekbar_progress),
                resources.getDrawable(R.drawable.video_new_seekbar_thumb));

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);
        videoPlayer.setIsTouchWigetFull(true);

//        videoPlayerStandard.setOnPlayErrorCallback(new JCVideoPlayerStandard.OnPlayErrorCallback() {
//            @Override
//            public boolean onError() {
//                if(ToolsDevice.isNetworkAvailable(mContext)
//                        && isNetworkDisconnect
//                        && autoRetryCount < 3){
//                    autoRetryCount++;
//                    JCUtils.saveProgress(mContext,mVideoUrl,videoPlayerStandard.getCurrentPositionWhenPlaying());
//                    videoPlayerStandard.startVideo();
//                    return true;
//                }
//                return false;
//            }
//        });
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(videoPlayer);
        if (!TextUtils.isEmpty(mImgUrl)) {
            if(view_Tip==null){
                initView(mContext);
                mPraentViewGroup.addView(view_Tip);
            }
            mImageView = new ImageViewVideo(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
            mImageView.parseItemImg(ImageView.ScaleType.CENTER_CROP, mImgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
            mImageView.setLayoutParams(params);
            mPraentViewGroup.addView(mImageView);
            this.view_dish=mImageView;
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setOnClick();
                }
            });
        }
        String temp= (String) FileManager.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp) && "1".equals(temp))
            setShowMedia(true);
    }

    private void setNetworkCallback(){
        videoPlayer.addListener(new StandardGSYVideoPlayer.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                Log.i("tzy","wifiConnected");
                removeTipView();
                if(view_dish != null){
                    view_dish.performClick();
                }else{
                    onResume();
                }
                isNetworkDisconnect = false;
            }

            @Override
            public void mobileConnected() {
                Log.i("tzy","mobileConnected");
                if(!"1".equals(FileManager.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI).toString())){
                    if(isNetworkDisconnect){
                        removeTipView();
                        if(view_Tip==null){
                            initView(mContext);
                            mPraentViewGroup.addView(view_Tip);
                        }
                        onPause();
                    }
                }else if(videoPlayer.getCurrentState() == GSYVideoPlayer.CURRENT_STATE_PAUSE){
                    removeTipView();
                    onResume();
                }
                isNetworkDisconnect = false;
            }

            @Override
            public void nothingConnected() {
                Log.i("tzy","nothingConnected");
                if(view_Tip==null){
                    initNoNetwork(mContext);
                    mPraentViewGroup.addView(view_Tip);
                }
                onPause();
                isNetworkDisconnect = true;
            }
        });
    }
}
