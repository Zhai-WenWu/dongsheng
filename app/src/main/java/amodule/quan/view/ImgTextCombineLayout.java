package amodule.quan.view;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.widget.TextViewShow;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;
import acore.tools.Tools;
import acore.tools.ToolsDevice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aplug.basic.SubBitmapTarget;
import aplug.basic.LoadImage;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xianghatest.R;

import core.xiangha.emj.tools.EmjParseMsgUtil;
import core.xiangha.emj.view.EditTextShow;

/**
 * 图文混排控件
 */
public class ImgTextCombineLayout extends RelativeLayout {

	public final int TAG_ID = R.string.tag;
	public static final String IMGEURL = "imgUrl";// 图片url
	public static final String CONTENT = "content";// 文字内容
	private static final int BITMAP_OK = 200;
	private static final int BITMAP_NULL = 404;

	private Context context;
	public EditTextShow editText;
	private ImgTextCallBack imgTextCallBack;
	private TextView imgUrlTextView, water_tv;
	public TextViewShow textview;
	private ImageView image;
	private ImageView image_del;
	private View view;
	private RelativeLayout layout;
	private int view_waith;
	private LinearLayout water_lin;

	public ImgTextCombineLayout(Context context) {
		super(context);
		this.context = context;
		view = LayoutInflater.from(context).inflate(R.layout.a_common_post_imgtext, null);
		setImgTextCallBack(null);
		initView();
		addView(view);
	}

	/**
	 * 设置接口回调
	 *
	 * @param imgTextCallBack
	 */
	public void setImgTextCallBack(ImgTextCallBack imgTextCallBack) {
		if (imgTextCallBack == null) {
			this.imgTextCallBack = new ImgTextCallBack() {
				@Override
				public void onFocusChange(EditTextShow editTextshow, boolean hasFocus, ImgTextCombineLayout layout) {
				}

				@Override
				public void onDelete(ImgTextCombineLayout layout) {
				}

				@Override
				public void onClick(ImgTextCombineLayout layout) {
				}

				@Override
				public void onImageClick(ImgTextCombineLayout layout) {
				}

				@Override
				public void initImgNull(ImgTextCombineLayout layout) {
				}

				@Override
				public int getWidth() {
					return 0;
				}
			};
		} else
			this.imgTextCallBack = imgTextCallBack;
	}

	/**
	 * 初始化view
	 */
	public void initView() {
		layout = (RelativeLayout) view.findViewById(R.id.image_rela);
		image = (ImageView) view.findViewById(R.id.image);
		editText = (EditTextShow) view.findViewById(R.id.editText);
		imgUrlTextView = (TextView) view.findViewById(R.id.text_gone);
		textview = (TextViewShow) view.findViewById(R.id.textview);
		image_del = (ImageView) view.findViewById(R.id.image_del);
		water_tv = (TextView) view.findViewById(R.id.water_tv);
		water_lin = (LinearLayout) view.findViewById(R.id.water_lin);
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				imgTextCallBack.onFocusChange(editText, hasFocus, ImgTextCombineLayout.this);
			}
		});

		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imgTextCallBack.onImageClick(ImgTextCombineLayout.this);
			}
		});
		// 输入框被点击设置当前操作ImgTextCombineLayout为当前
		editText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imgTextCallBack.onClick(ImgTextCombineLayout.this);
			}
		});
		// 删除图片功能
		view.findViewById(R.id.image_del).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imgTextCallBack.onClick(ImgTextCombineLayout.this);
				showDialog();
			}
		});
	}

	/**
	 * 设置图文
	 *
	 * @param str_tv
	 * @param isEditText true为edittext,false 为textview
	 */
	@SuppressLint({"HandlerLeak"})
	public void setImgText(String str_tv, String imgUrl, boolean isEditText) {
		setImgText(str_tv, imgUrl, isEditText, 0);
	}

	/**
	 * 更改文字大小
	 *
	 * @param str_tv
	 * @param imgUrl
	 * @param isEditText
	 * @param textSize
	 */
	public void setImgText(String str_tv, String imgUrl, boolean isEditText, float textSize) {

		// 对文字处理
		if (isEditText) {
			editText.setText(str_tv);
			if (textSize > 0)
				editText.setTextSize(textSize);
			editText.setVisibility(View.VISIBLE);
			textview.setVisibility(View.GONE);
		} else {
			editText.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(str_tv)) {
			ArrayList<Map<String, String>> array = UtilString.getListMapByJson(str_tv);
			if (array.size() <= 0 || TextUtils.isEmpty(array.get(0).get("")))
				textview.setVisibility(View.GONE);
			else {
				textview.setVisibility(View.VISIBLE);
			}
		} else
			textview.setVisibility(View.GONE);
		setImageView(imgUrl);
	}
	public void setSubjectStyle(){
		int dp_12= (int) context.getResources().getDimension(R.dimen.dp_12);
		int dp_14= (int) context.getResources().getDimension(R.dimen.dp_14);
		textview.setPadding(0, dp_12, 0, 0);
		layout.setPadding(0, dp_14, 0, 0);
	}

	/**
	 * 设置文字
	 *
	 * @param str_tv
	 */
	public void setTextView(String str_tv) {
		if (editText != null)
			editText.setText(str_tv);
	}

	/**
	 * 设置图片
	 *
	 * @param imgUrl
	 */
	@SuppressLint("HandlerLeak")
	public void setImageView(final String imgUrl) {
		// 图片处理
		view.findViewById(R.id.image_rela).setVisibility(View.GONE);
		image.setVisibility(View.GONE);
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case BITMAP_NULL:
						imgTextCallBack.initImgNull(ImgTextCombineLayout.this);
						break;
					case BITMAP_OK:
						Bitmap bmp = (Bitmap) msg.obj;
						if (bmp != null) {
							view.findViewById(R.id.image_rela).setVisibility(View.VISIBLE);
							image.setVisibility(View.VISIBLE);
							int newWaith = imgTextCallBack.getWidth() - (int) context.getResources().getDimension(R.dimen.dp_20) * 2;

							if (bmp != null) {
								int waith = newWaith;
								if (bmp.getWidth() <= newWaith)
									waith = 0;
//								UtilImage.setImgViewByWH(image, bmp, waith, 0, false);
								image.setImageBitmap(bmp);
								setDelImageView(newWaith, waith, bmp.getWidth());
								//显示水印
//							if(LoginManager.isLogin()){
//								water_tv.setText(LoginManager.userInfo.get("nickName"));
//							}
							}
						}
						break;
				}
			}
		};
		if (!TextUtils.isEmpty(imgUrl)) {
			imgUrlTextView.setText(imgUrl);
//			Log.i("imgUrl::", imgUrl);
			if (imgUrl.indexOf("http") == 0) {
				setHttpImageView(image, imgUrl);
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {
						int newWaith = imgTextCallBack.getWidth() - (int) context.getResources().getDimension(R.dimen.dp_15) * 2;
//						Log.i("imgUrl::", imgUrl);
						Bitmap bitmap = UtilImage.imgPathToBitmap(imgUrl, newWaith, 0, false, null);
						if (bitmap != null) {
							Message msg = new Message();
							msg.obj = bitmap;
							msg.what = BITMAP_OK;
							handler.sendMessage(msg);
						} else if (bitmap == null || (bitmap != null && bitmap.getWidth() <= 0)) {
							handler.sendEmptyMessage(BITMAP_NULL);
						}
					}
				}).start();
			}
		}
	}

	/**
	 * 设置当前删除按钮的位置
	 * w
	 *
	 * @param newWaith 当前界面可供显示的宽度
	 * @param waith    =newWaith时表示当前屏幕宽度小于图片宽度，=0时表示当前图片宽度小于屏幕宽度
	 * @param bmWaith  图片宽度
	 */
	private void setDelImageView(int newWaith, int waith, int bmWaith) {
		if (waith <= 0) {
			//对删除按钮显示
			int im_waith = (newWaith - bmWaith) / 2;
			int dp_40 = (int) context.getResources().getDimension(R.dimen.dp_40);
			int dp_8 = (int) context.getResources().getDimension(R.dimen.dp_8);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(dp_40, dp_40);
			layoutParams.setMargins(0, 0, im_waith, 0);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			image_del.setLayoutParams(layoutParams);
			image_del.setPadding(dp_8, dp_8, dp_8, dp_8);
			//水印
			RelativeLayout.LayoutParams layoutParams_water = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			int dp_11 = (int) context.getResources().getDimension(R.dimen.dp_11);
			layoutParams_water.setMargins(0, 0, im_waith, 0);
			layoutParams_water.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParams_water.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			water_lin.setLayoutParams(layoutParams_water);
			water_lin.setPadding(0, 0, dp_11, dp_8);

		}
	}

	/**
	 * 设置当前控件焦点
	 *
	 * @param isInput true 弹起键盘
	 */
	public void setEditTextFocus(boolean isInput) {
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
		// 手动弹出键盘
		if (isInput) {
			InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(editText, 0);
		}
	}

	/**
	 * 清除焦点
	 */
	public void setEditTextNoneFocus() {
		editText.clearFocus();
	}

	/**
	 * 插入图片
	 * <p>
	 * 图片路径数组
	 *
	 * @return 图文混排控件数组
	 */
	public String insertImg() {
		// 1、获取当前光标位置
		int selection = editText.getSelectionStart();
		String content = editText.getText().toString();
		String content_before = content.substring(0, selection);// 光标之前的文字
		String content_after = content.substring(selection, content.length());// 光标之后的文字
		editText.setText(content_before);

		return content_after;
	}

	/**
	 * 获取图片路径和文字
	 *
	 * @return Map<String,String> map：图片路径、文字
	 */
	public Map<String, String> getImgText() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(IMGEURL, imgUrlTextView.getText().toString());
		map.put(CONTENT, getUnicodeText(editText));
		return map;
	}

	/**
	 * 设置数据
	 *
	 * @param data
	 */
	public void editAdd(String data) {
		ArrayList<Map<String, String>> list = UtilString.getListMapByJson(data);
		int index = editText.getSelectionStart();
		editText.setFriends(index, list);
	}

	/**
	 * 控制水印
	 *
	 * @param markText
	 */
	public void setWaterMark(boolean isVisible, String markText) {
//		if(isVisible){//显示
//			water_lin.setVisibility(View.VISIBLE);
//			if(TextUtils.isEmpty(markText)&& LoginManager.isLogin()){
//				water_tv.setText(LoginManager.userInfo.get("nickName"));
//			}else{
//				water_tv.setText(markText);
//			}
//		}else{//不显示
//			water_lin.setVisibility(View.GONE);
//			
//		}
	}

	/**
	 * 替换所有<img>标签，转换成unicode编码,供发送到服务器
	 *
	 * @return
	 */
	private String getUnicodeText(EditTextShow edit) {
		String senStr = EmjParseMsgUtil.convertToMsg(context, edit.getEditableText());
		return senStr;
	}

	/**
	 * 展示删除dialog
	 */
	private void showDialog() {
		final Dialog dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window = dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message = (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText("确认要删除这张图片吗？");
		dialog_message.setTextColor(Color.parseColor("#333333"));
		TextView dialog_cancel = (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure = (TextView) window.findViewById(R.id.dialog_sure);
		dialog_sure.setTextColor(Color.parseColor("#333333"));
		dialog_cancel.setText("取消");
		dialog_sure.setText("确定");
		dialog_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				imgTextCallBack.onDelete(ImgTextCombineLayout.this);
				dialog.cancel();
			}
		});
		dialog.show();
	}

	/**
	 * 处理http图片
	 * @param imageview
	 * @param imgUrl
	 */
	private void setHttpImageView(ImageView imageview, String imgUrl) {
		//处理站位置的图片
		image.setVisibility(View.VISIBLE);
		view.findViewById(R.id.image_rela).setVisibility(View.VISIBLE);
		view.findViewById(R.id.image_del).setVisibility(View.GONE);
		view.findViewById(R.id.water_lin).setVisibility(View.GONE);
		InputStream is = getResources().openRawResource(R.drawable.i_nopic);
		Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
		image.setScaleType(ScaleType.CENTER_CROP);
		if (view_waith > 0)
			UtilImage.setImgViewByWH(image, bitmap, view_waith, view_waith * 2 / 3, true);
		else
			UtilImage.setImgViewByWH(image, bitmap, imgTextCallBack.getWidth(), imgTextCallBack.getWidth() * 2 / 3, true);
		setImg(imgUrl, imageview, 0);
		// 设置默认图
	}

	/**
	 * http设置图片
	 *
	 * @param img_url
	 * @param imageView
	 * @param roundImgPixels
	 */
	private void setImg(final String img_url, final ImageView imageView, final int roundImgPixels) {
		imageView.setClickable(true);
		if (img_url != null && img_url.length() < 10)
			return;
		imageView.setTag(TAG_ID, img_url);
		BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
				.load(img_url)
				.setImageRound(roundImgPixels)
				.build();
		if (bitmapRequest != null)
			bitmapRequest.into(new SubBitmapTarget() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
					ImageView img = null;
					if (imageView.getTag(TAG_ID).equals(img_url))
						img = imageView;
					if (img != null && bitmap != null) {
						// 图片圆角和宽高适应
						imageView.setScaleType(ScaleType.CENTER_CROP);
						if (view_waith > 0)
							UtilImage.setImgViewByWH(imageView, bitmap, view_waith, 0, false);
						else if (roundImgPixels == 0)
							UtilImage.setImgViewByWH(imageView, bitmap, ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_10) * 2, 0, false);
						else
							UtilImage.setImgViewByWH(imageView, bitmap, 0, 0, false);
//						imageView.setImageBitmap(bitmap);
						imageView.setVisibility(View.VISIBLE);
					}
				}
			});
		view.findViewById(R.id.image_rela).setVisibility(View.VISIBLE);
		view.findViewById(R.id.image_del).setVisibility(View.GONE);
		view.findViewById(R.id.water_lin).setVisibility(View.GONE);
	}

	/**
	 * 接口回调
	 */
	public interface ImgTextCallBack {
		/** 删除当前控件 */
		public void onDelete(ImgTextCombineLayout layout);

		/** 当editTextshow焦点改变 */
		public void onFocusChange(EditTextShow editTextshow, boolean hasFocus, ImgTextCombineLayout layout);

		/** 当editTextshow被点击 */
		public void onClick(ImgTextCombineLayout layout);

		/** 当前屏幕宽度 */
		public int getWidth();

		/** 图片被点击 */
		public void onImageClick(ImgTextCombineLayout layout);

		/** 加载图片为null */
		public void initImgNull(ImgTextCombineLayout layout);
	}

	/**
	 * 设置贴子详情页图片宽度
	 */
	public void setViewWaith() {
		view_waith = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_76);
	}

}
