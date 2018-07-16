package third.aliyun.edit.effects.audiomix;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.io.File;

import acore.override.view.BaseView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnItemClickListener;
import third.aliyun.edit.effects.control.UIEditorPage;
import third.aliyun.edit.util.Common;
import third.aliyun.edit.util.MusicBean;
import third.aliyun.edit.util.MusicSQL;
import xh.basic.internet.FileDownloadCallback;
import xh.basic.internet.progress.UtilInternetFile;

/**
 * item view
 */

public class AudioItemView extends BaseView implements View.OnClickListener{
    private TextView musicName,music_state;
    private ImageView selectFlag;
    private ImageView music_select_img;//音乐图标
    private int position=-1;
    private MusicBean musicBeans;
    private int selectedIndex=-1;
    private Animation anim;//旋转动画
    private ImageView progressBar;
    public AudioItemView(Context context) {
        super(context, R.layout.work_aliyun_svideo_music_item_view);
        initView();
    }

    public AudioItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.work_aliyun_svideo_music_item_view);
        initView();
    }

    public AudioItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.work_aliyun_svideo_music_item_view);
        initView();
    }
    private void initView(){
        music_select_img= (ImageView) findViewById(R.id.music_select_img);
        musicName = (TextView) findViewById(R.id.music_name);
        selectFlag = (ImageView) findViewById(R.id.selected_flag);
        music_state= (TextView) findViewById(R.id.music_state);
        progressBar= (ImageView) findViewById(R.id.progressBar);
        anim = AnimationUtils.loadAnimation(getContext(), R.anim.feekback_progress_anim);
    }
    public void setdata(int position, MusicBean musicBean, int selectedIndex){
        Log.i("xianghaTag","selectedIndex:::"+selectedIndex);
        this.position= position;
        this.musicBeans = musicBean;
        this.selectedIndex=selectedIndex;
        this.setOnClickListener(this);
        //处理音乐名称
        if(musicBeans.getName() == null && musicBeans.getUrl() == null){
            musicName.setText(R.string.no_music);
        }else{
            musicName.setText(musicBeans.getName());
        }
        //是否下载
        if(musicBeans.isDownLoadState()&&position!=0){
            music_state.setVisibility(View.VISIBLE);
        }else{
            music_state.setVisibility(View.GONE);
        }
        isProgressDownLoad(!TextUtils.isEmpty(musicBean.getIsDownLoad())&&"1".equals(musicBean.getIsDownLoad()));
        if(position!=0){
            if(position == selectedIndex){
                selectFlag.setVisibility(View.VISIBLE);
                music_state.setVisibility(View.GONE);
                music_select_img.setImageResource(R.drawable.aliyun_svideo_music_item_select);
                musicName.setTextColor(Color.parseColor("#ffda44"));
            }else{
                selectFlag.setVisibility(View.GONE);
                musicName.setTextColor(Color.parseColor("#a0a0a0"));
                music_select_img.setImageResource(R.drawable.aliyun_svideo_music_item_normal);
            }
        }else{
            if(position == selectedIndex){
                selectFlag.setVisibility(View.VISIBLE);
                music_state.setVisibility(View.GONE);
                music_select_img.setImageResource(R.drawable.aliyun_svideo_music_item_select_no);
                musicName.setTextColor(Color.parseColor("#ffda44"));
            }else{
                selectFlag.setVisibility(View.GONE);
                musicName.setTextColor(Color.parseColor("#a0a0a0"));
                music_select_img.setImageResource(R.drawable.aliyun_svideo_music_item_normal_no);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if(musicBeans!=null&&!TextUtils.isEmpty(musicBeans.getName()) && !TextUtils.isEmpty(musicBeans.getUrl())) {
            if (!musicBeans.isDownLoadState()) {
                downloadMusic(musicBeans,position);
            } else {
                if(!TextUtils.isEmpty(musicBeans.getLocationUrl())){
                    File file = new File(musicBeans.getLocationUrl());
                    if (file.exists()) {
                        if (mItemClick != null) {
                            EffectInfo info = new EffectInfo();
                            info.type = UIEditorPage.AUDIO_MIX;
                            info.setPath(musicBeans.getLocationUrl());
                            info.id = musicBeans.getId();
                            mItemClick.onItemClick(info, position);
                        }
                    } else {
                        downloadMusic(musicBeans,position);
                    }
                }else{
                    downloadMusic(musicBeans,position);
                }
            }
        }else{
            if (mItemClick != null) {
                EffectInfo info = new EffectInfo();
                info.type = UIEditorPage.AUDIO_MIX;
                info.setPath(null);
                mItemClick.onItemClick(info, position);
            }
        }
    }
    private void downloadMusic(MusicBean musicBean, final int position){
        String filePath= Common.QU_DIR+"/"+musicBean.getCode();
        if(audioItemClick!=null){
            audioItemClick.audioClick(position);
        }
        musicBean.setIsDownLoad("1");//加载中
        isProgressDownLoad(true);
        UtilInternetFile.in().downloadFileProgress(musicBean.getUrl(), filePath, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                Log.i("xianghaTag","url:::;"+url+"::::flag:::"+flag);
                if(flag>= ReqInternet.REQ_OK_IS){
                    musicBean.setLocationUrl(filePath);
                    musicBean.setIsDownLoad("2");
                    Log.i("xianghaTag",musicBean.getName()+":::musicBeans:::;"+musicBean.getLocationUrl()+"::::download:::"+musicBean.getIsDownLoad());
                    MusicSQL.getInstance().updateMusicBean(musicBean,true);
                    EffectInfo info = new EffectInfo();
                    info.type = UIEditorPage.AUDIO_MIX;
                    info.setPath(filePath);
                    info.id = musicBean.getId();
                    music_state.setVisibility(View.VISIBLE);
                    if(audioItemClick!=null){
                        audioItemClick.audioDownLoadClick(info,position);
                    }
                }else{
                    musicBean.setIsDownLoad("0");
                }
                isProgressDownLoad(false);
            }
        }, new FileDownloadCallback() {
            @Override
            public void onProgress(long l, long l1, boolean b) {
//                Log.i("xianghaTag","l::"+l+"::l1:::"+l1+":::"+b);
            }
        });
    }
    private OnItemClickListener mItemClick;
    public void setItemClick(OnItemClickListener onItemClickListener){
        this.mItemClick= onItemClickListener;
    }
    private void isProgressDownLoad(boolean state){
        if(state){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(anim);
            this.setEnabled(false);
        }else{
            progressBar.setVisibility(View.GONE);
            progressBar.clearAnimation();
            this.setEnabled(true);
        }
    }
    private OnlineAudioMixNewAdapter.AudioItemClick audioItemClick;
    public void setAudioItemClick(OnlineAudioMixNewAdapter.AudioItemClick audioItemClick){
     this.audioItemClick= audioItemClick;
    }
}
