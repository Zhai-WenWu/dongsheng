package amodule.main.view;

import java.util.ArrayList;
import java.util.Map;

import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.main.Main;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

public class MainBuoy {
	private Main mAct;
	// 活动浮标所有东西
	private Handler mainFloatHandler;
	private String floatIndex = "1";
	private String floatSubjectList = "1";
	private String floatSubjectInfo = "1";
	private boolean isOneFloat = true;
	private boolean isMove = false;// 活动图标是否全部滑出
	private boolean isClosed = false;
	private Animation close;// 关闭动画
	private Animation open;// 打开动画
	private ImageView imageButton;
	
	public MainBuoy(Main act){
//		Log.i("tzy","create MainBuoy");
		this.mAct = act;
		// 浮动按钮
		isMove = true;
		
		initBuoy();
		initAnimation();
		initHandler();
		getBuoyData();// 加载数据(悬浮按钮)
		isOneFloat = true;
	}
	
	private void initBuoy(){
		imageButton = new ImageView(mAct);
		int width = Tools.getDimen(mAct, R.dimen.dp_45);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
		DisplayMetrics dm = ToolsDevice.getWindowPx(mAct);
		params.setMargins(params.leftMargin, dm.heightPixels / 5 * 2,params.rightMargin, params.bottomMargin);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		if(mAct.getRootLayout() == null){
			return;
		}
		mAct.getRootLayout().addView(imageButton,params);
		hide();//初始化完成后hide浮标
	}
	
	private void initAnimation() {
		float floatAnimation = Tools.getDimen(mAct, R.dimen.dp_35);
		close = new TranslateAnimation(0, 0 + floatAnimation, 0, 0);
		close.setFillEnabled(true);
		close.setFillAfter(true);
		close.setDuration(300);
		open = new TranslateAnimation(0 + floatAnimation, 0, 0, 0);
		open.setFillEnabled(true);
		open.setFillAfter(true);
		open.setDuration(300);
	}
	
	private void initHandler() {
		mainFloatHandler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					if (isMove) {
						executeCloseAnim();
					}
					break;
				case 2:
					if (!isMove) {
						executeOpenAnim();
					}
					break;
				}
				return false;
			}
		});
	}

	//获取活动数据
	public void getBuoyData() {
		ReqInternet.in().doGet(StringManager.api_getActivityBuoy, new InternetCallback(mAct) {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					ArrayList<Map<String, String>> FloatDataList = UtilString.getListMapByJson(returnObj);
					if (FloatDataList.size() > 0) {
						Map<String, String> map = FloatDataList.get(0);
						final String floatUrl = map.get("url");
						if(!TextUtils.isEmpty(floatUrl)){
							bindClick(floatUrl);
						}
						if (!TextUtils.isEmpty(map.get("img"))) {
							setBuoyImage(imageButton, map.get("img"));
						}
						if(!TextUtils.isEmpty(map.get("site"))){
							ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("site"));
							for(Map<String, String> praseMap : listMapByJson){
								parseDisplayPosition(praseMap);
							}
						}
					}
				}
			}

			private void parseDisplayPosition(Map<String, String> map) {
				String site = map.get("");
				if ("index".equals(site)) {
					floatIndex = "2";
					if (mAct.getCurrentTab() == 0) {
						show();
					}
				} else if ("subjectList".equals(site)) {
					floatSubjectList = "2";
					if (mAct.getCurrentTab() == 1) {
						show();
					}
				} else if ("subjectInfo".equals(site)) {
					floatSubjectInfo = "2";
				}
			}

			private void bindClick(final String floatUrl) {
				imageButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (isMove) {
							AppCommon.openUrl(mAct,floatUrl, true);
							executeCloseAnim();
						} else {
							executeOpenAnim();
						}
					}
				});
			}
			
			/**
			 * 处理图片
			 * @param iv ImageView
			 * @param imgUrl 图片链接
			 */
			private void setBuoyImage(final ImageView iv, String imgUrl) {
				iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
					.load(imgUrl)
					.setPlaceholderId(R.drawable.z_quan_float_activity)
					.setErrorId(R.drawable.z_quan_float_activity)
					.build();
				if(bitmapRequest != null)
					bitmapRequest.into(iv);
			}
		});
	}
	
	/**
	 * 供外部调用,设置浮动按钮的数据.
	 */
	public void setFloatMenuData() {
		if ("2".equals(floatIndex)) {
			show();
			if (isClosed) {
				imageButton.startAnimation(isMove ? open :close);
				isClosed = false;
			}
		} else {
			clearAnimation();
			hide();
			if (isOneFloat) {
				isClosed = false;
				isOneFloat = false;
			} else {
				isClosed = true;
			}
			isMove = true;
		}
	}
	
	private void executeOpenAnim() {
		imageButton.startAnimation(open);
		isMove = true;
	}
	
	private void executeCloseAnim() {
		imageButton.startAnimation(close);
		isMove = false;
	}
	
	public void sendEmptyMessage(int what){
		mainFloatHandler.sendEmptyMessage(what);
	}
	
	public void sendMessage(int what,Object obj){
		Message msg = new Message();
		msg.what = what;
		msg.obj = obj;
		mainFloatHandler.sendMessage(msg);
	}
	
	public void clearAnimation(){
		if(imageButton != null){
			imageButton.clearAnimation();
		}
	}
	
	public void show(){
		if(imageButton != null){
			imageButton.setVisibility(View.VISIBLE);
		}
	}
	
	public void hide(){
		if(imageButton != null){
			imageButton.setVisibility(View.GONE);
		}
	}
	
	public String getFloatIndex() {
		return floatIndex;
	}

	public String getFloatSubjectList() {
		return floatSubjectList;
	}

	public void setMove(boolean isMove) {
		this.isMove = isMove;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

}
