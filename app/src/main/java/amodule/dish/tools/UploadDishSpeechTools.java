package amodule.dish.tools;

import android.content.Context;
import android.widget.EditText;

import com.xiangha.R;

import third.speech.SpeechIflytek;
import third.speech.SpeechIflytek.OnSpeechIflytekListener;

public class UploadDishSpeechTools {
	private static UploadDishSpeechTools speechTools;
	private SpeechIflytek speechIflytek;
	
//	private TextView mStartBtn;
	private EditText mCurrentTv;
	private String mCurrentStr;
	private String start,end;
	private int index = 0;
	
	public static UploadDishSpeechTools createUploadDishSpeechTools(){
		if(speechTools == null){
			speechTools = new UploadDishSpeechTools();
		}
		return speechTools;
	}
	
	public void initSpeech(Context con) {
		speechIflytek = new SpeechIflytek(con, new OnSpeechIflytekListener() {
			
			@Override
			public void onResult(String result,boolean isLast) {
				String text = start + result + end;
				mCurrentTv.setText(text);
				Object obj = mCurrentTv.getTag(R.id.dish_upload_number);
				int maxLength = -1;
				if(obj != null){
					maxLength = Integer.parseInt(String.valueOf(obj));
				}
				if(maxLength > 1){
					if((start + result).length() < maxLength)
						mCurrentTv.setSelection((start + result).length());
					else
						speechIflytek.onCancleSpeech();
				}else if((start + result).length() < mCurrentTv.getText().length())
					mCurrentTv.setSelection((start + result).length());
				if(isLast){
					start += result;
				}
			}
			@Override
			public void onError(String errorInfo) {}
		});
		speechIflytek.mSpeech_VAD_EOS = 10000;
	}
	
//	public void setStartButton(TextView startBtn){
//		mStartBtn = startBtn;
//		mStartBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				XHClick.mapStat(XHApplication.in(), UploadDishActivity.STATISTICS_ID, "语音", "点击键盘上的语音按钮");
//				startSpeech();
//			}
//		});
//	}
	
	public void startSpeech(EditText view){
		mCurrentTv = view;
		mCurrentStr = String.valueOf(mCurrentTv.getText());
		index = mCurrentTv.getSelectionStart();
		start = mCurrentStr.substring(0, index);
		end = mCurrentStr.substring(index,mCurrentStr.length());
		speechIflytek.starSpeech();
	}
	
//	public void setCurrentView(EditText view){
//		mCurrentTv = view;
//		mCurrentStr = String.valueOf(mCurrentTv.getText());
//		index = mCurrentTv.getSelectionStart();
//		if(TextUtils.isEmpty(mCurrentStr))
//			((View) mStartBtn.getParent()).setVisibility(View.VISIBLE);
//		else
//			((View) mStartBtn.getParent()).setVisibility(View.GONE);
//	}
	
	public void onDestroy() {
		if (speechIflytek != null)
			speechIflytek.onDestroy();
	}
}
