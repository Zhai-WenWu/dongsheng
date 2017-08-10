package third.ad;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.Tools;
import third.ad.tools.AdConfigTools;
/**
 * 广告父类
 * @author FangRuijiao
 */
public abstract class AdParent {
	public static final String ADKEY_GDT = "isGdt";
	public static final String ADKEY_JD = "isJD";
	public static final String ADKEY_BANNER = "isBanner";
	
	public static final String TONGJI_GDT = "gdt";
	public static final String TONGJI_JD = "jingdong";
	public static final String TONGJI_BANNER = "banner";
	public static final String TONGJI_TX_API = "tencent_api";

	/**该广告的类型*/
	protected String mAdKey = "";
	protected AdClickListener mListener;
	protected String mAdPlayId = "";

	/**
	 * 设置此广告是否需要：当出现在屏幕内后才显示
	 */
	public boolean isNeedOnScreen = false;

	/**
	 * 在父类先判断接口中广告是否显示，若显示再在子类中判断是否有数据
	 * @param adPlayId : 广告体id
	 * @param listener
	 */
	public boolean isShowAd(String adPlayId,AdIsShowListener listener){
		mAdPlayId = adPlayId;
		initAdKey();
		return AdConfigTools.getInstance().isShowAd(adPlayId, mAdKey);
	}
	/**
	 * 广告曝光，onResume时调用
	 */
	public abstract void onResumeAd();
	/**
	 * 广告不显示：onPause时调用
	 */
	public abstract void onPsuseAd();
	
	public abstract void onDestroyAd();
	
	public void initAdKey(){
		if(this instanceof GdtAdNew){
			mAdKey = ADKEY_GDT;
		}else if(this instanceof BannerAd){
			mAdKey = ADKEY_BANNER;
		}else if(this instanceof AdeazAdCreate){
			mAdKey = ADKEY_JD;
		}else if(this instanceof TencenApiAd){
			mAdKey = ADKEY_JD;
		}
	}
	
	public void setOnAdClick(AdClickListener listener){
		mListener = listener;
	}
	
	protected void onAdClick(String from,String channel){
		postTongji(channel, "0", "click");
		//umeng的统计
		AdConfigTools.getInstance().onAdClick(XHApplication.in(),channel, from, "");
		//自己网站的统计
		AdConfigTools.getInstance().clickAds(mAdPlayId,channel,"0");
		if(mListener != null) mListener.onAdClick();
	}
	protected void onAdShow(String from,String channel){
		postTongji(channel, "0", "show");
		AdConfigTools.getInstance().onAdShow(XHApplication.in(),channel,from,"");
	}
	protected void onAdClick(String from,String channel,String bannerId){
		postTongji(channel, bannerId, "click");
		AdConfigTools.getInstance().onAdClick(XHApplication.in(),channel,from,"");
		AdConfigTools.getInstance().clickAds(mAdPlayId,channel,bannerId);
		if(mListener != null) mListener.onAdClick();
	}
	protected void onAdShow(String from,String channel,String bannerId){
		postTongji(channel, bannerId, "show");
		AdConfigTools.getInstance().onAdShow(XHApplication.in(),channel,from,"");
	}
	
	private void postTongji(String channel,String bannerId,String event){
		AdConfigTools.getInstance().postTongji(mAdPlayId, channel, bannerId, event, "普通广告位");
	}

	/**
	 * 用于异步返回广告是否显示
	 * @author FangRuijiao
	 */
	public interface AdIsShowListener{
		public void onIsShowAdCallback(AdParent adParent , boolean isShow);
	}
	public interface AdClickListener{
		public void onAdClick();
	}

	public static abstract class AdListener{
		public void onAdCreate(){

		}
		public void onAdOver(View adView, Bitmap msg, int tag){

		}
		public int getImgHeight(Context con){
			return Tools.getDimen(con, R.dimen.dp_52);
		}
	}

	/**
	 * 广告展示统计
	 */
	protected void onAdShow(String oneLevel,String twoLevel,String threeLevel,String key,String mId){
		//自己网站上的统计
		postTongji(key, mId, "show");
		XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
	}

	/**
	 * 广告点击统计
	 * @param oneLevel
	 * @param twoLevel
	 * @param threeLevel
	 * @param key
     * @param mId
     */
	protected void onAdClick(String oneLevel,String twoLevel,String threeLevel,String key,String mId){
		postTongji(key, mId, "click");
		//umeng的统计
		XHClick.mapStat(XHApplication.in(), oneLevel, twoLevel, threeLevel);
			//其他统计
		AdConfigTools.getInstance().clickAds(mAdPlayId,key,mId);
	}

}
