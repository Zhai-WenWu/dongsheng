package amodule.main.view.circle;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.logic.XHClick;
import acore.tools.Tools;
import third.ad.AdParent;
import third.ad.AdsShow;
import third.ad.BannerAd;
import third.ad.tools.AdPlayIdConfig;

/**
 * PackageName : amodule.main.view.circle
 * Created by MrTrying on 2016/8/24 17:43.
 * E_mail : ztanzeyu@gmail.com
 */
public class CircleHeaderAD extends LinearLayout {
	private String stiaticID = "";

	public CircleHeaderAD(Context context) {
		this(context,null,0);
	}

	public CircleHeaderAD(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CircleHeaderAD(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.a_circle_header_ad,this);
		setOrientation(VERTICAL);
	}

	//生活圈首页顶部
	public AdsShow[] init(Activity activity){
		RelativeLayout advert_rela_banner = (RelativeLayout) findViewById(R.id.circle_ad_banner_layout);
		BannerAd bannerAdBurden = new BannerAd(activity,"community_top", advert_rela_banner);
		AdParent[] adsBurdenParent = { bannerAdBurden};
		for (AdParent adParent:adsBurdenParent){
			adParent.setOnAdClick(new AdParent.AdClickListener() {
				@Override
				public void onAdClick() {
					XHClick.mapStat(getContext(),stiaticID,"banner位","");
				}
			});
		}
		AdsShow adBurden = new AdsShow(adsBurdenParent, AdPlayIdConfig.MAIN_CIRCLE_TITLE);
		AdsShow[] mAds = new AdsShow[]{adBurden};
		return mAds;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(h != 0 && h != oldh){
			post(new Runnable() {
				@Override
				public void run() {
					findViewById(R.id.blank_view).setVisibility(View.VISIBLE);
				}
			});
		}else if(h == Tools.getDimen(getContext(),R.dimen.dp_10)){
			findViewById(R.id.blank_view).setVisibility(View.GONE);
		}
	}

	public String getStiaticID() {
		return stiaticID;
	}

	public void setStiaticID(String stiaticID) {
		this.stiaticID = stiaticID;
	}
}
