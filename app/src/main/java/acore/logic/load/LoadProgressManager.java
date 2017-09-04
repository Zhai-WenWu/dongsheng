package acore.logic.load;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;
import acore.tools.ToolsDevice;

/**
 * progress的管理类
 * @author Eva
 *
 */
public class LoadProgressManager {
	private LinearLayout mLoadFailLayout;
	private RelativeLayout mProgressBar;
	private View mProgressShadow;
	private TextView mLoadFailBtn;
	private int topHeight;
	
	public LoadProgressManager(Context context , RelativeLayout layout){
		topHeight = Tools.getStatusBarHeight(context) + Tools.getDimen(context,R.dimen.dp_45);
		initProgress(context , layout);
		initLoadFailLayout(context, layout);
	}
	
	/**
	 * 初始化progress
	 * @param context
	 * @param layout
	 */
	@SuppressLint("InflateParams")
	private void initProgress(Context context , RelativeLayout layout){
		LayoutInflater inflater = LayoutInflater.from(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mProgressShadow = new View(context);
		mProgressShadow.setBackgroundColor(Color.parseColor("#33000000"));
		mProgressShadow.setLayoutParams(lp);
		mProgressShadow.setVisibility(View.GONE);
		layout.addView(mProgressShadow);
		//初始化progressBar
		mProgressBar = (RelativeLayout) inflater.inflate(R.layout.xh_main_loading, null);
		lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mProgressBar.setLayoutParams(lp);
		//设置加载中动画
		ImageView loadingView = (ImageView)mProgressBar.findViewById(R.id.loadingIv);
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.loading_anim);
		loadingView.startAnimation(anim);
		
		hideProgressBar();
		layout.addView(mProgressBar);
	}
	
	/**
	 * 初始化loadFailLayout
	 * @param context
	 * @param layout
	 */
	private void initLoadFailLayout(Context context , RelativeLayout layout){
		mLoadFailLayout = new LinearLayout(context);
		mLoadFailLayout.setOrientation(LinearLayout.VERTICAL);
		ImageView loadFaildImg = new ImageView(context);
		TextView loadFailTv = new TextView(context);
		mLoadFailBtn = new TextView(context);
		mLoadFailLayout.addView(loadFaildImg);
		mLoadFailLayout.addView(loadFailTv);
		mLoadFailLayout.addView(mLoadFailBtn);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.setMargins(0,topHeight,0,0);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mLoadFailLayout.setLayoutParams(lp);
		mLoadFailLayout.setGravity(Gravity.CENTER);
		
		int dp = ToolsDevice.dp2px(context, 52); 
		LinearLayout.LayoutParams loadFaildParams = new LinearLayout.LayoutParams(dp, dp);
		loadFaildParams.setMargins(0, 0, 0, ToolsDevice.dp2px(context, 32));
		loadFaildParams.gravity = Gravity.CENTER_HORIZONTAL;
		loadFaildImg.setLayoutParams(loadFaildParams);
		loadFaildParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		loadFaildParams.setMargins(0, 0, 0, ToolsDevice.dp2px(context, 14));
		loadFailTv.setLayoutParams(loadFaildParams);
		loadFaildParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLoadFailBtn.setLayoutParams(loadFaildParams);
		
		loadFaildImg.setScaleType(ScaleType.CENTER_INSIDE);
		loadFaildImg.setBackgroundResource(R.drawable.z_loading_failed);
		loadFaildImg.setClickable(true);
		loadFailTv.setText("加载失败，请重试");
		loadFailTv.setTextSize(Tools.getDimenSp(context, R.dimen.sp_16));
		loadFailTv.setTextColor(Color.parseColor("#949494"));
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setCornerRadius(Tools.getDimen(context,R.dimen.dp_4));
		String color = Tools.getColorStr(context,R.color.comment_color);
		gradientDrawable.setStroke(Tools.getDimen(context,R.dimen.dp_0_5), Color.parseColor(color));
		mLoadFailBtn.setBackgroundDrawable(gradientDrawable);
		mLoadFailBtn.setClickable(false);
		mLoadFailBtn.setGravity(Gravity.CENTER);
		mLoadFailBtn.setText("重新加载");
		mLoadFailBtn.setTextColor(Color.parseColor(color));
		mLoadFailBtn.setTextSize(Tools.getDimenSp(context, R.dimen.sp_14));
		int lrDp = Tools.getDimen(context,R.dimen.dp_20);
		int tbDp = Tools.getDimen(context,R.dimen.dp_7);
		mLoadFailBtn.setPadding(lrDp, tbDp, lrDp, tbDp);
		hideLoadFailBar();
		layout.addView(mLoadFailLayout);
	}
	
	/**
	 * 设置FailLayout的OnClickListener
	 * @param listener
	 */
	public void setFailClickListener(OnClickListener listener){
		if(listener != null){
			mLoadFailLayout.setOnClickListener(listener);
		}
	}
	
	public boolean isShowingProgressBar(){
		if(mProgressBar == null){
			return false;
		}
		return mProgressBar.getVisibility() == View.VISIBLE;
	}
	
	public void showProgressBar(){
		if(mProgressBar != null){
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideProgressBar(){
		if(mProgressBar != null){
			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	public boolean isShowingLoadFailBar(){
		if(mLoadFailLayout == null){
			return false;
		}
		return mLoadFailLayout.getVisibility() == View.VISIBLE;
	}
	
	public void showLoadFailBar(){
		if(mLoadFailBtn != null){
			mLoadFailLayout.setVisibility(View.VISIBLE);
		}
	}
	
	 public void hideLoadFailBar(){
		 if(mLoadFailBtn != null){
			 mLoadFailLayout.setVisibility(View.GONE);
		 }
	 }
	 
	 public void showProgressShadow(){
		 if(mProgressShadow != null){
			 mProgressShadow.setVisibility(View.VISIBLE);
		 }
		 mProgressBar.setOnClickListener(new OnClickListener() {
			@Override	public void onClick(View v) {}
		});
	 }
}
