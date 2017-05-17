package acore.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.Tools;

/**
 * 自定义 Toast，用于提示用户加积分
 * @author FangRuijiao
 */
public class ToastCustom {
	
	private WindowManager mWindowManager;
	private Context mContext;
	private ArrayList<Map<String, String>> mArrayData;
	private List<View> arrayView;
	private int mIndex;
	private int mViewId;
	
	public ToastCustom(Context context,int viewId,ArrayList<Map<String, String>> array){
		mContext = context;
		mViewId = viewId;
		mArrayData = array;
		init();
	}
	
	private void init(){
		mIndex = 0;
		arrayView = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		WindowManager.LayoutParams layoutParams;
		// Context.getSystemService(Context.WINDOW_SERVICE); 这个Context要用Application的context
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		for(int i = 0; i < mArrayData.size(); i++){
			View view = inflater.inflate(mViewId, null);
			TextView tv = (TextView)view.findViewById(R.id.pop_tv);
			String showText;
			if(mArrayData.get(i).containsKey("message")) showText = mArrayData.get(i).get("message");
			else showText = mArrayData.get(i).get("");
			tv.setText(showText);
			
			layoutParams = new WindowManager.LayoutParams();
	//        //设置window的type
	        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
	        layoutParams.format = PixelFormat.RGBA_8888;
	        //设置浮动窗口不可聚焦
	        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
	        //位置
	        layoutParams.gravity = Gravity.CENTER;
	        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
	        layoutParams.height = Tools.getDimen(mContext, R.dimen.dp_100);
	        mWindowManager.addView(view, layoutParams);
	        tv.setVisibility(View.GONE);
	        arrayView.add(view);
		}
	}
	
	public void show() {
		if(mIndex < arrayView.size()){
			View view = arrayView.get(mIndex);
			exceScale(view.findViewById(R.id.pop_tv));
		}
    }
	
	/*********动画*********/
	/***缩放动画*/
	public void exceScale(final View view){
		view.setVisibility(View.VISIBLE);
		Animation scale = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.scale_form0_to1);
		scale.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				mIndex ++;
				show();
				exceSet(view);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}});
		view.startAnimation(scale);
	}
	/**组合动画*/
	public void exceSet(final View mView){
		Animation set = (Animation)AnimationUtils.loadAnimation(mContext, R.anim.set_translate_up_alpha_0);
		set.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				mView.setVisibility(View.GONE);
				if(mIndex >= arrayView.size()) exceOver();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}});
		mView.startAnimation(set);
	}
	
	private void exceOver(){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				try{
					for(View view : arrayView){
						mWindowManager.removeView(view);
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		},500);
	}
	
	
}
