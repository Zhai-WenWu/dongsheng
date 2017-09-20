/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

/**
 * 随处调用分享.
 * @author intBird 20140213.
 * 
 */
package third.share.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.xiangha.R;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ImgManager;
import acore.tools.ObserverManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import xh.basic.tool.UtilFile;

public class ShareTools {
	private static ShareTools shareTools = null;
	private static Context mContext;
	private OnekeyShare oks;

	public static final String QQ_ZONE = QZone.NAME;
	public static final String QQ_NAME = QQ.NAME;
	public static final String WEI_XIN = Wechat.NAME;
	public static final String WEI_QUAN = WechatMoments.NAME;
	public static final String SINA_NAME = SinaWeibo.NAME;
	public static final String SHORT_MESSAGE = ShortMessage.NAME;
	public static final String LINK_COPY = "link_copy";
	
	public static String IMG_TYPE_WEB="web";
	public static String IMG_TYPE_RES="res";
	public static String IMG_TYPE_LOC="loc";
	
	public static String mFrom ="",mParent = "",mClickUrl="";

	public static ShareTools getBarShare(Context act) {
		if (shareTools == null) {
			shareTools = new ShareTools();
//			ShareSDK.initSDK(act);
		}
		mContext = act;
		return shareTools;
	}
	
	public void showSharePlatform(String title, String content,String types,
			String img, final String clickUrl, String platform,String from,String parent,boolean isShowBeginToast) {
		starEvent("a_share400", mParent,mFrom);
		mClickUrl = clickUrl + "";
		mFrom = from + "";
		mParent = parent + "";
		if (platform == LINK_COPY && !TextUtils.isEmpty(clickUrl)) {
			XHClick.onEvent(mContext, "a_share_click", "拷贝");
			Tools.inputToClipboard(mContext,clickUrl);
			Toast.makeText(mContext, "链接已复制", Toast.LENGTH_SHORT).show();
			return;
		}
		String[] pf = getPlatform(platform);
		XHClick.onEvent(mContext, "a_share_click", pf[0]);
		if(isShowBeginToast) Toast.makeText(mContext, "正在分享", Toast.LENGTH_LONG).show();
		String imgUrl = "",imgPath = "";
		if(img == null || img.length()==0){
			types=IMG_TYPE_RES;
			img=R.drawable.share_launcher+"";
		}
		if(types.equals(IMG_TYPE_WEB)){
			imgUrl = img;
			if (imgUrl != null && imgUrl.endsWith(".webp"))
				imgUrl = imgUrl.replace(".webp", "");
		}
		else if(types.equals(IMG_TYPE_RES)){
			imgPath = drawableToPath(img);
		}
		else if(types.equals(IMG_TYPE_LOC)){
			imgPath = img;
		}
		if(content.length() < 1){
			content = " ";
		}
		if(platform.equals(SINA_NAME)){
			content = content + clickUrl;
		}
		if(platform.equals(SHORT_MESSAGE)){
			content = title + content + clickUrl;
//			title = " ";
		}
		oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		//是否直接分享
		oks.setSilent(true);
		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(title);
		oks.setTitleUrl(clickUrl);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(content);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(imgPath); // 确保SDcard下面存在此张图片
		oks.setImageUrl(imgUrl);
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(clickUrl);
		
		oks.setCallback(new PlatformActionListener() {

			@Override
			public void onError(Platform plf, int arg1, Throwable arg2) {
				arg2.printStackTrace();
				Message msg = new Message();
				msg.what = SHARE_ERROR;
				msg.obj = plf.getName();
				shareHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(Platform plf, int arg1,HashMap<String, Object> arg2) {
				Message msg = new Message();
				msg.what = SHARE_OK;
				msg.obj = plf.getName();
				shareHandler.sendMessage(msg);
			}

			@Override
			public void onCancel(Platform plf, int arg1) {
				Message msg = new Message();
				msg.what = SHARE_CANCLE;
				msg.obj = plf.getName();
				shareHandler.sendMessage(msg);
			}
		});
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		// oks.setComment(comment);
		// site是分享此内容的网站名称，仅在QQ空间使用
		// oks.setSite(sit);
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		// oks.setSiteUrl(sitUrl);
		
		oks.setPlatform(platform);
		oks.show(mContext);
	}
	
	private void starEvent(String eventId,String parentType,String shareFrom){
		if(parentType == ""){
			XHClick.mapStat(mContext, eventId, shareFrom,"");
		}else{
			XHClick.mapStat(mContext, eventId, parentType,shareFrom);
		}
	}

	public void showSharePlatform(String title, String content,String types,String img, final String clickUrl, String platform,String from,String parent) {
		showSharePlatform(title, content, types, img, clickUrl, platform, from,parent,true);
	}
	
	public String drawableToPath(String dbName){
		String dbPath = UtilFile.getSDDir() + "long/" + dbName;
		File file = new File(dbPath);
		if(file.exists()){
			return file.getAbsolutePath();
		}else{
			Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), Integer.parseInt(dbName));
			return saveDrawable(bmp,"long/" + dbName);
		}
	}
	
	public String saveDrawable(Bitmap btm,String name){
		InputStream ips = ImgManager.bitmapToInputStream(btm,0);
		File file = UtilFile.saveFileToCompletePath(UtilFile.getSDDir() + name, ips,false);
		if(file == null){
			return null;
		}
		return file.getAbsolutePath();
	}

	public void showShare() {
		oks.show(getContext());
	}

	/**
	 * 分享指定平台
	 * @param platform
	 */
	public void sharePlatform(String platform) {

	}

	/**
	 * 为了获取方便.
	 * @return
	 */
	private Context getContext() {
		return mContext;
	}
	
	public Handler shareHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			int flag = msg.what;
			String pla = msg.obj.toString();
			String[] pf = getPlatform(pla);
			switch(flag){
			case SHARE_OK:
				starEvent("a_share_success", mParent,mFrom);
				XHClick.statisticsShare(mFrom, mClickUrl, pf[1]);
				Tools.showToast(mContext, pf[0] + "分享成功");
				notifyShareResult(pf[0],"2");
				break;
			case SHARE_ERROR:
				if(("微信".equals(pf[0]) || pf[0].indexOf("微信") > -1) && ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0){
					Tools.showToast(mContext, "未检测到相关应用");
				}else
					Tools.showToast(mContext, pf[0] + "分享失败");
				notifyShareResult(pf[0],"1");
				break;
			case SHARE_CANCLE:
				Tools.showToast(mContext, pf[0] + "取消分享");
				notifyShareResult(pf[0],"1");
				break;
			}
			return false;
		}
	});

	public void notifyShareResult(String platform,String success){
		Map<String,String> data = new HashMap<>();
		data.put("platform",platform);
		data.put("status",success);
		ObserverManager.getInstence().notify(ObserverManager.NOTIFY_SHARE,this,data);
	}
	
	public String[] getPlatform(String name){
    	String[] pf = new String[2];
    	if(ShareTools.QQ_NAME.equals(name)){
    		pf[0] = "QQ";
    		pf[1] = "1";
		}else if(ShareTools.QQ_ZONE.equals(name)){
			pf[0] = "QQ空间";
			pf[1] = "2";
		}else if(ShareTools.WEI_XIN.equals(name)){
			pf[0] = "微信";
			pf[1] = "3";
		}else if(ShareTools.WEI_QUAN.equals(name)){
			pf[0] = "微信朋友圈";
			pf[1] = "4";
		}else if(ShareTools.SINA_NAME.equals(name)){
			pf[0] = "新浪";
			pf[1] = "5";
		}else if(ShareTools.SHORT_MESSAGE.equals(name)){
			pf[0] = "短信";
			pf[1] = "6";
		}
		return pf;
	}
	
	private final int SHARE_OK = 1;
	private final int SHARE_ERROR = 2;
	private final int SHARE_CANCLE = 3;
}
