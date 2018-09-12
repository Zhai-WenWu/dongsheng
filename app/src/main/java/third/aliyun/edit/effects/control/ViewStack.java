/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.control;

import android.content.Context;
import android.content.Intent;

import acore.logic.XHClick;
import acore.override.XHApplication;
import third.aliyun.edit.effects.audiomix.AudioMixChooserMediator;
import third.aliyun.edit.effects.coverImg.CoverImgMediator;
import third.aliyun.edit.effects.filter.FilterChooserMediator;
import third.aliyun.work.EditorActivity;


public class ViewStack {

    private final Context mContext;
    private EditorService mEditorService;
    private OnEffectChangeListener mOnEffectChangeListener;
    private BottomAnimation mBottomAnimation;

    private FilterChooserMediator mFilterChooserMediator;
    private AudioMixChooserMediator mAudioMixChooserMediator;
    private CoverImgMediator coverImgMediator;

    public ViewStack(Context context) {
        mContext = context;
    }

    public void setActiveIndex(int value) {

        UIEditorPage index = UIEditorPage.get(value);
        switch (index) {
            case FILTER_EFFECT:
                XHClick.onEvent(XHApplication.in(),"a_shoot_handle","滤镜");
                mFilterChooserMediator = FilterChooserMediator.newInstance();
                mFilterChooserMediator.setmEditorService(mEditorService);
                mFilterChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                mFilterChooserMediator.setBottomAnimation(mBottomAnimation);
                mFilterChooserMediator.show(((EditorActivity) mContext).getSupportFragmentManager(), "filter");
                break;
            case AUDIO_MIX:
                XHClick.onEvent(XHApplication.in(),"a_shoot_handle","音乐");
                mAudioMixChooserMediator = AudioMixChooserMediator.newInstance();
                mAudioMixChooserMediator.setBottomAnimation(mBottomAnimation);
                mAudioMixChooserMediator.setmEditorService(mEditorService);
                mAudioMixChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                mAudioMixChooserMediator.show(((EditorActivity) mContext).getSupportFragmentManager(), "audioMix");
                break;
            case COVER_IMG:
                XHClick.onEvent(XHApplication.in(),"a_shoot_handle","封面");
                coverImgMediator= CoverImgMediator.newInstance();
                coverImgMediator.setVideoPath(videoPath);
                coverImgMediator.setVideoCallBack(videoCallBack);
                coverImgMediator.setVideoDurtion(durtion);
                coverImgMediator.setBottomAnimation(mBottomAnimation);
                coverImgMediator.setmEditorService(mEditorService);
                coverImgMediator.show(((EditorActivity) mContext).getSupportFragmentManager(), "cover");
                videoCallBack.zoomLayoutParams();
                break;
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        }
    }

    public void setEditorService(EditorService editorService) {
        mEditorService = editorService;
    }

    public void setEffectChange(OnEffectChangeListener onEffectChangeListener) {
        mOnEffectChangeListener = onEffectChangeListener;
    }

    public void setBottomAnimation(BottomAnimation bottomAnimation) {
        mBottomAnimation = bottomAnimation;
    }
    private String videoPath;
    public void setVideopath(String videopath){
        this.videoPath= videopath;
    }
    private long durtion;
    public void setVideoDurtion(long durtion){
        this.durtion= durtion;
    }
    private VideoCallBack videoCallBack;
    public void setVideoCallBack(VideoCallBack videoCallBack){
        this.videoCallBack = videoCallBack;
    }

}
