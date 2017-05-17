package amodule.user.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.LoginManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
/**
 * 评论点赞控件
 * @author FangRuijiao
 */
public class CommonLikeView extends LinearLayout{

	private Context mCon;
	
	private String mUrl,mParams,mKey,mValue;
	private int mLikeNum;
	
	private ImageView mLikeIv;
	private TextView mCommonTv,mLikeTv;
	
	private CommonLikeCallback mCallback;
	
	public CommonLikeView(Context context) {
		super(context);
		mCon = context;
		init();
	}
	
	public CommonLikeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCon = context;
		init();
	}
	
	/**
	 * 初始化UI
	 */
	private void init(){
		View view = LayoutInflater.from(mCon).inflate(R.layout.view_common_like, null);
		addView(view,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mCommonTv = (TextView)view.findViewById(R.id.view_common_tv);
		mLikeIv = (ImageView)view.findViewById(R.id.view_like_iv);
		mLikeTv = (TextView)view.findViewById(R.id.view_like_tv);
		mLikeIv.setOnClickListener(onLikeClick);
		mLikeTv.setOnClickListener(onLikeClick);
		findViewById(R.id.view_common_iv).setOnClickListener(onCommonClick);
		mCommonTv.setOnClickListener(onCommonClick);
	}
	
	private OnClickListener onLikeClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!LoginManager.isLogin()) {
				//登录
				Intent intent = new Intent(mCon,LoginByAccout.class);
				mCon.startActivity(intent);
				return;
			}
			parseZanClick();
		}
	};
	/**
	 * 评论点击事件
	 */
	private OnClickListener onCommonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mCallback.onClickCommon();
		}
	};
	
	/**
	 * 点赞事件处理
	 * @param map
	 */
	private void parseZanClick() {
		if (!"2".equals(mValue)) {
			mLikeNum ++;
			mValue = "2";
			parseZanIcon(mValue);
			setLikeNum();
			// 请求网络;
			ReqInternet.in().doPost(mUrl, mParams + "&" + mKey + "=" + mValue, 
					new InternetCallback(mCon) {
						@Override
						public void loaded(int flag, String url,Object returnObj) {
							if(mCallback != null)
								mCallback.onCallback(flag >= ReqInternet.REQ_OK_STRING);
						}
					});
		}
		else{
			Tools.showToast(mCon, "您已经赞过了，谢谢！");
		}
	}
	
	/**
	 * 根据标示,设置赞的图标
	 * @param like
	 */
	private void parseZanIcon(String like) {
		if ("2".equals(like)) {
			mLikeIv.setImageResource(R.drawable.z_quan_home_body_ico_good_active);
		} else {
			mLikeIv.setImageResource(R.drawable.z_quan_home_body_ico_good);
		}
	}
	
	/**
	 * * 设置数据
	 * @param url : 点击后的请求url
	 * @param params : 除了关注的key和value其他的参数
	 * @param LikeKey ： 点赞时请求服务端时需要传的key
	 * @param value ： 当前状态
	 * @param commonNum : 评论数量
	 * @param likeNum ： 点赞数量
	 */
	public void initData(String url,String params,String LikeKey,String value,String commonNum,String likeNum,OnClickListener listener){
		mUrl = url;
		mParams = params;
		mKey = LikeKey;
		mValue = value;
		if(TextUtils.isEmpty(likeNum))
			mLikeNum = 0;
		else
			mLikeNum = Integer.parseInt(likeNum);
		parseZanIcon(value);
		setLikeNum();
		if(TextUtils.isEmpty(commonNum))
			setCommonNum("0");
		else
			setCommonNum(commonNum);
		if(listener != null){
			mLikeIv.setOnClickListener(listener);
			mLikeTv.setOnClickListener(listener);
		}else{
			mLikeIv.setOnClickListener(onLikeClick);
			mLikeTv.setOnClickListener(onLikeClick);
		}
	}
	
	private void setLikeNum(){
		if(mLikeNum > 0)
			mLikeTv.setVisibility(View.VISIBLE);
		else
			mLikeTv.setVisibility(View.GONE);
		mLikeTv.setText(String.valueOf(mLikeNum));
	}
	private void setCommonNum(String commonNum){
		if(Integer.parseInt(commonNum) > 0)
			mCommonTv.setVisibility(View.VISIBLE);
		else
			mCommonTv.setVisibility(View.GONE);
		mCommonTv.setText(commonNum);
	}
	
	/**
	 * 成功生效后回调
	 */
	public void setCallback(CommonLikeCallback callback){
		mCallback = callback;
	}
	
	public interface CommonLikeCallback{
		/**
		 * 设置后的返回回调
		 * @param isOk
		 */
		public void onCallback(boolean isOk);
		
		/*** 点击评论回调 */
		public void onClickCommon();
	}

}
