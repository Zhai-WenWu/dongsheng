package third.video;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiangha.R;

import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCNetworkBroadcastReceiver;
import fm.jiecao.jcvideoplayer_lib.JCUtils;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import fm.jiecao.jiecaovideoplayer.CustomView.XHVideoPlayerStandard;
import xh.basic.tool.UtilFile;

/**
 * Created by sll on 2017/6/12.
 */

public class SimpleVideoPlayerController extends VideoPlayerController {


    /**
     * 初始化操作：
     *
     * @param context
     * @param viewGroup ---布局容器
     * @param imgUrl    ---图片路径
     */
    public SimpleVideoPlayerController(Context context, ViewGroup viewGroup, String imgUrl) {
        super(context, viewGroup, imgUrl);
    }

    public SimpleVideoPlayerController(Context context) {
        super(context);
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
        videoPlayerStandard = new XHVideoPlayerStandard(mContext);
        videoPlayerStandard.setIsHideReplay(true);
        videoPlayerStandard.setIsShowThumbOnce(true);
        videoPlayerStandard.addNetworkNotifyListener(new JCNetworkBroadcastReceiver.NetworkNotifyListener() {
            @Override
            public void wifiConnected() {
                if(null != view_Tip){
                    view_Tip.performClick();
                    FileManager.saveShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI,"0");
                }
                onResume();
            }

            @Override
            public void mobileConnected() {
                if(view_Tip==null){
                    initView(mContext);
                    mPraentViewGroup.addView(view_Tip);
                }
                onPause();
            }

            @Override
            public void nothingConnected() {
                isNetworkDisconnect = true;
            }
        });
        videoPlayerStandard.setOnPlayErrorCallback(new JCVideoPlayerStandard.OnPlayErrorCallback() {
            @Override
            public boolean onError() {
                if(ToolsDevice.isNetworkAvailable(mContext)
                        && isNetworkDisconnect
                        && autoRetryCount < 3){
                    autoRetryCount++;
                    JCUtils.saveProgress(mContext,mVideoUrl,videoPlayerStandard.getCurrentPositionWhenPlaying());
                    videoPlayerStandard.startVideo();
                    return true;
                }
                return false;
            }
        });
        if (mPraentViewGroup == null)
            return;
        if (mPraentViewGroup.getChildCount() > 0) {
            mPraentViewGroup.removeAllViews();
        }
        mPraentViewGroup.addView(videoPlayerStandard);
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
}
