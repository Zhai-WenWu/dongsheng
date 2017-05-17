package amodule.dish.video.control;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.util.ArrayList;

import acore.logic.LoginManager;
import amodule.dish.video.bean.SpeechBean;

/**
 * 语音合成
 */
public class SpeechHandlerControl {
    private static String TAG = "zhangyujian";
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 引擎类型---当前使用在线合成（云端）
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 默认发音人
    private String voicer = "xiaoyan";
    private ArrayList<SpeechBean> listBeans;//数据集合
    private int index=0;
    public SpeechHandlerControl(ArrayList<SpeechBean> lists,Context context){
        this.listBeans = lists;
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
    }

    /**
     * 开始合成
     */
    private void startHandler(){
        setParam();
       int code= mTts.synthesizeToUri(listBeans.get(index).getText(),listBeans.get(index).getPath(),mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                //未安装则跳转到提示安装页面
                Log.d(TAG, "未安装则跳转到提示安装页面");
            }else {
                Log.d(TAG, "语音合成失败,错误码: "+code);
            }
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败,错误码："+code);
                Log.d(TAG, "语音合成失败,错误码: "+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
                Log.d(TAG, "初始化成功 ");
                startHandler();
            }
        }
    };

    /**
     * 参数设置
     * @return
     */
    private void setParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            if(LoginManager.getSex().equals("男")){                                               //男性
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaofeng");
                //设置合成语速
                mTts.setParameter(SpeechConstant.SPEED,  "50");
//                mTts.setParameter(SpeechConstant.SAMPLE_RATE,String.valueOf(44100));
                //设置合成音调
                mTts.setParameter(SpeechConstant.PITCH,  "49");
                //设置合成音量
                mTts.setParameter(SpeechConstant.VOLUME,  "100");
            }else{                                                  //中性和女性
                // 设置在线合成发音人
                mTts.setParameter(SpeechConstant.VOICE_NAME, "aisjinger");
                //设置合成语速
                mTts.setParameter(SpeechConstant.SPEED,  "48");
//                mTts.setParameter(SpeechConstant.SAMPLE_RATE,String.valueOf(44100));
                //设置合成音调
                mTts.setParameter(SpeechConstant.PITCH,  "45");
                //设置合成音量
                mTts.setParameter(SpeechConstant.VOLUME,  "100");
            }

        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
//        mTts.setParameter(SpeechConstant.STREAM_TYPE,  "3");
        // 设置播放合成音频打断音乐播放，默认为true
//        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, listBeans.get(index).getPath());
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.d(TAG, "开始播放 ");
        }
        @Override
        public void onSpeakPaused() {
            Log.d(TAG, "暂停播放 ");
        }
        @Override
        public void onSpeakResumed() {
            Log.d(TAG, "继续播放 ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,String info) {
            // 合成进度
            Log.d(TAG, "合成进度 "+percent);
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            Log.d(TAG, "合成成功：： "+error);
            if(error==null){
                listBeans.get(index).setSuccess(true);
                index++;
                Log.d(TAG, "index: "+index);
                if(index<=listBeans.size()-1){
                    startHandler();
                }
            }
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

}
