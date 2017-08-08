package amodule.quan.view;

import android.app.Activity;
import android.content.Context;

import com.sina.sinavideo.sdk.VDVideoExtListeners;
import com.sina.sinavideo.sdk.VDVideoView;
import com.sina.sinavideo.sdk.data.VDVideoInfo;
import com.xianghatest.R;

import acore.logic.XHClick;
import acore.tools.CPUTool;
import acore.tools.ToolsDevice;
import third.video.VideoApplication;

/**
 * Created by ：fei_teng on 2017/2/10 16:05.
 */

public class CircleVideoController {

    private VDVideoView mVDVideoView;

    public static volatile CircleVideoController  circleVideoController;
    private CircleVideoController(){
    }

    public static CircleVideoController getCircleVideoControllerInstance(){
        if(circleVideoController == null){
            synchronized (CircleVideoController.class){
                if(circleVideoController == null){
                    circleVideoController = new CircleVideoController();
                }
            }
        }
        return circleVideoController;
    }


    public VDVideoView getVDVideoView(Activity activity) {

        if (ToolsDevice.isNetworkAvailable(activity)) {
            try {
                VideoApplication.getInstence().initialize(activity);
            } catch (Exception e) {
                statisticsInitVideoError(activity);
            }
        }

        if(mVDVideoView != null){
            if(activity.equals(mVDVideoView.getContext())){
                return mVDVideoView;
            }else{
                mVDVideoView.release(false);
                mVDVideoView = createVDVideoView(activity);
            }
        }else{
            mVDVideoView = createVDVideoView(activity);
        }
        return  mVDVideoView;
    }


    private VDVideoView createVDVideoView(Activity activity){

        VDVideoView  mVDVideoView = new VDVideoView(activity);
        mVDVideoView.setLayers(R.array.my_videoview_layers);

        mVDVideoView.setCompletionListener(new VDVideoExtListeners.OnVDVideoCompletionListener() {
            @Override
            public void onVDVideoCompletion(VDVideoInfo info, int status) {
            }
        });

        return mVDVideoView;
    }

    //统计视频初始化错误
    private void statisticsInitVideoError(Context context) {
        XHClick.mapStat(context, "init_video_error", "CPU型号", "" + CPUTool.getCpuName());
        XHClick.mapStat(context, "init_video_error", "手机型号", android.os.Build.BRAND + "_" + android.os.Build.MODEL);
    }

}
