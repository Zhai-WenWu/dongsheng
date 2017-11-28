package amodule.user.view;

import acore.logic.LoginManager;
import acore.override.XHApplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

/**
 * 关注控件
 * @author FangRuijiao
 */
public class FollowView extends LinearLayout{
	
	private Context mCon;
	private TextView mFollowTv;
	//-----------样式------------
	public String M_FOLLOW_TEXT = "已关注",M_NOT_FOLLOW_TEXT="关注";
	public float M_TEXT_SIZE = 12;
	public int M_FOLLOW_BACK_RESOUSE = R.drawable.bg_round2_999999;
	public int M_NOT_FOLLOW_BACK_RESOUSE = R.drawable.bg_round_white8;
	public int M_Follow_TEXT_COLOR = Color.parseColor("#999999");
	public int M_NOT_Follow_TEXT_COLOR = Color.parseColor("#f23030");
	//----------显示，关注，取消关注-------------
	/**	不显示关注按钮 */
	public String FOLLOW_GONE = "1";
	/**	关注 */
	public String FOLLOW = "2";
	/**	未关注 */
	public String FOLLOW_NOT = "3";
	//---------关注人时的数据-----------
	private String mUrl,mParams,mKey,mFollowState;
	//----------回调---------
	private FollowCallback mFollowCallback;

	public FollowView(Context context) {
		super(context);
		mCon = context;
		init();
	}
	
	public FollowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCon = context;
		init();
	}
	
	/**
	 * 初始化UI
	 */
	private void init(){
		LayoutInflater mInflater = LayoutInflater.from(mCon);
		View view = mInflater.inflate(R.layout.view_follow, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		this.addView(view);
		mFollowTv = (TextView) view.findViewById(R.id.view_follow_tv);
		mFollowTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!LoginManager.isLogin()) {
					Intent intent = new Intent(mCon, LoginByAccout.class);
					mCon.startActivity(intent);
					return;
				}
				onAttentionClick();
				if (mFollowState.equals(FOLLOW_NOT))
					mFollowState = FOLLOW;
				else if (mFollowState.equals(FOLLOW))
					mFollowState = FOLLOW_NOT;
				paresFolState(mFollowState);
			}
		});
	}
	
	/**
	 * 设置数据
	 * @param url : 点击后的请求url
	 * @param params : 除了关注的key和value其他的参数
	 * @param key ： 请求服务端时需要传的key
	 * @param value ： 当前状态
	 */
	public void setData(String url,String params,String key,String value){
		mUrl = url;	
		mParams = params;
		mKey = key;
		mFollowState = value;
		if(M_TEXT_SIZE != 12)
			mFollowTv.setTextSize(M_TEXT_SIZE);
		paresFolState(value);
	}
	
	private void paresFolState(String folState){
		if (folState.equals(FOLLOW)) {
			this.setVisibility(View.VISIBLE);
			mFollowTv.setText(M_FOLLOW_TEXT);
			mFollowTv.setTextColor(M_Follow_TEXT_COLOR);
			mFollowTv.setBackgroundResource(M_FOLLOW_BACK_RESOUSE);
		} else if (folState.equals(FOLLOW_NOT)) {
			this.setVisibility(View.VISIBLE);
			mFollowTv.setText(M_NOT_FOLLOW_TEXT);
			mFollowTv.setTextColor(M_NOT_Follow_TEXT_COLOR);
			mFollowTv.setBackgroundResource(M_NOT_FOLLOW_BACK_RESOUSE);
		}else{
			this.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 关注请求
	 * @param code
	 */
	public void onAttentionClick() {
		ReqInternet.in().doPost(mUrl,mParams + "&" + mKey + "=" + mFollowState,
				new InternetCallback(XHApplication.in()) {
					@Override
					public void loaded(int flag, String url, Object returnObj) {
						if(mFollowCallback != null)
							mFollowCallback.onCallback(flag, url, returnObj);
					}
				});
	}
	
	/**
	 * 成功生效后回调
	 */
	public void setCallback(FollowCallback callback){
		mFollowCallback = callback;
	}
	
	public interface FollowCallback{
		/**
		 * 设置关注、取消关注后的返回回调
		 * @param flag
		 * @param url
		 * @param returnObj
		 */
		public void onCallback(int flag, String url, Object returnObj);
	}
}
