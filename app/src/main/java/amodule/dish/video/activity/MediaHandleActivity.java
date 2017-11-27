package amodule.dish.video.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.Timer;
import java.util.TimerTask;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.tools.DeviceUtilDialog;

/**
 *合成页面
 */

public class MediaHandleActivity extends BaseActivity implements View.OnClickListener{
    private ProgressBar progressBar;
    private TextView progress_text;
    private UploadDishData uploadDishData;
//    private MediaHandleControl mediaHandlerContorl;
    private int progress_now=0;//当前进度
    private int progress_new=20;//可以达到的进度
    private Timer originalTimer,sucessTimer;
    private final int STATE_PROCESS=101;
    private String path_finish="";
    private int id_data=-1;
    private String time_key="";//合成时间戳
    private String coverPath="";//当前大图路径
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case STATE_PROCESS:
                    if(progress_now<progress_new)
                        progressBar.setProgress(progress_now++);
                    progress_text.setText(progress_now+"%");
                    if(progress_now>=100){
                        cancelOriTime();
                        cancelSucessTimer();
                        startPreview();
                    }
                    Log.i("mediahandler","progress_now::"+progress_now);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle= getIntent().getExtras();
        if(bundle!=null){
            uploadDishData= (UploadDishData) bundle.getSerializable("uploadDishData");
        }
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initData();
    }

    private void initView() {
        initActivity("", 3, 0, 0, R.layout.media_handle);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progress_text= (TextView) findViewById(R.id.progress_text);
        findViewById(R.id.media_del).setOnClickListener(this);
    }

    private void initData() {
        DeviceUtilDialog dialog= new DeviceUtilDialog(this);
        dialog.deviceLowState(new DeviceUtilDialog.DeviceCallBack() {
            @Override
            public void backResultState(Boolean state) {
            }
        });
//        mediaHandlerContorl= new MediaHandleControl(this,uploadDishData);
//        mediaHandlerContorl.startVideo();
//        mediaHandlerContorl.setHandlerDataCallBack(new MediaHandleControl.HandlerDataCallBack() {
//            @Override
//            public void setCallBack(int progress) {
//                progress_new=progress;
//                Log.i("mediahandler","progress_new::"+progress_new);
//            }
//
//            @Override
//            public void CallBackError() {
//                //出现错误，终止全部操作
//                XHClick.mapStat(MediaHandleActivity.this,"a_video_splice","合成失败","");
//            }
//
//            @Override
//            public void callBackSucess(String path,int id,String time,String cover) {
//                XHClick.mapStat(MediaHandleActivity.this,"a_video_splice","合成成功","");
//                Log.i("zhangyujian","合成成功:::"+path+":::"+id);
//                path_finish= path;
//                id_data=id;
//                time_key= time;
//                coverPath= cover;
//                //成功
//                if(progress_now<=95) {
//                    startSucessTimer();
//                }else {
//                    startPreview();
//                }
//            }
//        });
//        originalTimer= new Timer();
//        originalTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.sendEmptyMessage(STATE_PROCESS);
//            }
//        },0,1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.media_del:
                showDialog();
                break;
        }
    }

    /**
     * 展示dialog
     */
    private void showDialog(){
        final DialogManager dialogManager = new DialogManager(this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(this).setText("确认要终止合成视频吗?"))
                .setView(new HButtonView(this)
                        .setNegativeText("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                XHClick.mapStat(MediaHandleActivity.this,"a_video_splice","终止合成","取消");
                            }
                        })
                        .setPositiveText("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                XHClick.mapStat(MediaHandleActivity.this,"a_video_splice","终止合成","确定");
                                MediaHandleActivity.this.finish();
                            }
                        }))).show();
    }
    @Override
    protected void onDestroy() {
        Log.i("mediahandler","onDestroy");
        cancelOriTime();
        cancelSucessTimer();
        super.onDestroy();
    }

    @Override
    public void finish() {
        Log.i("mediahandler","finish");
        cancelOriTime();
        cancelSucessTimer();
        super.finish();
    }

    @Override
    public void onBackPressed() {
//        mediaHandlerContorl.setStop(true);
//        MediaHandleControl.delAllMediaHandlerData(uploadDishData.getId());
        showDialog();
//        super.onBackPressed();
    }

    private void cancelOriTime(){
        if(originalTimer!=null)originalTimer.cancel();
        originalTimer=null;
    }
    private void startSucessTimer(){
        sucessTimer= new Timer();
        sucessTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(STATE_PROCESS);
            }
        },0,100);
    }
    private void cancelSucessTimer(){
        if(sucessTimer!=null)sucessTimer.cancel();
        sucessTimer=null;
    }

    /**
     * 开始预览页面
     */
    private void startPreview(){
        Log.i("zhangyujian","合成成功:::"+path_finish+":::"+id_data+"::::"+time_key+"::::");
//        if(!TextUtils.isEmpty(path_finish)&&id_data>-1) {
//            Intent intent = new Intent(MediaHandleActivity.this, MediaPreviewActivity.class);
//            intent.putExtra("path", path_finish);
//            intent.putExtra("id", id_data);
//            intent.putExtra("time",time_key);
//            intent.putExtra("coverPath",coverPath);
//            MediaHandleActivity.this.startActivity(intent);
//            MediaHandleActivity.this.finish();
//        }
    }
}
