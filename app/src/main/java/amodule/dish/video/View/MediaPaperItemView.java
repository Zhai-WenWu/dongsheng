package amodule.dish.video.View;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.video.bean.MediaPaperBean;

import static com.xiangha.R.id.item_surfaceVideoView;

/**
 * 视频裁剪itemview
 */
public class MediaPaperItemView extends RelativeLayout implements View.OnClickListener{
    private Activity context;
    private  MediaSurfaceVideoView surfaceVideoView;
    /** 是否需要回复播放 */
    private boolean mNeedResume;
    private String mPath;//视频路径
    private boolean isPrepared =false;
    private boolean isOnClickStart=false;
    private  RangeSeekBar<Float> rangeSeekBar;
    private MediaPaperBean mediaBean;
    private TextView pager_time,pager_index_1,pager_index_2;
    private int view_index=0;
    private int size=0;
    private int media_waith=0;
    private int media_height=0;
    private Timer timer;
    private float nowValue=0;
    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    nowValue+=0.1;
                    rangeSeekBar.startNowShow(nowValue,mediaBean.getAllTime());
                    break;
            }
        }
    };
    public MediaPaperItemView(Activity context, MediaPaperBean mediaBean, int position,int size) {
        super(context);
        this.mPath=mediaBean.getPath();
        this.mediaBean= mediaBean;
        this.view_index=position;
        this.size= size;
        media_waith= ToolsDevice.getWindowPx(context).widthPixels- Tools.getDimen(context,R.dimen.dp_40);
        media_height= media_waith/16*9;
        initView(context);
    }
    private void initView(Activity context){
        this.context= context;
        LayoutInflater.from(context).inflate(R.layout.a_media_paper_item,this,true);
        surfaceVideoView= (MediaSurfaceVideoView) findViewById(item_surfaceVideoView);
        surfaceVideoView.setOnClickListener(this);
        rangeSeekBar= (RangeSeekBar<Float>) findViewById(R.id.rangseekbar);
        pager_time= (TextView) findViewById(R.id.pager_time);
        pager_index_1= (TextView) findViewById(R.id.pager_index_1);
        pager_index_2= (TextView) findViewById(R.id.pager_index_2);
        RelativeLayout media_rela = (RelativeLayout) findViewById(R.id.media_rela);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,media_height);
        media_rela.setLayoutParams(layoutParams);

        rangeSeekBar.setRangeValues((float)0, mediaBean.getAllTime());
        rangeSeekBar.setSelectedMinValue(mediaBean.getStartTime());
        rangeSeekBar.setSelectedMaxValue(mediaBean.getEndTime());

        pager_time.setText( mediaBean.getCutTime()+"秒");
        pager_index_2.setText("/"+size);
        pager_index_1.setText(String.valueOf(view_index+1));
        initData();
        setLisenter();
        findViewById(R.id.image_left).setOnClickListener(this);
        findViewById(R.id.image_right).setOnClickListener(this);
        if(view_index==0){
           findViewById(R.id.image_left).setVisibility(View.INVISIBLE);
            if(view_index==size-1)findViewById(R.id.image_right).setVisibility(View.INVISIBLE);
            else findViewById(R.id.image_right).setVisibility(View.VISIBLE);
        }else if(view_index==size-1){
            findViewById(R.id.image_left).setVisibility(View.VISIBLE);
            findViewById(R.id.image_right).setVisibility(View.INVISIBLE);
        }else{
            findViewById(R.id.image_left).setVisibility(View.VISIBLE);
            findViewById(R.id.image_right).setVisibility(View.VISIBLE);
        }
        surfaceVideoView.setCallBack(new MediaSurfaceVideoView.MediaStateCallBack() {
            @Override
            public void getMediaState(boolean state) {

                if(state){//保持高亮
                    MediaPaperItemView.this.context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }else {//取消高亮
                    MediaPaperItemView.this.context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                nowValue=mediaBean.getStartTime();
                if(state){
                    Log.i("itemview","开始：");
                    startShowPlayer();
                }else{
                    Log.i("itemview","关闭：");
                    stopShowPlayer();
                }
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData(){
        surfaceVideoView.setMediaBean(mediaBean);
    }
    /**
     * 设置数据
     */
    public void setData(){

    }
    /**
     * 开始播放
     */
    public void startVideo(){
        isOnClickStart=true;
        if(isOnClickStart&&isPrepared) {
//            surfaceVideoView.start();
        }
    }
    private void setLisenter(){
        rangeSeekBar.setNumberCallBack(new RangeSeekBar.NumberCallBack() {

            @Override
            public void getstartAndEndValue(float startValue, float endValue,boolean isTouchState) {
                Log.i("time",startValue+"::::"+endValue+":::"+isTouchState);
                DecimalFormat df   = new DecimalFormat("######0.0");
                df.format(startValue);
                df.format(endValue);
                if(startValue!=mediaBean.getStartTime()||endValue!=mediaBean.getEndTime()){
                    mediaBean.setStartTime(startValue);
                    mediaBean.setEndTime(endValue);
                    surfaceVideoView.getSurfaceVideoView().seekTo((int) (startValue * 1000));

                    pager_time.setText(String.valueOf(mediaBean.getCutTime())+"秒");
                    if(isTouchState){
                        surfaceVideoView.getSurfaceVideoView().pauseDelayed((int)mediaBean.getCutTime()*1000);
                        surfaceVideoView.getSurfaceVideoView().start();

                    }
                    if(callBack!=null)callBack.changeVideoTime();
                }
            }
        });
    }

    public void onResume(){
        Log.i("time","onResume");
        surfaceVideoView.onResume();
    }
    public void onPause(){
        Log.i("time","onPause");
        surfaceVideoView.onPause();
    }

    public void onDestory(){
        Log.i("time","onDestory");
        surfaceVideoView.onDestory();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.item_surfaceVideoView:
                surfaceVideoView.onClickView();
                break;
            case R.id.image_left:
                if(findViewById(R.id.image_left).getVisibility()==VISIBLE)
                callBack.changeVideoIndex(false);
                break;
            case R.id.image_right:
                if(findViewById(R.id.image_right).getVisibility()==VISIBLE)
                callBack.changeVideoIndex(true);
                break;
        }
    }

    /**
     * 时间回调
     */
    public interface VideoTimeCallBack{
        public void changeVideoTime();
        public void changeVideoIndex(boolean state);
    }
    private VideoTimeCallBack callBack;
    public void setChangeTimeCallBack(VideoTimeCallBack callBack){
        this.callBack= callBack;
    }

    private void startShowPlayer(){
        if(timer==null) timer= new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        },0,100);
    }

    private void stopShowPlayer(){
        timer.cancel();
        timer=null;
//        rangeSeekBar.stopshow(mediaBean.startTime,mediaBean.getAllTime());
    }

}
