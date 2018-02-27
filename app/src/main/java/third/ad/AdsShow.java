package third.ad;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.StringManager;
import third.ad.AdParent.AdIsShowListener;
import third.ad.db.XHAdSqlite;
import third.ad.db.bean.AdBean;

/**
 * 用于创建广告，因需求：一个位置多个广告，具体显示哪个，按优先级看参数判断
 * @author FangRuijiao
 */
public class AdsShow {
	
	/** 一共含有的广告数 */
	private AdParent[] mAds;
	/** 当前现实广告 */
	private AdParent mAd;
	private String mAdPlayId;
	/**是否已经显示*/
	private boolean mAlreadyShow = false;
	private boolean isOnScreent = false;
	/**
	 * @param ads : 此位置一共含有几个广告
	 */
	public AdsShow(AdParent[] ads,String adPlayId){
		mAds = sortAds(ads,adPlayId);
//		mAds=ads;
		mAdPlayId = adPlayId;
	}
	
	public void onResumeAd(){
		if(mAds.length > 0)
			initAdObject(0);
	}
	
	public void onPauseAd(){
		if(mAd != null){
			mAd.onPsuseAd();
		}
	}
	
	public void isOnScreen(boolean flag){
		if(flag && !mAlreadyShow){
			if(mAd != null){
				isOnScreent = true;
				mAd.onResumeAd();
				mAlreadyShow = true;
			}
		}
	}
	
	private void initAdObject(final int index){
		if(index < mAds.length){
			mAds[index].isShowAd(mAdPlayId, new AdIsShowListener() {
				@Override
				public void onIsShowAdCallback(AdParent adParent , boolean isShow) {
					if(isShow){
						mAd = mAds[index];
						if(mAd.isNeedOnScreen){
							if(isOnScreent){
								mAlreadyShow = true;
								mAd.onResumeAd();
							}
						}else{
							mAlreadyShow = true;
							mAd.onResumeAd();
						}
					} else {
						initAdObject(index + 1);
					}
				}
			});
		}
	}

	/**
	 * 对广告集合进行排序
	 * @param ads
	 * @return
     */
	private AdParent[] sortAds(AdParent[] ads,String adPlayId){
		XHAdSqlite adSqlite = XHAdSqlite.newInstance(XHApplication.in());
		AdBean adBean = adSqlite.getAdConfig(adPlayId);
		int lenght = ads.length;
		if (lenght == 1) {
			return ads;
		}
		if(adBean == null || TextUtils.isEmpty(adBean.adConfig)){
			return ads;
		}
		ArrayList<AdParent> adParents= new ArrayList<>();
		Map<String, String> data = StringManager.getFirstMap(adBean.adConfig);
		final String[] keys = {"1","2","3","4","5"};
		for(String key:keys){
			if(data.containsKey(key)){
				adParents = handlerStringData(data.get(key), adParents, ads);
			}
		}

		return  adParents.toArray(new AdParent[adParents.size()]);
	}

	/**
	 * 添加数据
	 * @param data
	 * @param adParents
	 * @param ads
     * @return
     */
	private ArrayList<AdParent> handlerStringData(String data,ArrayList<AdParent> adParents,AdParent[] ads){
		int lenght= ads.length;
		Map<String,String> map_ad=StringManager.getFirstMap(data);
		if(map_ad.get("open").equals("2")){//首先必须是打开iad数据
			if(map_ad.get("type").equals("api")){//判断当前类型是什么进行添加
				for(int i=0;i<lenght;i++){
					if(ads[i] instanceof TencenApiAd){
						adParents.add(ads[i]);
					}
				}
			}else if(map_ad.get("type").equals("personal")){
				for(int i=0;i<lenght;i++){
					if(ads[i] instanceof BannerAd){
						adParents.add(ads[i]);
					}
				}
			}
		}
		return adParents;
	}
}
