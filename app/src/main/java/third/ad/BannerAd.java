package third.ad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
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
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import acore.widget.ScrollLinearListLayout;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;

/**
 * banner广告，策略：服务端返回是否显示banner广告，若显示，则banner信息随着返回
 * @author FangRuijiao
 */
public class BannerAd extends AdParent{
	private Activity mAct;
	private RelativeLayout mLayoutParent;
	private ImageViewVideo mImgViewSingle/*,mImgView,mCurrentImgView*/;
//	private TextView mTitleView,mContentView;
	/** banner广告数据 */
	private String mImgUrl,mClickUrl,mId;
	/** banner广告无需刷新，次标记标识是否已经加载过 */
	private boolean mIsHasShow = false;
	private OnBannerListener mListener;
	
	private String mImgKey = "appImg";
	private boolean mIsMain = false;
	
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
		getStiaticsData();
//		mImgView = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv);
//		mTitleView = (TextView)mLayoutParent.findViewById(R.id.ad_banner_item_title);
//		mContentView = (TextView)mLayoutParent.findViewById(R.id.ad_banner_item_content);
	}
	public BannerAd(Activity act,String from,RelativeLayout layoutParent,boolean isMain){
		mAct = act;
		mFrom = from;
		this.StatisticKey=from;
		mLayoutParent = layoutParent;
		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
		mIsMain = isMain;
		getStiaticsData();
	}
	public BannerAd(Activity act,String from,RelativeLayout layoutParent,OnBannerListener listener){
		mAct = act;
		mFrom = from;
		this.StatisticKey=from;
		mLayoutParent = layoutParent;
		mImgViewSingle = (ImageViewVideo)mLayoutParent.findViewById(R.id.ad_banner_item_iv_single);
		mListener = listener;
		getStiaticsData();
	}
	
	@Override
	public boolean isShowAd(String adPlayId,AdIsShowListener listener) {
		boolean isShow = true;
			String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
			ArrayList<Map<String, String>> list = StringManager.getListMapByJson(data);
			Map<String, String> temp = new HashMap<>();
			if(list != null && list.size() > 0){
				temp = list.get(0);
				list = StringManager.getListMapByJson(temp.get(adPlayId));
				if(list != null && list.size() > 0){
					temp = list.get(0);
				}else{
					temp = new HashMap<String, String>();
				}
			}
			ArrayList<Map<String, String>> array = UtilString.getListMapByJson(temp.get("banner"));
			if(array == null || array.size() == 0) {
				isShow = false;
			}else if(!LoginManager.isShowAd()&&array.get(0).containsKey("adType")&&!"1".equals(array.get(0).get("adType"))){//1活动，2广告,美食家只显示活动
				isShow = false;
			}else{
				Map<String, String> map = array.get(0);
				String imgs = map.get("imgs");
				if(TextUtils.isEmpty(imgs)){
					isShow = false;
				}else{
					array = UtilString.getListMapByJson(imgs);
					if(array != null && array.size() > 0) {
						mImgUrl = array.get(0).get(mImgKey);
						if(TextUtils.isEmpty(mImgUrl)){
							isShow = false;
						}else{
							mId = map.get("id");
							mClickUrl = map.get("url");
						}
					}
				}
			}
		listener.onIsShowAdCallback(this,isShow);
		return isShow;
	}

	@Override
	public void onResumeAd() {
		if(!mIsHasShow){
			mIsHasShow = true;
			setActivityData();
		}
	}
	
	private void setActivityData(){
//			onAdShow(mFrom,TONGJI_BANNER , mId);
			onAdShow(ad_show,twoData,key,key,mId);//更改统计
			if(mListener != null) mListener.onShowAd();
//			String name = mData.get("name");
//			String content = mData.get("subhead");
//			//Log.i("FRJ","name:" + name + ";content:" + content);
//			nameAndContentAllIsNull = true;
//			mCurrentImgView = mImgView;
//			if(!TextUtils.isEmpty(name)){
//				mTitleView.setText(name);
//				mTitleView.setVisibility(View.VISIBLE);
//				nameAndContentAllIsNull = false;
//			}
//			if(!TextUtils.isEmpty(content)){
//				mContentView.setText(content);
//				mContentView.setVisibility(View.VISIBLE);
//				nameAndContentAllIsNull = false;
//			}
//			if(nameAndContentAllIsNull) mCurrentImgView = mImgViewSingle;
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
			if(mIsMain){
				mLayoutParent.setOnClickListener(ScrollLinearListLayout.getOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onAdClick();
					}
				}));
			}else{
				mLayoutParent.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onAdClick();
					}
				});
			}
	}

	public void onAdClick(){
//		onAdClick(mFrom,TONGJI_BANNER, mId);
		onAdClick(ad_click,twoData,key,key,mId);
		AppCommon.openUrl(mAct, mClickUrl, true);
	}
	
	public interface OnBannerListener{
		public void onShowAd();
		public void onImgShow(int imgH);
	}

	@Override
	public void onPsuseAd() {

	}
	@Override
	public void onDestroyAd() {

	}
	/**
	 * 获取广告统计层级数据
	 */
	private void getStiaticsData(){
		String msg= FileManager.getFromAssets(mAct, "adStatistics");
		ArrayList<Map<String,String>> listmap = StringManager.getListMapByJson(msg);
		if(listmap.get(0).containsKey(StatisticKey)){
			ArrayList<Map<String,String>> stiaticsList= StringManager.getListMapByJson(listmap.get(0).get(StatisticKey));
			ad_show = stiaticsList.get(0).get("show");
			ad_click = stiaticsList.get(0).get("click");
			twoData =stiaticsList.get(0).get("twoData");
		}
	}

}
