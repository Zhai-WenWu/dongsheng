package amodule.main.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.article.activity.ArticleEidtActiivty;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.tools.DeviceUtilDialog;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.LoadImage;
import aplug.recordervideo.activity.RecorderActivity;
import aplug.recordervideo.tools.ToolsCammer;
import aplug.shortvideo.activity.MediaRecorderActivity;
import xh.windowview.XhDialog;

import static amodule.main.activity.MainChangeSend.dishVideoMap;

public class ChangeSendDialog extends Dialog{

	private Activity activity;
	protected View view;
	protected int height;
	
	private ImageView closeImage,tastImg;
	private GridView mGridView;
	private List<Map<String, String>> list;

	public ChangeSendDialog(Activity activity) {
		super(activity, R.layout.a_main_change_send);
		this.activity = activity;
		this.getWindow().setBackgroundDrawableResource(R.color.c_white_transparent_EE);
		/* 无标题栏 */
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		Window dialogWindow = activity.getWindow();

		/* 设置为全屏 */
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		lp.height = WindowManager.LayoutParams.FLAG_FULLSCREEN;

		this.getWindow().setGravity(Gravity.BOTTOM);

		// view
		this.view = this.getLayoutInflater().inflate(R.layout.a_main_change_send, null);
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();

		this.height = display.getHeight();
		this.addContentView(view, new LayoutParams(display.getWidth(), LayoutParams.WRAP_CONTENT));

		// 对话框设置监听
		this.setOnDismissListener(onDismissListener);
		init();
	}

	private void init(){
		closeImage = (ImageView) findViewById(R.id.close_image);
		tastImg = (ImageView) findViewById(R.id.change_send_tast);
		mGridView = (GridView) findViewById(R.id.change_send_gridview);
		initData();
		AdapterSimple adapter = new AdapterSimple(mGridView, list,
				R.layout.a_mian_change_send_item,
				new String[] { "name","img" }, new int[] { R.id.change_send_gridview_item_name,R.id.change_send_gridview_item_iv }
		);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				onClick(list.get(position).get("tag"));
			}
		});
		findViewById(R.id.activityLayout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDialog();
			}
		});
		findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDialog();
			}
		});
	}

	private void initData(){
		int itemNum = 0;
		list = new ArrayList<>();
		if (LoginManager.isShowsendsubjectButton()) {
			addButton("1",R.drawable.pulish_subject,"发贴");
			itemNum ++;
		}
		if (LoginManager.isShowsendDishButton()) {
			addButton("2",R.drawable.send_dish,"发菜谱");
			itemNum ++;
		}
		if (LoginManager.isShowShortVideoButton()) {
			addButton("3",R.drawable.pulish_video,"小视频");
			itemNum ++;
		}
		if (LoginManager.isShowRecorderVideoButton()) {
			addButton("4",R.drawable.pulish_record,"录制菜谱");
			itemNum ++;
		}
		if (LoginManager.isShowSendVideoDishButton()) {
			addButton("5", R.drawable.pulish_video_dish,"发视频菜谱");
			itemNum++;
		}
		//TODO 暂时添加
		addButton("6", R.drawable.pulish_video_dish,"发文章");
		itemNum++;
		if(dishVideoMap != null && dishVideoMap.size() > 0){
			String img = dishVideoMap.get("img");
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(activity)
					.load(img)
					.setSaveType(LoadImage.SAVE_CACHE)
					.build();
			if(bitmapRequest != null)
				bitmapRequest.into(tastImg);
			tastImg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AppCommon.openUrl(activity,dishVideoMap.get("url"),true);
					closeDialog();
				}
			});
		}
		if(itemNum > 2){
			mGridView.setNumColumns(3);
		}else{
			mGridView.setNumColumns(2);
		}
	}

	private void addButton(String tag,int img,String name){
		Map<String, String> uploadVideoDishMap = new HashMap<>();
		uploadVideoDishMap.put("tag", tag);
		uploadVideoDishMap.put("img", "ico" + img);
		uploadVideoDishMap.put("name", name);
		list.add(uploadVideoDishMap);
	}

	public void onClick(String tag){
		if(TextUtils.isEmpty(tag)) return;
		switch (tag){
			case "1": //发帖
				closeDialog();
				XHClick.mapStat(activity, "a_post_button","发帖子","");
				Intent subIntent = new Intent(activity,UploadSubjectNew.class);
				subIntent.putExtra("skip", true);
				activity.startActivity(subIntent);
				XHClick.track(activity,"发美食贴");
				break;
			case "2": //发菜谱
				if (!LoginManager.isLogin()) {
					Tools.showToast(activity,"请登录");
					Intent intent = new Intent(activity, LoginByAccout.class);
					activity.startActivity(intent);
					return;
				}
				XHClick.mapStat(activity, "uploadDish", "uploadDish", "从导航发", 1);
				XHClick.mapStat(activity, "a_post_button","发菜谱","");
				closeDialog();
				Intent dishIntent = new Intent(activity, UploadDishActivity.class);
				activity.startActivity(dishIntent);
				XHClick.track(activity,"发菜谱");
				break;
			case "3": //小视频
				closeDialog();
				if(LoginManager.canPublishShortVideo()) {
					XHClick.mapStat(activity, "a_post_button", "小视频", "");
					Intent smallVideo = new Intent(activity, MediaRecorderActivity.class);
					activity.startActivity(smallVideo);
					XHClick.track(activity,"发小视频贴");
				}else{
					Tools.showToast(activity,"请绑定手机号");
					Intent bindPhone = new Intent(activity, LoginByAccout.class);
					bindPhone.putExtra("type", "bind");
					bindPhone.putExtra("title", "绑定手机号");
					activity.startActivity(bindPhone);
				}
				break;
			case "4": //录制菜谱
				DeviceUtilDialog deviceUtilDialog = new DeviceUtilDialog(activity);
				deviceUtilDialog.deviceStorageSpaceState(1024, 500, new DeviceUtilDialog.DeviceCallBack() {
					@Override
					public void backResultState(Boolean state) {
						if(!state){
							if(ToolsCammer.checkSuporRecorder(true)){
								XHClick.mapStat(activity, "a_post_button","录制菜谱","");
								Intent recoreVideo = new Intent(activity, RecorderActivity.class);
								activity.startActivity(recoreVideo);
								activity.finish();
							}else{
								final XhDialog xhDialog = new XhDialog(activity);
								xhDialog.setTitle("您的设备不支持1080p视频拍摄！")
										.setSureButton("确定", new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												activity.finish();
												xhDialog.cancel();
											}
										}).show();
							}
						}else{
							activity.finish();
						}
					}
				});
				break;
			case "5": //发视频菜谱
				closeDialog();
				XHClick.mapStat(activity, "a_post_button","发视频菜谱","");
				Intent videoDish = new Intent(activity, UploadDishActivity.class);
				videoDish.putExtra(UploadDishActivity.DISH_TYPE_KEY, UploadDishActivity.DISH_TYPE_VIDEO);
				activity.startActivity(videoDish);
				break;
			case "6":
				closeDialog();
				Intent article = new Intent(activity, ArticleEidtActiivty.class);
				activity.startActivity(article);
				break;
		}
	}

	@Override
	public void show() {
		super.show();
		Animation cycleAnim = AnimationUtils.loadAnimation(activity, R.anim.shake);
		mGridView.startAnimation(cycleAnim);

		final Animation scale_to_visibilty = AnimationUtils.loadAnimation(activity, R.anim.rotate_45);
		closeImage.setAnimation(scale_to_visibilty);
	}

	/**
	 * 关闭dialog
	 */
	public void closeDialog() {
		Animation cycleAnim = AnimationUtils.loadAnimation(activity, R.anim.drop);
		mGridView.startAnimation(cycleAnim);
		closeImage.clearAnimation();
		Animation scale_to_nothing = AnimationUtils.loadAnimation(activity, R.anim.rotate_ninus_45);
		closeImage.startAnimation(scale_to_nothing);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				ChangeSendDialog.this.dismiss();
				activity.overridePendingTransition(R.anim.in_from_nothing, R.anim.out_to_nothing);
			}
		}, 280);
	}


	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			closeDialog();
		}
	};
}
