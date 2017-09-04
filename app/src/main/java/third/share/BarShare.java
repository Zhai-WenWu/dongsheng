package third.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.tools.FileManager;
import amodule.main.view.SharePopupWindow;
import third.share.activity.ShareNewActivity;
import third.share.tools.ShareTools;

public class BarShare {
	
	public static String IMG_TYPE_WEB="web";
	public static String IMG_TYPE_RES="res";
	public static String IMG_TYPE_LOC="loc";
	
	public String appID;
	private Context mContext;
	private String mTitle,mType,mClickUrl,mContent,mImgUrl,mFrom,mParent;
	
	public BarShare(Context context,String from,String parent){
		mContext = context;
		mFrom = from;
		mParent = parent;
	}
	
	/**
	 * @param type web,网络图片;res:资源文件;loc:本地图片;
	 * @param title 分享标题
	 * @param content 分享内容
	 * @param imgUrl 分享图片
	 * @param contentUrl 点击分享的连接指向
	 */
	public void setShare(String type,String title,String content,String imgUrl,String contentUrl) {
		mTitle = title;
		mType = type;
		mClickUrl = contentUrl;
		mContent= content;
		mImgUrl = imgUrl;
	}
	
	/**
	 * @param title 分享标题
	 * @param content 分享内容
	 * @param bitmap 分享图片
	 * @param contentUrl 点击分享的连接指向
	 */
	public void setShare(String title,String content,Bitmap bitmap,String contentUrl) {
		mTitle = title;
		mType = ShareTools.IMG_TYPE_LOC;
		mClickUrl = contentUrl;
		mContent= content;
		mImgUrl = ShareTools.getBarShare(mContext).saveDrawable(bitmap,FileManager.save_cache + "/share_" + System.currentTimeMillis() + ".png");
		if(mImgUrl == null){
			mType = ShareTools.IMG_TYPE_RES;
			mImgUrl = "" + R.drawable.share_launcher;
		}
	}
	
	public void openShare(){
		Intent intent = new Intent(mContext,ShareNewActivity.class);
		intent.putExtra("type", mType);
		intent.putExtra("title", mTitle);
		intent.putExtra("clickUrl", mClickUrl);
		intent.putExtra("content", mContent);
		intent.putExtra("imgUrl", mImgUrl);
		intent.putExtra("from", mFrom);
		intent.putExtra("parent", mParent);
		mContext.startActivity(intent);
	}

	public void openSharePopup() {
		Map<String, String> dataMap = new HashMap<>();
		dataMap.put("type", mType);
		dataMap.put("title", mTitle);
		dataMap.put("clickUrl", mClickUrl);
		dataMap.put("content", mContent);
		dataMap.put("imgUrl", mImgUrl);
		dataMap.put("from", mFrom);
		dataMap.put("parent", mParent);
		new SharePopupWindow((Activity) mContext, dataMap).showShare();
	}
}
