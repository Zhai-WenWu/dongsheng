package amodule.dish.view.UploadDish;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishMakeOptionActivity;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.tools.UploadDishControl;
import amodule.dish.tools.UploadDishSpeechTools;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;
 
/**
 * 做法步骤控制
 * @author FrangRuijiao
 * @data 2015年10月15日10:19
 */
@SuppressWarnings("EmptyCatchBlock")
public class DishMakeView{

	private UploadDishActivity mAct;
	private View mMakeView;
	private UploadDishData mDishData;

	private LinearLayout linearMake;
	private LayoutInflater inflater;

	private int stepCreatingNum = 0;
	private static final int MAKE_STEP_MAX = 40;
	private int makeStepIndex = 0;
	/**
	 * 下面两个存着当前等待着点击编辑数据等待回来数据的控件
	 */
	private ImageView currentMakeImg;
	private TextView currentMakeImgPathTV;

	/** 选中数据集合 */
	private ArrayList<String> mImageList = new ArrayList<>();

	/**
	 * 做法步骤控制构造
	 */
	public DishMakeView(UploadDishActivity activity, View makeView, UploadDishData dishData) {
		this.mAct = activity;
		mMakeView = makeView;
		mDishData = dishData;
		init();
	}

	private void init() {
		mImageList = new ArrayList<>();
		mAct.findViewById(R.id.dish_upload_item_make_add_max).setVisibility(View.VISIBLE);
		mAct.findViewById(R.id.dish_upload_item_make_add_max).setOnClickListener(getAddMakeMaxListener());
		inflater = LayoutInflater.from(mAct);
		linearMake = (LinearLayout)mAct.findViewById(R.id.linear_make);
		mAct.findViewById(R.id.tv_addMake).setOnClickListener(getAddStepListener());
		mAct.findViewById(R.id.tv_trimMake).setOnClickListener(getOnChangeStepListener());
		setMakesView(mDishData.getMakes(),3);
	}

	public int getCurrentCreatingNum(){
		return stepCreatingNum;
	}

	protected int getImageEmpty(){
		int index = -1;
		for(int i = 0; i < linearMake.getChildCount(); i++){
			View view = linearMake.getChildAt(i);
			TextView tv_path = (TextView)view.findViewById(R.id.tv_make_path);
			if(!TextUtils.isEmpty(tv_path.getText())){
				index = i;
			}
		}
		return index;
	}

	/**
	 * 组合步骤做法和效果图数据 根据dataType返回草稿数据、所有数据
	 */
	public String getDishMakeData() {
		JSONArray jsonArray = new JSONArray();
		int setpIndex = 1;
		for (int index = 0; index < linearMake.getChildCount(); index++) {
			View view = linearMake.getChildAt(index);
			//noinspection EmptyCatchBlock
			try {
				String makeInfo = ((EditText) view.findViewById(R.id.dish_up_make_title)).getText().toString();
				makeInfo = StringManager.getUploadString(makeInfo);
//				String makeId = ((TextView) view.findViewById(R.id.tv_makeStep)).getText().toString();
				String makeImg = ((TextView) view.findViewById(R.id.tv_make_path)).getText().toString();
				// 如果没有步骤图并且没有图片则舍弃该数据;
				if (makeInfo.length() == 0 && makeImg.length() == 0) {
					continue;
				}
				// 数据库存储使用Json格式
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("makesStep", setpIndex);
				jsonObj.put("makesImg", makeImg);
				jsonObj.put("makesInfo", makeInfo);
				jsonArray.put(jsonObj);
				setpIndex ++;
			} catch (JSONException ex) {
			}
		}
		return jsonArray.toString();
	}



	/**
	 * 批量添加步骤图
	 */
	public View.OnClickListener getAddMakeMaxListener(){
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(makeStepIndex < MAKE_STEP_MAX){
					XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "步骤操作", "点击批量添加步骤图");
					//新方法开启图片选择
					Intent intent = new Intent();
					intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, mImageList);
					if(mImageList.size() > 0){
						intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, (MAKE_STEP_MAX - (getImageEmpty() + 1)) > 8 ? 8 : MAKE_STEP_MAX - (getImageEmpty() + 1));
					}else{
						intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, 8);
					}
					intent.setClass(mAct, ImageSelectorActivity.class);
					mAct.startActivityForResult(intent, UploadDishActivity.DISH_ADD_MAKE_MAX);
				}else{
					Tools.showToast(mAct, "最多" + MAKE_STEP_MAX + "步哦!");
				}
			}
		};
	}

	/**
	 * 点击调整步骤
	 */
	public View.OnClickListener getOnChangeStepListener(){
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "步骤操作", "点击调整步骤按钮");
				Intent it = new Intent(mAct,UploadDishMakeOptionActivity.class);
				it.putExtra("makesJson", getDishMakeData());
				mAct.startActivityForResult(it, UploadDishActivity.DISH_MAKE_ITEM_OPTION);
			}
		};
	}

	/**
	 * 点击添加步骤
	 */
	public View.OnClickListener getAddStepListener(){
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setMakesView("",1);
			}
		};
	}

	private void changeMakeImage(ArrayList<String> resultLists){
		int linearMakeCount = linearMake.getChildCount();
		int resultSize = resultLists.size();
		int index = getImageEmpty();
		int size = linearMakeCount - (index + 1); //之所以+1是因为算出来的是index
		if(size < resultSize){
			int addNum = resultSize - size;
			setMakesView("",addNum);
			linearMakeCount += addNum;
		}
		int imgIndex = 0;
		for(int j = index + 1; j < linearMakeCount; j++){
			if(imgIndex < resultSize){
				View view = linearMake.getChildAt(j);
				TextView tv_path = (TextView)view.findViewById(R.id.tv_make_path);
				ImageView iv_makes = (ImageView) view.findViewById(R.id.iv_makes);
				mImageList.remove(tv_path.getText());
				String imgPath = resultLists.get(imgIndex);
				setImageView(view,iv_makes,tv_path,imgPath);
				mImageList.add(imgPath);
				imgIndex ++;
			}else{
				break;
			}
		}
	}
	
	/**
	 * 点击删除步骤
	 * @param v
	 */
	private void onDeleteStepView(View v) {
		View parentView = (View) v.getParent();
		TextView tvPath = (TextView)parentView.findViewById(R.id.tv_make_path);
		mImageList.remove(tvPath.getText());
		linearMake.removeView(parentView);
		makeStepIndex --;
		for(int i = 0; i < linearMake.getChildCount(); i++){
			View view = linearMake.getChildAt(i);
			TextView tv_step = (TextView) view.findViewById(R.id.tv_makeStep);
			tv_step.setText((i + 1)+"");
		}
	}

	/**
	 * 根据resultCode处理，修改做法操作、传步骤图片操作
	 */
	public void onResultBack(int resultCode, Intent data) {
		switch (resultCode) {
		case UploadDishActivity.DISH_ADD_MAKE:
			ArrayList<String> resultList = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
			if(resultList.size() > 0 && currentMakeImg != null){
				
				String imgUrl = resultList.get(0);
				mImageList.add(imgUrl);
				mImageList.remove(currentMakeImgPathTV.getText());
				setImageView((View)currentMakeImg.getParent().getParent(), currentMakeImg,currentMakeImgPathTV, imgUrl);
			}
			break;
		case UploadDishActivity.DISH_ADD_MAKE_MAX:
			ArrayList<String> resultLists = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
			if(resultLists.size() > 0){
				changeMakeImage(resultLists);
			}
			break;
		case UploadDishActivity.DISH_MAKE_ITEM_OPTION:
			String json = data.getStringExtra(UploadDishMakeOptionActivity.MAKE_ITEM_OPTION_DATA);
			linearMake.removeAllViews();
			makeStepIndex = 0;
			mImageList.clear();
			setMakesView(json,3);
			break;
		}
	}
	
	/**
	 * 设置详细做法界面数据
	 * @param makesJson 要添加入的配料数据,没有,则留空;
	 * @param increase 当makesJson为空时有用,表示为空需要增加的空行数量;
	 */
	public void setMakesView(String makesJson, int increase) {
		ArrayList<Map<String, String>> listMakes = UtilString.getListMapByJson(makesJson);
		// 数据库中没有数据,则默认添加increase条空行;
		boolean isRecovery = listMakes.size() != 0;
		int deep = listMakes.size() == 0 ? increase : listMakes.size();
		for (int index = 0; index < deep; index++) {
			makeStepIndex++;
			if(makeStepIndex > MAKE_STEP_MAX){
				Tools.showToast(mAct, "最多" + MAKE_STEP_MAX + "步哦!");
				return;
			}
			View view = inflater.inflate(R.layout.a_dish_upload_make_item, null);

			TextView tv_step = (TextView) view.findViewById(R.id.tv_makeStep);
			final EditText etMakeTitle = (EditText) view.findViewById(R.id.dish_up_make_title);
			final TextView tv_make_path = (TextView) view.findViewById(R.id.tv_make_path);
			final ImageView iv_makes = (ImageView) view.findViewById(R.id.iv_makes);
			ImageView ivMakeDele = (ImageView) view.findViewById(R.id.iv_makeDele);

			ivMakeDele.setTag(makeStepIndex);
			ivMakeDele.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.i("FRJ","delete Clcik");
					onDeleteStepView(v);
				}
			});
			iv_makes.setTag(makeStepIndex);
			linearMake.addView(view);
			// 有则恢复数据
			if (isRecovery) {
				final Map<String, String> mapBurden = listMakes.get(index);
				tv_step.setText(mapBurden.get("makesStep"));
				etMakeTitle.setText(mapBurden.get("makesInfo"));
				String path = mapBurden.get("makesImg");
				setImageView(view,iv_makes,tv_make_path,path);
				mImageList.add(path);
			}else{
				setImageView(view,iv_makes,tv_make_path,"");
			}
			iv_makes.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					currentMakeImg = iv_makes;
					currentMakeImgPathTV = tv_make_path;
					//新方法开启图片选择
					Intent intent = new Intent();
					intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
					intent.putExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, mImageList);
					intent.setClass(mAct, ImageSelectorActivity.class);
					mAct.startActivityForResult(intent, UploadDishActivity.DISH_ADD_MAKE);
				}
			});
			tv_step.setText(makeStepIndex + "");
//			etMakeTitle.setOnFocusChangeListener(mAct);
			view.findViewById(R.id.dish_up_speech_make_title).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "语音", "直接点击语音按钮");
					etMakeTitle.requestFocus();
					UploadDishSpeechTools.createUploadDishSpeechTools().startSpeech(etMakeTitle);
				}
			});
//			etMakeTitle.addTextChangedListener(mAct);
		}
	}
	
	/**
	 * 界面上所有的imageView操作;
	 * @param imageView
	 * @param imgUrl 图片url
	 */
	private void setImageView(final View view,final ImageView imageView,final TextView tv_make_path, final String imgUrl) {
		// 根据类型和位序来设置图片
		if (imgUrl != null && imgUrl.length() > 0) {
			if (imgUrl.indexOf("http") == 0) {
				tv_make_path.setText(imgUrl);
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mAct)
					.load(imgUrl)
					.build();
				if(bitmapRequest != null)
					bitmapRequest.into(getTarget(view,imageView,160,0));
			} else{
				uploadImage(view,imageView,tv_make_path,imgUrl);
			}
		}
	}
	
	private void uploadImage(final View view,final ImageView imageView,final TextView tv_make_path,final String imgUrl){
		final int wdp = ToolsDevice.dp2px(mAct, 160);
		final Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
 				Bitmap bmp = (Bitmap)msg.obj;
				if(bmp != null){
					tv_make_path.setText(imgUrl);
					setMakeBackGone(view);
					UtilImage.setImgViewByWH(imageView, bmp, wdp, 0, false);
					UploadDishControl uploadDishControl = UploadDishControl.getInstance();
					uploadDishControl.uploadImg(mDishData.getUploadTimeCode(), UploadDishControl.imgType_makeImg, imgUrl);
				}
				stepCreatingNum --;
			}
			
		};
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				stepCreatingNum ++;
				Bitmap bmp = UtilImage.imgPathToBitmap(imgUrl, wdp, 0, false, null);
				Message msg = new Message();
				msg.obj = bmp;
				handler.sendMessage(msg);

			}
		}).start();
	}
	
	private SubBitmapTarget getTarget(final View view, final ImageView v, final int width_dp, final int height_dp){
		return new SubBitmapTarget(){
			@Override
			public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
				setMakeBackGone(view);
				ImageView img = v;
				if (img != null && bitmap != null) {
					UtilImage.setImgViewByWH(img, bitmap, ToolsDevice.dp2px(mAct, width_dp), height_dp, false);
				}
			}};
	}

	private void setMakeBackGone(View view){
		view.findViewById(R.id.iv_makes_back0).setBackgroundColor(0xF5F5F5);
		view.findViewById(R.id.iv_makes_back1).setVisibility(View.GONE);
		view.findViewById(R.id.iv_makes_back2).setVisibility(View.GONE);
	}
	
}
