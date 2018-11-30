/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.audiomix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.xiangha.R;

import java.util.ArrayList;

import third.aliyun.edit.effects.control.BaseChooser;
import third.aliyun.edit.effects.control.EffectInfo;
import third.aliyun.edit.effects.control.OnItemClickListener;
import third.aliyun.edit.effects.control.UIEditorPage;
import third.aliyun.edit.util.MusicBean;
import third.aliyun.edit.util.MusicSQL;


public class AudioMixChooserMediator extends BaseChooser implements OnItemClickListener, OnClickListener {
    private static final String MUSIC_WEIGHT = "music_weight";
    private static final String MUSIC_WEIGHT_KEY = "music_weight_key";

    private RecyclerView mOnlineMusicRecyclerView;
    private SeekBar mMusicWeightSeekBar;
    private EffectInfo mMusicWeightInfo = new EffectInfo();
    private OnlineAudioMixNewAdapter mOnlineAudioMixAdapter;
    private ArrayList<MusicBean> mMusicList = new ArrayList<>();

    public static AudioMixChooserMediator newInstance() {
        AudioMixChooserMediator dialog = new AudioMixChooserMediator();
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
//        initResourceOnLine();
        requestMusic();
    }
    private int getMusicWeight(){
        return getContext().getSharedPreferences(MUSIC_WEIGHT, Context.MODE_PRIVATE).getInt(MUSIC_WEIGHT_KEY,50);
    }
    private void saveMusicWeight(){
        Context context = getContext();
        if(context != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(MUSIC_WEIGHT, Context.MODE_PRIVATE).edit();
            int weight = mMusicWeightSeekBar.getProgress();
            editor.putInt(MUSIC_WEIGHT_KEY, weight);
            editor.commit();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        saveMusicWeight();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.work_aliyun_svideo_music_view, container);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMusicWeightSeekBar = (SeekBar) view.findViewById(R.id.music_weight);
        mMusicWeightSeekBar.setMax(100);
        int musicWeight = getMusicWeight();
        mMusicWeightSeekBar.setProgress(musicWeight);
        mMusicWeightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mOnEffectChangeListener != null){
                    mMusicWeightInfo.isAudioMixBar = true;
                    mMusicWeightInfo.type = UIEditorPage.AUDIO_MIX;
                    mMusicWeightInfo.musicWeight = seekBar.getMax() - i;
                    mOnEffectChangeListener.onEffectChange(mMusicWeightInfo);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mOnlineMusicRecyclerView = (RecyclerView) view.findViewById(R.id.recylerView);
        mOnlineMusicRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        mOnlineAudioMixAdapter = new OnlineAudioMixNewAdapter(getActivity(), mMusicList);
        mOnlineAudioMixAdapter.setOnItemClickListener(this);
        mOnlineMusicRecyclerView.setAdapter(mOnlineAudioMixAdapter);
        mOnlineMusicRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if(mEditorService != null && mEditorService.isFullScreen()) {
//            mOnlineMusicRecyclerView.setBackgroundColor(Color.parseColor("#dd171717"));
        }
    }
    private void requestMusic(){
        ArrayList<MusicBean> musicBeanArrayList=MusicSQL.getInstance().queryAllShowData();
        if(musicBeanArrayList != null){
            MusicBean empty = new MusicBean();
            mMusicList.clear();
            mMusicList.add(0,empty);
            mMusicList.addAll(musicBeanArrayList);
        }
            mOnlineAudioMixAdapter.setData(mMusicList);
            int index = getLastSelectIndex(mEditorService.getEffectIndex(UIEditorPage.AUDIO_MIX),mMusicList);
            mOnlineAudioMixAdapter.setSelectedIndex(index);

    }
    private int getLastSelectIndex(int id,ArrayList<MusicBean> mMusicList){
        int index = 0;
        if(mMusicList == null){
            return index;
        }
        for(MusicBean musicForm : mMusicList){
            if(musicForm.getId() ==  id){
                break;
            }
            index++;
        }
        return index;
    }

    @Override
    public boolean onItemClick(EffectInfo effectInfo, int index) {
        if (mOnEffectChangeListener != null) {
            effectInfo.musicWeight = mMusicWeightSeekBar.getMax() - mMusicWeightSeekBar.getProgress();
            mOnEffectChangeListener.onEffectChange(effectInfo);
        }
//        mOnlineAudioMixAdapter.clearSelect();
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX,effectInfo.id);
        return true;
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
//        if(id == R.id.voice_btn) {
//            AliyunIPlayer mPlayer = ((EditorActivity)getActivity()).getPlayer();
//            if(mPlayer != null) {
//                boolean isAudioSilence = mPlayer.isAudioSilence();
//                mPlayer.setAudioSilence(!isAudioSilence);
//                mVoiceBtn.setSelected(!isAudioSilence);
//            }
//        }
    }

}
