package third.speech;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 讯飞语音集成
 * @author FangRuijiao
 * @date ：2016-2-26
 */
public class SpeechIflytek {
	
//	private final String TAG = "FRJ";
	
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mDialog;
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private Toast mToast;
	private OnSpeechIflytekListener mSpeechIflytekListener;
	
	/**
	 * 开始录入音频后，音频前面部分最长静音时长。
	 */
	public int mSpeech_VAD_BOS = 2000;
	/**
	 * 开始录入音频后，音频后面部分最长静音时长
	 */
	public int mSpeech_VAD_EOS = 2000;
	/**
	 * 网络连接超时时间值范围：[0, 30000]
	 */
	public int mSpeech_NET_TIMEOUT = 200000;
	/**
	 * 是否加标点
	 */
	public boolean mSpeech_ASR_PTT = true;
	
	
	public SpeechIflytek(Context con,OnSpeechIflytekListener speechIflytekListener){
		mSpeechIflytekListener = speechIflytekListener;
		//1.创建SpeechRecognizer对象，第二个参数：本地识别时传InitListener
		mIat= SpeechRecognizer.createRecognizer(con, null);
		//1.创建RecognizerDialog对象
		mDialog = new RecognizerDialog(con, mInitListener);
		mToast = Toast.makeText(con, "", Toast.LENGTH_SHORT);
	}
	
	public void starSpeech(){
		mIatResults.clear();
		setParam();
		//3.设置回调接口
		mDialog.setListener(mRecognizerDialogListener);
		//4.显示dialog，接收语音输入
		try{
			mDialog.show();
			showTip("请开始说话…");
		}catch (Exception e){

		}
		//3.开始听写
//		mIat.startListening(mRecoListener);
	}
	
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
//			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};
	
	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
//			Log.i(TAG,"结果出来啦。。。:" + isLast);
			printResult(results,isLast);
		}
		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
//			Log.i(TAG,"出错啦。。。");
			mSpeechIflytekListener.onError(error.getPlainDescription(true));
			showTip(error.getPlainDescription(true));
		}

	};
	
	private void printResult(RecognizerResult results,boolean isLast) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);
		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}
		mSpeechIflytekListener.onResult(resultBuffer.toString(),isLast);
	}
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
	//听写监听器
	private RecognizerListener mRecoListener = new RecognizerListener(){
		//听写结果回调接口(返回Json格式结果，用户可参见附录13.1)；
		//一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
		//关于解析Json的代码可参见Demo中JsonParser类；
		//isLast等于true时会话结束。
		public void onResult(RecognizerResult results, boolean isLast) {
//			Log.d(TAG,"出结果啦");
			printResult(results,isLast);
		}
		//会话发生错误回调接口
		public void onError(SpeechError error) {
//			Log.d(TAG,"会话发生错误回调接口");
			//获取错误码描述
			showTip(error.getPlainDescription(true));
		}
		//开始录音
		public void onBeginOfSpeech() {
//			Log.d(TAG,"开始录音");
		}
		//volume音量值0~30，data音频数据
		public void onVolumeChanged(int volume, byte[] data){
//			Log.d(TAG,"volume音量值0~30，data音频数据");
		}
		//结束录音
		public void onEndOfSpeech() {
//			Log.d(TAG,"结束录音");
		}
		//扩展用接口
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
//			Log.d(TAG,"扩展用接口");
		}
	};
	
	/**
	 * 参数设置 详见《MSC Reference Manual》SpeechConstant类
	 * @return
	 */
	private void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
		//网络连接超时时间值范围：[0, 30000]
		mIat.setParameter(SpeechConstant.NET_TIMEOUT,String.valueOf(mSpeech_NET_TIMEOUT));
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, String.valueOf(mSpeech_VAD_BOS));
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, String.valueOf(mSpeech_VAD_EOS));
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		if(mSpeech_ASR_PTT)
			mIat.setParameter(SpeechConstant.ASR_PTT,  "1");
		else
			mIat.setParameter(SpeechConstant.ASR_PTT,  "0");
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}
	
	public void onCancleSpeech(){
		mIat.cancel();
		mDialog.cancel();
	}
	
	public void onDestroy(){
		// 退出时释放连接
		mIat.cancel();
		mIat.destroy();
	}
	
	public interface OnSpeechIflytekListener{
		public void onResult(String result,boolean isLast);
		public void onError(String errorInfo);
	}
}
