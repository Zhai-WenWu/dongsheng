package amodule.dish.video.View;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import org.json.JSONObject;

import amodule.dish.video.bean.MediaPaperBean;
import aplug.recordervideo.tools.ToolsCammer;

/**
 * 视频播放视频业务view类
 */
public class MediaListViewSurfaceVideoView extends RelativeLayout  {
    private Context context;
    private SurfaceVideoView surfaceVideoView;
    private ImageView stopView, topView;
    private boolean isShowStop = false;//是否显示暂停
    private String shopTopPath = "";//顶部显示view路径
    private MediaPaperBean mediaBean;
    private int position; //当前所在位置----更换数据要用
    private boolean startVideo=false;
    private boolean onClickView=false;
    private boolean isPlay=false;//当前播放器状态

    /**
     * 是否需要回复播放
     */
    private boolean mNeedResume;

    public MediaListViewSurfaceVideoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MediaListViewSurfaceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MediaListViewSurfaceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.view_media_video, this, true);
        surfaceVideoView = (SurfaceVideoView) findViewById(R.id.surfaceVideoView);
        stopView = (ImageView) findViewById(R.id.stopView);
        topView = (ImageView) findViewById(R.id.topView);
//        surfaceVideoView.
        setLisenter();

    }

    private void setLisenter() {
        //准备监听
        surfaceVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPlay=false;
                startVideo=true;
                if(!onClickView) {
                    stopView.setVisibility(View.VISIBLE);
                    topView.setVisibility(View.VISIBLE);
                    surfaceVideoView.seekTo((int) mediaBean.getStartTime() * 1000);
//                surfaceVideoView.start();
                    surfaceVideoView.pause();
                }else{
                    onClickView();
                }
            }
        });
        //播放状态监听
        surfaceVideoView.setOnPlayStateListener(new SurfaceVideoView.OnPlayStateListener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
//                stopView.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
                Log.i("zhangyujian","isPlaying::"+isPlaying);
                if(isPlaying){
                    isPlay=true;
                    stopView.setVisibility(View.GONE);
                    topView.setVisibility(View.GONE);
                }else{
                    isPlay=false;
                    stopView.setVisibility(View.VISIBLE);
                    topView.setVisibility(View.VISIBLE);
                }
            }
        });
        //播放失败监听
        surfaceVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                topView.setVisibility(View.VISIBLE);
                stopVideo();
                if(callBack!=null)callBack.playState(true);
                return false;
            }
        });
        //播放实时监听
        surfaceVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(callBack!=null)callBack.playState(true);
                topView.setVisibility(View.VISIBLE);
                stopVideo();
            }
        });
        surfaceVideoView.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
            }
        });

        surfaceVideoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickView();
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
    public void onClickView(){
        onClickView=true;
        Log.i("zhangyujian",":::"+startVideo+":::onClickView:::"+surfaceVideoView.isPlaying());
        if(startVideo) {
            if (surfaceVideoView.isPlaying())
                surfaceVideoView.pause();
            else {
                surfaceVideoView.seekTo((int) (mediaBean.getStartTime() * 1000));
                surfaceVideoView.pauseDelayed((int) mediaBean.getCutTime() * 1000);
                surfaceVideoView.start();
            }
        }
    }

    /**
     * 初始化数据--并进行初始化
     * @param mediaBean
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setMediaBean(MediaPaperBean mediaBean){

        Log.i("zhangyujian","::::"+mediaBean.getPath());
        this.mediaBean= mediaBean;
        if(mediaBean.isCut){
            topView.setBackground(new BitmapDrawable(ToolsCammer.getFrameAtTime(mediaBean.getPath(), (long) (mediaBean.getStartTime()*1000))));
        }else{
            topView.setBackground(new BitmapDrawable(ToolsCammer.getFrameAtTime(mediaBean.getPath())));
        }
        topView.setVisibility(View.VISIBLE);
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
        isPlay = false;
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
            Log.i("surfaceview","str::"+str);
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
    public void setInfo(String str,String state,String mediaState){
        Log.i("surfaceview","state::"+state+":::mediaState::"+mediaState);
        try {
            JSONObject jsonObject= new JSONObject(str);
            MediaPaperBean mediaPaperBean= new MediaPaperBean();
            mediaPaperBean.jsonToBean(jsonObject);
            setMediaBean(mediaPaperBean);
            if(state!=null&&state.equals("onResume")){
                onResume();
//                setStateData(mediaState);
            }else if(state!=null&&state.equals("onPause")){
                onPause();
            }else if(state!=null&&state.equals("onDestory")){
                onDestory();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public interface  MediaViewCallBack{
        /**
         * state true当前正在播放，false不是播放状态
         * @param state
         */
        public void mediaPlayState(boolean state);
    }

    /**
     *
     * @param mediaState
     */
    public void setStateData(String mediaState){
        Log.i("surfaceview","mediaState::"+mediaState);
        if("1".equals(mediaState)){
            surfaceVideoView.pause();
            stopView.setVisibility(View.VISIBLE);
            topView.setVisibility(View.VISIBLE);
        }else{
            surfaceVideoView.reOpen();
            surfaceVideoView.seekTo((int) (mediaBean.getStartTime() * 1000));
            surfaceVideoView.pauseDelayed((int) mediaBean.getCutTime() * 1000);
            surfaceVideoView.start();
        }
    }
    public void resele(){
        surfaceVideoView.release();
    }

    /**
     * 获取当前播放器状态
     * @return
     */
    public boolean isPlay(){
        return isPlay;
    }
    public interface ViewPlayCallBack{
        public void playState(boolean state);
    }
    private ViewPlayCallBack callBack;

    /**
     * 设置
     * @param callBack
     */
    public void setPlayCallBack(ViewPlayCallBack callBack){
        this.callBack = callBack;
    }
}
