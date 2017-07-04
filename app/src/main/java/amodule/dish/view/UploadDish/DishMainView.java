package amodule.dish.view.UploadDish;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.AgreementManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.UploadDishContentControl;
import amodule.dish.tools.UploadDishControl;
import amodule.dish.tools.UploadDishSpeechTools;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import xh.basic.tool.UtilImage;
 
/**
 * 传菜谱主控制
 * @author LiangYuanYuan
 * @TODO 
 * @data 2015年10月14日下午4:40:21 
 */
public class DishMainView{
	private UploadDishActivity mUploadDishActivity;
	private View mDishView;
	private DishIngredientView mDishIngredientView;
	private DishMakeView mDishMakeView;
	private DishOtherControl mDishOtherControl;
	/**菜谱名、心得和小贴士*/
	private EditText mDishNameEt,mDishIdeaEt,mDishTipsEt;
	private ImageView dishImageIv;
	private UploadDishData uploadDishData;
	/**效果图图片地址*/
	private String imgUrl = "";
	/**协议*/
	private AgreementManager mAgreementManager;
	
	private boolean imgIsCreateOk = true;

	private UploadDishControl mUploadDishControl;
	
	/**
	 * 传菜谱主控制构造
	 */
	public DishMainView(UploadDishActivity uploadDishActivity, View dishView, UploadDishData dishData) {
		super();
		this.mUploadDishActivity = uploadDishActivity;
		this.mDishView = dishView;
		this.uploadDishData = dishData;
		initSpeech();
		initView();
		initData();
	}
	
	private void initSpeech(){
		UploadDishSpeechTools speechTools = UploadDishSpeechTools.createUploadDishSpeechTools();
		speechTools.initSpeech(mUploadDishActivity);
//		TextView btn = (TextView)mUploadDishActivity.findViewById(R.id.dish_up_speech_btn);
//		speechTools.setStartButton(btn);
	}
 
	/** 
	 * 初始化数据
	 */
	private void initData() {
		mUploadDishControl = UploadDishControl.getInstance();
		imgUrl = uploadDishData.getCover();
		setImageView(dishImageIv,imgUrl);
		String name=TextUtils.isEmpty(uploadDishData.getRemoveName())?uploadDishData.getName():uploadDishData.getName().replace(uploadDishData.getRemoveName(), "");
		changeViewData(name, mDishNameEt);
		changeViewData(uploadDishData.getStory(), mDishIdeaEt);
		changeViewData(uploadDishData.getTips(), mDishTipsEt);
	}

	/**
	 * 初始化
	 */
	public void initView() {
		mUploadDishActivity.findViewById(R.id.a_dish_score_hint_close).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mUploadDishActivity.findViewById(R.id.a_dish_score_hint).setVisibility(View.GONE);
			}
		});
		View dish_ingredientView = mDishView.findViewById(R.id.a_dish_ingredient_view);
		View dish_makeView = mDishView.findViewById(R.id.a_dish_make_view);
		LinearLayout dish_otherView = (LinearLayout) mDishView.findViewById(R.id.dish_other_content);
		//积分规则
		mDishView.findViewById(R.id.rule_tv).setOnClickListener(onIntegralInfoListener);
		//效果图
		dishImageIv = (ImageView)mDishView.findViewById(R.id.dishImage_iv);
		dishImageIv.setOnClickListener(clickDishImgListener);
		mDishIngredientView = new DishIngredientView(mUploadDishActivity, dish_ingredientView, uploadDishData,false);
		mDishMakeView  = new DishMakeView(mUploadDishActivity, dish_makeView, uploadDishData);
		mDishOtherControl = new DishOtherControl(mUploadDishActivity, dish_otherView, uploadDishData);
		mAgreementManager = new AgreementManager(mUploadDishActivity,StringManager.api_agreementOriginal);
		
		View dish_head_layout = mDishView.findViewById(R.id.a_dish_head_layout);
		View dish_tip_layout = mDishView.findViewById(R.id.a_dish_tip_layout);
		mDishNameEt= (EditText) mDishView.findViewById(R.id.a_dish_upload_title);
		mDishIdeaEt = (EditText) dish_head_layout.findViewById(R.id.a_dish_upload_item_speech_et);
		mDishTipsEt = (EditText) dish_tip_layout.findViewById(R.id.a_dish_upload_item_speech_et);
		TextView draftNum = (TextView)mUploadDishActivity.findViewById(R.id.a_dish_upload_go_draft_btn);
		
		mDishNameEt.setTag(R.id.dish_upload_number, 30);
		mDishNameEt.setTag(R.id.dish_upload_hint, "菜谱名不能超过30个字哦");
		mDishIdeaEt.setTag(R.id.dish_upload_number, 800);
		mDishIdeaEt.setTag(R.id.dish_upload_hint, "心得不能超过800个字哦");
		mDishIdeaEt.setMaxLines(5);

		UploadDishSqlite sqlite = new UploadDishSqlite(mUploadDishActivity);
		draftNum.setText("草稿箱  (" + sqlite.getAllDraftSize() + ")");

		UploadDishContentControl.addDishNameChangeListener(mUploadDishActivity,mDishNameEt);

		initSpeechView(mDishView,mDishNameEt,true);
		initSpeechView(dish_head_layout,mDishIdeaEt,true);
		initSpeechView(dish_tip_layout,mDishTipsEt,true);
		draftNum.setOnClickListener(mUploadDishActivity);
	}

	private void initSpeechView(View parentView,final EditText et,boolean isScroll){
		if(isScroll){
			ScrollView scrollView_parent = (ScrollView) mUploadDishActivity.findViewById(R.id.scrollView);
			ScrollView scrollView_edit = (ScrollView) parentView.findViewById(R.id.scrollviewedit);
			((acore.widget.ScrollviewEdit) scrollView_edit).setParent_scrollview(scrollView_parent);
		}
//		et.setOnFocusChangeListener(mUploadDishActivity);
		if(parentView != null){
			parentView.findViewById(R.id.a_dish_upload_item_speech_iv).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					XHClick.mapStat(mUploadDishActivity, UploadDishActivity.STATISTICS_ID, "语音", "直接点击语音按钮");
					et.requestFocus();
					UploadDishSpeechTools.createUploadDishSpeechTools().startSpeech(et);
				}
			});
		}
		et.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int maxLength = -1;
				String hint = "";
				if(et.getTag(R.id.dish_upload_number) != null){
					maxLength = Integer.parseInt(String.valueOf(et.getTag(R.id.dish_upload_number)));
					hint = String.valueOf(et.getTag(R.id.dish_upload_hint));
				}
				if(maxLength > 0 && s.length() > maxLength){
					CharSequence text = s.subSequence(0, maxLength);
					et.setText(text);
					et.setSelection(text.length());
					Tools.showToast(mUploadDishActivity, hint);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	
	/** 
	 * 获取各个菜谱数据
	 * @TODO 
	 * 食材和辅料控制_组合数据
	 * 做法步骤_组合数据
	 * 组合效果图，菜谱名、心得、小贴士数据
	 */
	public UploadDishData getDishData() {
		uploadDishData.setCover(imgUrl);
		uploadDishData.setName(StringManager.getUploadString(mDishNameEt.getText()));//菜谱名字
		uploadDishData.setStory(StringManager.getUploadString(mDishIdeaEt.getText()));//心得
		uploadDishData.setFood(mDishIngredientView.getFoodData());//食材
		uploadDishData.setBurden(mDishIngredientView.getIngredientData());//辅料 
		uploadDishData.setMakes(mDishMakeView.getDishMakeData());//获取做法步骤数据，包括文字和图片
		uploadDishData.setTips(StringManager.getUploadString(mDishTipsEt.getText()));// 小贴士
		uploadDishData.setAddTime(Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));//添加时间
		uploadDishData.setReadyTime(mDishOtherControl.getReadyTime()); //准备时间
		uploadDishData.setCookTime(mDishOtherControl.getCookTime()); //烹饪时间
		uploadDishData.setTaste(mDishOtherControl.getTaste()); //口味
		uploadDishData.setDiff(mDishOtherControl.getDiff()); //难度
		uploadDishData.setExclusive(mDishOtherControl.getExclusive()); //是否独家
		uploadDishData.setCheckGreement(mAgreementManager.getIsChecked());
		uploadDishData.setVideType(false);
		return uploadDishData; 
	}
	
	public int getCurrentCreatingNum(){
		return mDishMakeView.getCurrentCreatingNum();
	}
	
	public boolean getImgIsCreateOk(){
		return imgIsCreateOk;
	}
	
	/** 
	 * 根据resultCode处理，修改菜谱名效果图等操作
	 * @TODO 
	 * 该界面得到的resultCode,就在当前界面进行处理。
	 * 其他说有则进行下发
	 */
	public void onResultBack(int requestCode,Intent data){
		if (data == null)
			return;
		switch (requestCode) {
		case UploadDishActivity.DISH_ADD_MAKE:
			mDishMakeView.onResultBack(requestCode, data);
			break;
		case UploadDishActivity.DISH_ADD_MAKE_MAX:
			mDishMakeView.onResultBack(requestCode, data);
			break;
		case UploadDishActivity.DISH_MAKE_ITEM_OPTION:
			mDishMakeView.onResultBack(requestCode, data);
			break;
		case UploadDishActivity.DISH_CHOOSE_SINGLE_IMG:
			ArrayList<String> resultList = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
			if(resultList.size() > 0){
				imgUrl = resultList.get(0);
				setImageView(dishImageIv, imgUrl);
			}
			break;
		}
	}
	 
	/**
	 * 点击查看积分详情
	 * @TODO
	 */
	private OnClickListener onIntegralInfoListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) { 
			if (LoginManager.isLogin()) {
				String url = StringManager.api_integralInfo + "?code="+ LoginManager.userInfo.get("code");
				AppCommon.openUrl(mUploadDishActivity, url, true);
			} else {
				Tools.showToast(mUploadDishActivity, "登录后即可查看积分规则");
				mUploadDishActivity.startActivity(new Intent(mUploadDishActivity, LoginByAccout.class));
			}
		}
	};
	
	/**
	 * 点击添加效果图
	 */
	private OnClickListener clickDishImgListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) { 
			chooseImgMethod();
		}
	};
	
	/**
	 * 设置界面上的添加按钮与显示内容的显示状态
	 * @param content : 内容
	 * @param et : 存放内容的TextView
	 */
	private void changeViewData(String content, EditText et) {
		if (!TextUtils.isEmpty(content) && content.length() > 0) {
			et.setText(content);
		}
	}
	
	/** 
	 * 打开选择图片
	 */
	private void chooseImgMethod(){
		//新方法开启图片选择
		Intent intent = new Intent();
		intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
		intent.setClass(mUploadDishActivity, ImageSelectorActivity.class);
		mUploadDishActivity.startActivityForResult(intent, UploadDishActivity.DISH_CHOOSE_SINGLE_IMG);
	}
	
	/**
	 * 界面上所有的imageView操作;
	 * @param imageView
	 * @param imgUrl 
	 */
	private void setImageView(final ImageView imageView, final String imgUrl) {
		// 根据类型和位序来设置图片
		if (imgUrl != null && imgUrl.length() > 0) {
			if (imgUrl.indexOf("http") == 0) {
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(imageView.getContext())
					.load(imgUrl)
					.build();
				if(bitmapRequest != null)
					bitmapRequest.into(getTarget(imageView,0,0));
			} else {
				dealLocalImg(imageView);
			}
		}
	}
	
	
	private void dealLocalImg(final ImageView imageView){
		final Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Bitmap bmp = (Bitmap)msg.obj;
				if(bmp != null){
					imageView.setImageBitmap(bmp);
					//上传图片
					mUploadDishControl.uploadImg(uploadDishData.getUploadTimeCode(), UploadDishControl.imgType_bigImg, imgUrl);
				}else
					imgUrl = "";
				imgIsCreateOk = true;
			}
			
		};
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				imgIsCreateOk = false;
				Bitmap bmp = UtilImage.imgPathToBitmap(imgUrl, ToolsDevice.dp2px(mUploadDishActivity, 800), 
						ToolsDevice.dp2px(mUploadDishActivity, 400), false, null);
				Message msg = new Message();
				msg.obj = bmp;
				handler.sendMessage(msg);

			}
		}).start();
	}
	
	public boolean checkDishDataIsEmpty(){
		getDishData();
		return !(uploadDishData.getName().length() == 0 && uploadDishData.getCover().length() == 0 &&
				uploadDishData.getBurden().length() < 3 && uploadDishData.getFood().length() < 3 &&
				uploadDishData.getMakes().length() < 3 && uploadDishData.getTips().length() == 0);
	}
	
	private SubBitmapTarget getTarget(final ImageView v, final int width_dp, final int height_dp){
		return new SubBitmapTarget(){
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				ImageView img = v;
				if (img != null && bitmap != null) {
					UtilImage.setImgViewByWH(img, bitmap, ToolsDevice.dp2px(mUploadDishActivity, width_dp), height_dp, false);
				}
			}};
	}
}
