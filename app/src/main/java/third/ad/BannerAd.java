package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * banner广告，策略：服务端返回是否显示banner广告，若显示，则banner信息随着返回
 * @author FangRuijiao
 */
public class BannerAd{
	private Activity mAct;
	private RelativeLayout mLayoutParent;
	private ImageViewVideo mImgViewSingle;
	private String mClickUrl;
	/** banner广告数据 */
	/** banner广告无需刷新，次标记标识是否已经加载过 */
	private boolean mIsHasShow = false;
	private OnBannerListener mListener;
	
	private String mImgKey = "appImg";

	//页面来源
	private String mFrom;

	public int marginLeft = 0,marginRight = 0;
	private String StatisticKey;
	private String ad_show;//展示一级统计
	private String ad_click;//点击一级统一
	private String twoData;//二级统计
	private String key="xh";
	
	/**
	 * @param act
	 * @param layoutParent
	 */
	public BannerAd(Activity act,String from,RelativeLayout layoutParent){
		mAct = act;
		mFrom = from;
		this.StatisticKey=from;
		mLayoutParent = layoutParent;
		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
	}
	public BannerAd(Activity act,String from,RelativeLayout layoutParent,boolean isMain){
		mAct = act;
		mFrom = from;
		this.StatisticKey=from;
		mLayoutParent = layoutParent;
		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
	}
	public BannerAd(Activity act,String from,RelativeLayout layoutParent,OnBannerListener listener){
		mAct = act;
		mFrom = from;
		this.StatisticKey=from;
		mLayoutParent = layoutParent;
		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
		mListener = listener;
	}

	public void onShowAd(Map<String,String> map) {
		if(!mIsHasShow){
			mIsHasShow = true;
			setActivityData(map);
		}
	}
	
	private void setActivityData(Map<String,String> map){
			if(mListener != null) mListener.onShowAd();
			String mImgUrl = map.get("");
			mClickUrl = map.get("url");
			//设置活动图
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(XHApplication.in())
				.load(mImgUrl)
				.setRequestListener(new RequestListener<GlideUrl, Bitmap>() {
					@Override
					public boolean onResourceReady(Bitmap arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3, boolean arg4) {
						return false;
					}

					@Override
					public boolean onException(Exception arg0, GlideUrl arg1, Target<Bitmap> arg2, boolean arg3) {
						mImgViewSingle.setVisibility(View.GONE);
						return false;
					}
				})
				.build();
			if(bitmapRequest != null)
				bitmapRequest.into(new SubBitmapTarget() {
					@Override
					public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
						int imgViewWidth = mImgViewSingle.getWidth() > 0 ? mImgViewSingle.getWidth() : ToolsDevice.getWindowPx(mAct).widthPixels - marginLeft - marginRight;
//								//Log.i("FRJ","imgViewWidth:" + imgViewWidth);
						int imgHeight = imgViewWidth * bitmap.getHeight() / bitmap.getWidth();
						mImgViewSingle.setScaleType(ImageView.ScaleType.FIT_XY);
						UtilImage.setImgViewByWH(mImgViewSingle, bitmap, imgViewWidth, imgHeight, true);

						mImgViewSingle.setVisibility(View.VISIBLE);
						mLayoutParent.setVisibility(View.VISIBLE);
						if(mListener != null)
							mListener.onImgShow(imgHeight);
					}
				});
				mLayoutParent.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onAdClick();
					}
				});
	}

	public void onAdClick(){
//		onAdClick(mFrom,TONGJI_BANNER, mId);
		AppCommon.openUrl(mAct, mClickUrl, true);
	}
	
	public interface OnBannerListener{
		public void onShowAd();
		public void onImgShow(int imgH);
	}

}
