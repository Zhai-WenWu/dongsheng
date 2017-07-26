package third.video;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.VDVideoViewController;
import com.sina.sinavideo.sdk.VDVideoViewListeners;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.xiangha.R;

import acore.tools.FileManager;
import acore.tools.LogManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
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

        videoPlayerStandard = new XHVideoPlayerStandard(mContext);
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
        String temp= (String) UtilFile.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
            setShowMedia(true);
//        try {
//            //视频解码库初始化
//            VideoApplication.getInstence().initialize(mContext);
//        } catch (Exception e) {
//            statisticsInitVideoError(mContext);
//            LogManager.reportError("视频软解包初始化异常", e);
//            return;
//        } catch (Error e) {
//            statisticsInitVideoError(mContext);
//            return;
//        }
//
//        if (mVDVideoView == null) {
//            mVDVideoView = new VDVideoView(mContext);
//            mVDVideoView.setLayers(R.array.my_videoview_layers);
//            mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
//                //暂时未实现
//                @Override
//                public void onVDVideoCompletion(VDVideoInfo info, int status) {
//                }
//            });
//        }
//
//        VDVideoViewController controller = VDVideoViewController.getInstance(mContext);
//        if (controller != null) {
//            controller.addOnCompletionListener(new VDVideoViewListeners.OnCompletionListener() {
//                @Override
//                public void onCompletion() {
//                    if (mOnPlayingCompletionListener != null) {
//                        mOnPlayingCompletionListener.onPlayingCompletion();
//                    }
//                }
//            });
//        }
//        if (mPraentViewGroup == null)
//            return;
//        if (mPraentViewGroup.getChildCount() > 0) {
//            mPraentViewGroup.removeAllViews();
//        }
//        mPraentViewGroup.addView(mVDVideoView);
//        mVDVideoView.setVDVideoViewContainer((ViewGroup) mVDVideoView.getParent());
//        if (!TextUtils.isEmpty(mImgUrl)) {
//            if(view_Tip==null){
//                initView(mContext);
//                mPraentViewGroup.addView(view_Tip);
//            }
//            if (mImageView == null) {
//                mImageView = new ImageViewVideo(mContext);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                mImageView.playImgWH = Tools.getDimen(mContext, R.dimen.dp_50);
//                mImageView.parseItemImg(ImageView.ScaleType.CENTER_CROP, mImgUrl, true, false, R.drawable.i_nopic, FileManager.save_cache);
//                mImageView.setLayoutParams(params);
//                mPraentViewGroup.addView(mImageView);
//                this.view_dish=mImageView;
//                mImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        setOnClick();
//                    }
//                });
//            }
//        }
//        String temp= (String) UtilFile.loadShared(mContext,FileManager.SHOW_NO_WIFI,FileManager.SHOW_NO_WIFI);
//        if(!TextUtils.isEmpty(temp)&&"1".equals(temp))
//            setShowMedia(true);
    }
}
