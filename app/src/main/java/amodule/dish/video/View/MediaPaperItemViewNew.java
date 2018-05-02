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

import com.xiangha.R;

import java.util.Timer;
import java.util.TimerTask;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.video.bean.MediaPaperBean;

import static com.xiangha.R.id.item_surfaceVideoView;

/**
 * 视频裁剪itemview
 */
public class MediaPaperItemViewNew extends RelativeLayout implements View.OnClickListener{
    private Activity context;
    private  MediaSurfaceVideoView surfaceVideoView;
    /** 是否需要回复播放 */
    private boolean mNeedResume;
    private String mPath;//视频路径
    private boolean isPrepared =false;
    private boolean isOnClickStart=false;
    private MediaPaperBean mediaBean;
    private int view_index=0;
    private int size=0;
    private int media_waith=0;
    private int media_height=0;
    private Timer timer;
    private android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    float nowtime=surfaceVideoView.getPosition();
                    Log.i("zhangyujian",":当时时间::："+nowtime/1000);
                    callBack.changeVideoTime(nowtime/1000);
                    break;
            }
        }
    };
    public MediaPaperItemViewNew(Activity context, MediaPaperBean mediaBean, int position, int size) {
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
        LayoutInflater.from(context).inflate(R.layout.a_media_paper_item_new,this,true);
        surfaceVideoView= (MediaSurfaceVideoView) findViewById(item_surfaceVideoView);
        surfaceVideoView.setOnClickListener(this);
        RelativeLayout media_rela = (RelativeLayout) findViewById(R.id.media_rela);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,media_height);
        media_rela.setLayoutParams(layoutParams);

        initData();
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
                    MediaPaperItemViewNew.this.context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }else {//取消高亮
                    MediaPaperItemViewNew.this.context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
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
        public void changeVideoTime(float progressTime);
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
        if(timer!=null)timer.cancel();
        timer=null;
        callBack.changeVideoTime(-1);
//        rangeSeekBar.stopshow(mediaBean.startTime,mediaBean.getAllTime());
        callBack.changeVideoTime(0);
    }

    public void startVideo(){
        surfaceVideoView.getSurfaceVideoView().seekTo((int) (mediaBean.getStartTime() * 1000));
        surfaceVideoView.getSurfaceVideoView().pauseDelayed((int)mediaBean.getCutTime()*1000);
        surfaceVideoView.getSurfaceVideoView().start();
    }
    public void resetTime(){
        callBack.changeVideoTime(0);
    }
}
