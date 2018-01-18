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
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
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
		String newClickUrl = clickUrl;
		mClickUrl = newClickUrl + "";
		mFrom = from + "";
		mParent = parent + "";
		if (platform == LINK_COPY && !TextUtils.isEmpty(newClickUrl)) {
			XHClick.onEvent(mContext, "a_share_click", "拷贝");
			Tools.inputToClipboard(mContext,newClickUrl);
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
		if(TextUtils.isEmpty(content)){
			content = " ";
		}
		if(platform.equals(SINA_NAME)){
			content = content + newClickUrl;
		}
		if(platform.equals(SHORT_MESSAGE)){
			content = title + content + newClickUrl;
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
		oks.setTitleUrl(newClickUrl);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(content);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImagePath(imgPath); // 确保SDcard下面存在此张图片
		oks.setImageUrl(imgUrl);
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(newClickUrl);

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

	public void showSharePlatform(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return;
		String shareParams = map.get("shareParams");
		Map<String, String> shareParamsMap = StringManager.getFirstMap(shareParams);
		String shareType = shareParamsMap.get("shareType");
		String shareConfig = shareParamsMap.get("shareConfig");
		Map<String, String> shareConfigMap = StringManager.getFirstMap(shareConfig);
		Map<String, String> shareMap = StringManager.getFirstMap(shareConfigMap.get(shareType));
		if (TextUtils.equals(shareType, "2") && TextUtils.equals(WEI_XIN, map.get("platform"))) {
			shareMap.put("type", map.get("type"));
			showShareMiniProgram(shareMap);
		} else {
			String title = map.get("title");
			String content = map.get("content");
			String imgUrl = map.get("img");
			String clickUrl = map.get("url");
			String type = map.get("type");
			String from = map.get("from");
			String parent = map.get("parent");
			String platform = map.get("platform");
			showSharePlatform(title, content, type, imgUrl, clickUrl, platform, from, parent, true);
		}
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
					notifyShareResult(pf[0],"2", pf[2]);
					break;
				case SHARE_ERROR:
					if(("微信".equals(pf[0]) || pf[0].indexOf("微信") > -1) && ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0){
						Tools.showToast(mContext, "未检测到相关应用");
					}else
						Tools.showToast(mContext, pf[0] + "分享失败");
					notifyShareResult(pf[0],"1", pf[2]);
					break;
				case SHARE_CANCLE:
					Tools.showToast(mContext, pf[0] + "取消分享");
					notifyShareResult(pf[0],"1", pf[2]);
					break;
			}
			return false;
		}
	});

	public void notifyShareResult(String platform,String success, String jsCallbackParams){
		Map<String,String> data = new HashMap<>();
		data.put("platform",platform);
		data.put("status",success);
		data.put("callbackParams", jsCallbackParams);
		ObserverManager.getInstance().notify(ObserverManager.NOTIFY_SHARE,this,data);
	}

	public String[] getPlatform(String name){
		String[] pf = new String[3];
		if(ShareTools.QQ_NAME.equals(name)){
			pf[0] = "QQ";
			pf[1] = "1";
			pf[2] = "QQ";
		}else if(ShareTools.QQ_ZONE.equals(name)){
			pf[0] = "QQ空间";
			pf[1] = "2";
			pf[2] = "QZone";
		}else if(ShareTools.WEI_XIN.equals(name)){
			pf[0] = "微信";
			pf[1] = "3";
			pf[2] = "Wechat";
		}else if(ShareTools.WEI_QUAN.equals(name)){
			pf[0] = "微信朋友圈";
			pf[1] = "4";
			pf[2] = "WechatMoments";
		}else if(ShareTools.SINA_NAME.equals(name)){
			pf[0] = "新浪";
			pf[1] = "5";
			pf[2] = "SinaWeibo";
		}else if(ShareTools.SHORT_MESSAGE.equals(name)){
			pf[0] = "短信";
			pf[1] = "6";
			pf[2] = "";
		}
		return pf;
	}

	private final int SHARE_OK = 1;
	private final int SHARE_ERROR = 2;
	private final int SHARE_CANCLE = 3;

	private void showShareMiniProgram(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return;
		String path = "pages/index/index.html";
		String confPath = map.get("path");
		HashMap<String, Object> configShare = new HashMap<>();
		configShare.put("Id", "4");
		configShare.put("SortId", "1");
		configShare.put("AppId", "wx2b582fbe26ef8993");
		configShare.put("AppSecret", "178b4d14294057b0df3d4586621cfe00");
		configShare.put("UserName", "gh_7482de333db0");
		configShare.put("Path", TextUtils.isEmpty(confPath) ? path : confPath);
		configShare.put("BypassApproval", "false");
		configShare.put("Enable", "true");
		ShareSDK.setPlatformDevInfo(Wechat.NAME, configShare);
		String type = map.get("type");
		String img = map.get("img");
		String defaultpageUrl = "https://m.xiangha.com";
		String webpageUrl = map.get("url");
		if(img == null || img.length()==0){
			type = IMG_TYPE_RES;
			img = R.drawable.share_launcher+"";
		}
		String imgUrl = "",imgPath = "";
		if(type.equals(IMG_TYPE_WEB)){
			imgUrl = img;
			if (imgUrl != null && imgUrl.endsWith(".webp"))
				imgUrl = imgUrl.replace(".webp", "");
		} else if(type.equals(IMG_TYPE_RES)){
			imgPath = drawableToPath(img);
		} else if(type.equals(IMG_TYPE_LOC)){
			imgPath = img;
		}

		Platform platform = ShareSDK.getPlatform(Wechat.NAME);
		Platform.ShareParams shareParams = new  Platform.ShareParams();
		shareParams.setText(map.get("content"));
		shareParams.setTitle(map.get("title"));
		shareParams.setUrl(TextUtils.isEmpty(webpageUrl) ? defaultpageUrl : webpageUrl);
		if (!TextUtils.isEmpty(imgUrl))
			shareParams.setImageUrl(imgUrl);
		if (!TextUtils.isEmpty(imgPath))
			shareParams.setImagePath(imgPath);
		shareParams.setShareType(Platform.SHARE_WXMINIPROGRAM);
		platform.setPlatformActionListener(new PlatformActionListener() {
			@Override
			public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
				Message msg = new Message();
				msg.what = SHARE_OK;
				msg.obj = platform.getName();
				shareHandler.sendMessage(msg);
			}

			@Override
			public void onError(Platform platform, int i, Throwable throwable) {
				throwable.printStackTrace();
				Message msg = new Message();
				msg.what = SHARE_ERROR;
				msg.obj = platform.getName();
				shareHandler.sendMessage(msg);
			}

			@Override
			public void onCancel(Platform platform, int i) {
				Message msg = new Message();
				msg.what = SHARE_CANCLE;
				msg.obj = platform.getName();
				shareHandler.sendMessage(msg);
			}
		});
		platform.share(shareParams);
	}

}
