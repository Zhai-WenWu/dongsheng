package amodule.user.view;

import acore.override.XHApplication;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xianghatest.R;
/**
 * 文本编辑控件
 * @author FangRuijiao
 */
public class TextEditView extends LinearLayout{

	private Context mCon;
	private TextView mEditText;
	//用EditText显示信息，当超过规定字数时不显示省略号，这版不做编辑功能，故先注释
//	private View mEditIv;
	
	private String mUrl,mParams,mKey ,mValue;
	private TextEditCallback mFollowCallback;
	
	
	public TextEditView(Context context) {
		super(context);
		mCon = context;
		init();
	}
	
	public TextEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCon = context;
		init();
	}
	
	/**
	 * 初始化UI
	 */
	private void init(){
		LayoutInflater inflater = LayoutInflater.from(mCon);
		View view = inflater.inflate(R.layout.view_text_edit, null);
		this.addView(view);
//		final LinearLayout.LayoutParams clickParams = new LinearLayout.LayoutParams(ToolsDevice.dp2px(mCon, 200), LinearLayout.LayoutParams.WRAP_CONTENT);
		mEditText = (TextView)view.findViewById(R.id.view_text_edit_tv);
//		mEditIv = view.findViewById(R.id.view_text_edit_iv);
//		mEditIv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mEditText.setEnabled(true);
//				mEditText.setBackgroundResource(R.drawable.bg_round_ea5440);
//				mEditText.setLayoutParams(clickParams);
//				ToolsDevice.keyboardControl(true, mCon, mEditText);
//			}
//		});;
	}
	
	/**
	 * 当取消保存时调用
	 */
	public void setEditCancel(){
		LinearLayout.LayoutParams normalParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mEditText.setBackground(null);
		mEditText.setEnabled(false);
		mEditText.setLayoutParams(normalParams);
//		mEditIv.setVisibility(View.GONE);
	}
	
	/**
	 * 当滑动时调用
	 */
	public void setEditOk(){
		setEditCancel();
		onUpload();
	}
	
	private void onUpload() {
		ReqInternet.in().doPost(mUrl,mParams + "&" + mKey + "=" + mValue,
				new InternetCallback(XHApplication.in()) {
					@Override
					public void loaded(int flag, String url, Object returnObj) {
						if(mFollowCallback != null)
							mFollowCallback.onCallback(flag, url, returnObj);
					}
				});
	}
	/**
	 * 获取编辑控件是否有焦点
	 * @return
	 */
	public boolean editHasFocus(){
		return mEditText.hasFocus();
	}
	
	/**
	 * 设置数据
	 * @param url : 点击后的请求url
	 * @param params : 除了关注的key和value其他的参数
	 * @param key ： 请求服务端时需要传的key,为空则表示不可以编辑
	 * @param value ： 当前状态
	 */
	public void setData(String url,String params,String key,String value,String hintText){
		mUrl = url;
		mParams = params;
		mKey = key;
		mValue = value;
		mEditText.setHint(hintText);
		mEditText.setText(mValue);
//		if(!TextUtils.isEmpty(mValue) && !TextUtils.isEmpty(mKey)){
//			mEditIv.setVisibility(View.VISIBLE);
//		}
	}
	
	/**
	 * 成功生效后回调
	 */
	public void setCallback(TextEditCallback callback){
		mFollowCallback = callback;
	}
	
	public interface TextEditCallback{
		/**
		 * 设置后的返回回调
		 * @param flag
		 * @param url
		 * @param returnObj
		 */
		public void onCallback(int flag, String url, Object returnObj);
	}

}
