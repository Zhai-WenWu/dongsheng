package amodule.dish.video.View;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xiangha.R;

import org.json.JSONObject;

import amodule.dish.video.bean.MediaPaperBean;

/**
 * 视频播放视频业务view类
 */
public class MediaSurfaceVideoView extends RelativeLayout  {
    private Context context;
    private SurfaceVideoView surfaceVideoView;
    private ImageView stopView, topView;
    private boolean isShowStop = false;//是否显示暂停
    private String shopTopPath = "";//顶部显示view路径
    private MediaPaperBean mediaBean;
    private int position; //当前所在位置----更换数据要用
    /**
     * 是否需要回复播放
     */
    private boolean mNeedResume;

    public MediaSurfaceVideoView(Context context) {
        super(context);
        this.context = context;
    }

    public MediaSurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MediaSurfaceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    private void init(MediaPaperBean mediaBean) {
        this.mediaBean= mediaBean;
        LayoutInflater.from(context).inflate(R.layout.view_media_video, this, true);
        surfaceVideoView = (SurfaceVideoView) findViewById(R.id.surfaceVideoView);
        stopView = (ImageView) findViewById(R.id.stopView);
        topView = (ImageView) findViewById(R.id.topView);
        setLisenter();
//        surfaceVideoView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int height=surfaceVideoView.getVideoWidth();
//                RelativeLayout.LayoutParams  layoutParams = new RelativeLayout.LayoutParams(height/5,height/5);
//                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                stopView.setLayoutParams(layoutParams);
//            }
//        },100);
    }

    private void setLisenter() {
        //准备监听
        surfaceVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                stopView.setVisibility(View.VISIBLE);
//                surfaceVideoView.start();
                surfaceVideoView.seekTo((int) mediaBean.getStartTime() * 1000);
                surfaceVideoView.pause();
            }
        });
        //播放状态监听
        surfaceVideoView.setOnPlayStateListener(new SurfaceVideoView.OnPlayStateListener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
                stopView.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
                if(isPlaying){
                    if(callBack!=null)callBack.getMediaState(true);
                }else{
                    if(callBack!=null)callBack.getMediaState(false);
                }
            }
        });
        //播放失败监听
        surfaceVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if(callBack!=null)callBack.getMediaState(false);
                return false;
            }
        });
        //播放实时监听
        surfaceVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(callBack!=null)callBack.getMediaState(false);
            }
        });
    }

    public void onResume() {
        if (surfaceVideoView != null && mNeedResume) {
            mNeedResume = false;
            if (surfaceVideoView.isRelease())
                surfaceVideoView.reOpen();
            else
                surfaceVideoView.start();
        }
    }

    public void onPause() {
        if (surfaceVideoView != null) {
            if (surfaceVideoView.isPlaying()) {
                mNeedResume = true;
                surfaceVideoView.pause();
            }
        }
    }

    public void onDestory() {
        if (surfaceVideoView != null) {
            surfaceVideoView.release();
            surfaceVideoView = null;
        }
    }

    /**
     * 点击事件
     */
    public  void onClickView(){
//        if (surfaceVideoView.isPlaying())
//            surfaceVideoView.pause();
//        else {

            surfaceVideoView.seekTo((int) (mediaBean.getStartTime() * 1000));
            surfaceVideoView.pauseDelayed((int) mediaBean.getCutTime() * 1000);
            surfaceVideoView.start();

//        }
    }

    /**
     * 初始化数据--并进行初始化
     * @param mediaBean
     */
    public void setMediaBean(MediaPaperBean mediaBean){
        init(mediaBean);
        surfaceVideoView.setVideoPath(mediaBean.getPath());

    }

    /**
     * 获取当前view
     * @return
     */
    public SurfaceVideoView getSurfaceVideoView(){
        return surfaceVideoView;
    }
    /**
     * 暂停视频
     */
    public void stopVideo(){
        if (surfaceVideoView != null && surfaceVideoView.isPlaying())
            surfaceVideoView.pause();
    }

    /**
     * 设置JsonObject
     * @param str
     */
    public void setInfo(String str){
        try {
            JSONObject jsonObject= new JSONObject(str);
            MediaPaperBean mediaPaperBean= new MediaPaperBean();
            mediaPaperBean.jsonToBean(jsonObject);
            setMediaBean(mediaPaperBean);
        }catch (Exception e){

        }
    }

    /**
     * 设置JsonObject
     * @param str
     */
    public void setInfo(String str,String state){
        try {
            JSONObject jsonObject= new JSONObject(str);
            MediaPaperBean mediaPaperBean= new MediaPaperBean();
            mediaPaperBean.jsonToBean(jsonObject);
            setMediaBean(mediaPaperBean);
        }catch (Exception e){

        }
    }
    public interface MediaStateCallBack{
        /**
         * 当前状态，state true为播放状态，false为非播放状态
         * @param state
         */
        public void getMediaState(boolean state);
    }
    public MediaStateCallBack callBack;
    public void setCallBack(MediaStateCallBack callBack){
        this.callBack= callBack;
    }
    public int getPosition(){
        int position = 0;
        if (surfaceVideoView != null)
            position = surfaceVideoView.getCurrentPosition();
        return position;
    }
}

