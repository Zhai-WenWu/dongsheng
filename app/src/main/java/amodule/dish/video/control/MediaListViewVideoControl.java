package amodule.dish.video.control;

import amodule.dish.video.View.MediaListViewSurfaceVideoView;

/**
 * Created by Administrator on 2016/11/14.
 */
public class MediaListViewVideoControl {
    private static  MediaListViewVideoControl mediaControl;
    private  MediaListViewVideoControl(){}
    private MediaListViewSurfaceVideoView mediaListViewSurfaceVideoView;
    public static MediaListViewVideoControl getInstance(){
        if(mediaControl==null){
            synchronized (MediaControl.class){
                mediaControl= new MediaListViewVideoControl();
            }
        }
        return mediaControl;
    }

    /**
     * 开始
     */
    public void startAndStop(MediaListViewSurfaceVideoView videoview){
        if(mediaListViewSurfaceVideoView==null||mediaListViewSurfaceVideoView!=videoview){
            if(mediaListViewSurfaceVideoView!=null)
                mediaListViewSurfaceVideoView.onPause();
            mediaListViewSurfaceVideoView=videoview;
        }
        videoview.onClickView();
    }

    /**
     * 暂停
     */
    public void stop(){
        if(mediaListViewSurfaceVideoView!=null)
            mediaListViewSurfaceVideoView.onPause();
    }
}
