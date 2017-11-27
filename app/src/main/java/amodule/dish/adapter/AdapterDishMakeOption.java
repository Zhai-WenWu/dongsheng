package amodule.dish.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DragGridBaseAdapter;
import amodule.dish.activity.upload.UploadDishActivity;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

import static com.xiangha.R.id.iv_makes_back2;

/**
 * @author xiaanming
 * @blog http://blog.csdn.net/xiaanming
 */
public class AdapterDishMakeOption extends BaseAdapter implements DragGridBaseAdapter {

	private Context mCon;
	private List<Map<String, String>> list;
	private LayoutInflater mInflater;
	private int mHidePosition = -1;

	public boolean isEdit = false;
	private boolean isVideoMake = false;

	public AdapterDishMakeOption(Context context, List<Map<String, String>> list,boolean isVideoMake) {
		mCon = context;
		this.list = list;
		mInflater = LayoutInflater.from(context);
		this.isVideoMake = isVideoMake;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 由于复用convertView导致某些item消失了，所以这里不复用item，
	 */
	@SuppressLint("ViewHolder")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.a_dish_upload_make_item, null);
			int[] viewId = new int[]{R.id.dish_up_make_title, R.id.dish_up_make_title_show, R.id.iv_makes, R.id.tv_makeStep
					, R.id.iv_makeDele, R.id.tv_make_path, R.id.dish_up_speech_make_title
					, R.id.iv_makeMove, R.id.dish_up_make_title_rl,R.id.iv_makes_back0,R.id.iv_makes_back1, iv_makes_back2};
			viewHolder.setView(convertView,isVideoMake,viewId);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.setData(list.get(position));
		ImageView ivMakeDele = (ImageView) convertView.findViewById(R.id.iv_makeDele);
		ivMakeDele.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final DialogManager dialogManager = new DialogManager(mCon);
				dialogManager.createDialog(new ViewManager(dialogManager)
						.setView(new TitleMessageView(mCon).setText("确定删除本步骤吗？"))
						.setView(new HButtonView(mCon)
								.setNegativeText("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialogManager.cancel();
									}
								})
								.setPositiveText("删除", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										isEdit = true;
										onDeleteStepView(position);
										dialogManager.cancel();
									}
								}))).show();
			}
		});
		if (position == mHidePosition) {
			convertView.setVisibility(View.INVISIBLE);
		} else {
			convertView.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	/**
	 * 点击删除步骤
	 *
	 * @param position
	 *
	 */
	private void onDeleteStepView(int position) {
		XHClick.mapStat(mCon, UploadDishActivity.STATISTICS_MODIFY_MAKE_ID, "删除步骤", "");
		list.remove(position);
		notifiyView();
	}


	@Override
	public void reorderItems(int oldPosition, int newPosition) {
		isEdit = true;
		Map<String, String> temp = list.get(oldPosition);
		if (oldPosition < newPosition) {
			for (int i = oldPosition; i < newPosition; i++) {
				Collections.swap(list, i, i + 1);
			}
		} else if (oldPosition > newPosition) {
			for (int i = oldPosition; i > newPosition; i--) {
				Collections.swap(list, i, i - 1);
			}
		}
		list.set(newPosition, temp);
		XHClick.mapStat(mCon, UploadDishActivity.STATISTICS_MODIFY_MAKE_ID, "上下移动步骤", "");
	}

	@Override
	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition;
		notifiyView();
	}

	public List<Map<String, String>> getData() {
		return list;
	}

	private void notifiyView() {
		Map<String, String> map;
		for (int i = 0; i < list.size(); i++) {
			map = list.get(i);
			map.put("makesStep", String.valueOf(i + 1));
		}
		notifyDataSetChanged();
	}

	private class ViewHolder {
		private View view;
		private EditText etMakeTitle;
		private TextView etMakeTitleShow;
		private ImageView iv_makes;
		private TextView tv_step;
		private ImageView ivMakeDele;
		private TextView tv_make_path;
		private RelativeLayout rlTitle;
		private ImageView iv_makes_back1;
		private FrameLayout iv_makes_back0;
		private TextView iv_makes_back2;

		public ViewHolder(){
		}

		public void setView(View view,boolean isVideoMake,int... param) {
			this.view = view;
			etMakeTitle = (EditText) view.findViewById(param[0]);
			etMakeTitleShow = (TextView) view.findViewById(param[1]);
			iv_makes = (ImageView) view.findViewById(param[2]);
			tv_step = (TextView) view.findViewById(param[3]);
			ivMakeDele = (ImageView) view.findViewById(param[4]);
			tv_make_path = (TextView) view.findViewById(param[5]);
			View speechView = view.findViewById(param[6]);
			View moveView = view.findViewById(param[7]);
			rlTitle = (RelativeLayout) view.findViewById(param[8]);
			if(isVideoMake){
				iv_makes_back0 = (FrameLayout) view.findViewById(param[9]);
				iv_makes_back1 = (ImageView) view.findViewById(param[10]);
				iv_makes_back2 = (TextView) view.findViewById(param[11]);

				RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) iv_makes_back0.getLayoutParams();
				layoutParams1.width = Tools.getDimen(mCon,R.dimen.dp_160);
				layoutParams1.height = Tools.getDimen(mCon,R.dimen.dp_90);
				iv_makes_back1.setImageResource(R.drawable.z_caipu_upload_video_bigpic);
				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iv_makes_back1.getLayoutParams();
				layoutParams.width = Tools.getDimen(mCon,R.dimen.dp_27);
				layoutParams.height = Tools.getDimen(mCon,R.dimen.dp_31);
				layoutParams.setMargins(0,Tools.getDimen(mCon,R.dimen.dp_20),0,0);
				iv_makes_back2.setTextSize(Tools.getDimenSp(mCon,R.dimen.sp_13));
				FrameLayout.LayoutParams layoutParams_iv_makes_back2 = (FrameLayout.LayoutParams) iv_makes_back2.getLayoutParams();
				layoutParams_iv_makes_back2.setMargins(0,Tools.getDimen(mCon,R.dimen.dp_57),0,0);
			}

			etMakeTitle.setVisibility(View.GONE);
			etMakeTitleShow.setVisibility(View.VISIBLE);
			speechView.setVisibility(View.GONE);
			moveView.setVisibility(View.VISIBLE);
			ivMakeDele.setVisibility(View.VISIBLE);
			rlTitle.setBackgroundDrawable(null);
			tv_step.setBackgroundDrawable(null);
		}

		public void setData(Map<String, String> map) {
			etMakeTitleShow.setText(map.get("makesInfo"));
			tv_step.setText(map.get("makesStep"));
			String path = map.get("makesImg");
			setImageView(view, iv_makes, tv_make_path, path);
			restoreMakeView(iv_makes, tv_make_path, path);
		}

		/**
		 * 界面上所有的imageView操作;
		 * @param imageView
		 * @param imgUrl    图片url
		 */
		private void setImageView(final View view, final ImageView imageView, final TextView tv_make_path, final String imgUrl) {
			// 根据类型和位序来设置图片
			if (!TextUtils.isEmpty(imgUrl)) {
//				if (imageView.getTag() != null && imageView.getTag().equals(imgUrl))
//					return;
				imageView.setTag(imgUrl);
				imageView.setImageResource(R.drawable.bg_round_user_icon);

				if (imgUrl.indexOf("http") == 0) {
					tv_make_path.setText(imgUrl);
					BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(view.getContext())
							.load(imgUrl)
							.build();
					if (bitmapRequest != null)
						bitmapRequest.into(getTarget(view, imageView, 160, 0));
				} else {
					final int wdp = ToolsDevice.dp2px(mCon, 160);
					final Handler handler = new Handler() {

						@Override
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							ImageView img = null;
							Bitmap bmp = (Bitmap) msg.obj;
							if (imageView.getTag().equals(imgUrl))
								img = imageView;
							if (bmp != null && img != null) {
								tv_make_path.setText(imgUrl);
								setMakeBackGone(view);
								UtilImage.setImgViewByWH(imageView, bmp, wdp, 0, false);
							}
						}

					};
					new Thread(new Runnable() {

						@Override
						public void run() {
							Bitmap bmp = UtilImage.imgPathToBitmap(imgUrl, wdp, 0, false, null);
							Message msg = new Message();
							msg.obj = bmp;
							if(msg!=null)
								handler.sendMessage(msg);

						}
					}).start();
				}
			}
		}

		private SubBitmapTarget getTarget(final View view, final ImageView v, final int width_dp, final int height_dp) {
			return new SubBitmapTarget() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
					setMakeBackGone(view);
					ImageView img = v;
					if (img != null && bitmap != null) {
						UtilImage.setImgViewByWH(img, bitmap, ToolsDevice.dp2px(mCon, width_dp), height_dp, false);
					}
				}
			};
		}

		private void setMakeBackGone(View view) {
			view.findViewById(R.id.iv_makes_back0).setBackgroundColor(0xF5F5F5);
			view.findViewById(R.id.iv_makes_back1).setVisibility(View.GONE);
			view.findViewById(R.id.iv_makes_back2).setVisibility(View.GONE);
			view.findViewById(R.id.iv_makes).setVisibility(View.VISIBLE);
		}

		private void restoreMakeView(ImageView iv, TextView tv_path, String path) {
			view.findViewById(R.id.iv_makes).setVisibility(View.GONE);
		}
	}


}
