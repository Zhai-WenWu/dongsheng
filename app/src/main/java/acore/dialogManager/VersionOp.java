///**
// * @author 方瑞娇
// * 2015-6-3 上午10:59:31
// */
//package acore.dialogManager;
//
//import android.app.Activity;
//import android.content.Context;
//import android.net.Uri;
//import android.util.Log;
//
//import com.download.down.VersionUpload;
//import com.xiangha.R;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import acore.logic.XHClick;
//import acore.override.helper.XHActivityManager;
//import acore.tools.FileManager;
//import acore.tools.StringManager;
//import acore.tools.Tools;
//import aplug.basic.InternetCallback;
//import aplug.basic.ReqInternet;
//import xh.basic.tool.UtilLog;
//
//public class VersionOp extends DialogManagerParent{
//	private static VersionOp versionOp=null;
//	private String path = FileManager.getSDCacheDir();
//	private String apkName = "香哈菜谱";
//	private VersionUpload versionUpload;
//	/***/
//	private boolean mShowPro;
//	/**是否必须升级*/
//	boolean isMustUpdata = false;
//	/***/
//	private boolean misSilentInstall = false;
//
//	private int appNum,hintNum;
//	private String newNum,nowNum;
//
//	private static String tongjiId = "a_silent";
//
//	public static VersionOp getInstance(){
//		if(versionOp==null){
//			versionOp= new VersionOp();
//		}
//		return versionOp;
//	}
//
//	@Override
//	public void isShow(final OnDialogManagerCallback callback) {
//		getUpdata(false, new OnGetUpdataCallback() {
//			@Override
//			public void onPreUpdate() {
//			}
//
//			@Override
//			public void onNeedUpdata() {
//				callback.onShow();
//			}
//
//			@Override
//			public void onNotNeed() {
//				callback.onGone();
//			}
//
//			@Override
//			public void onFail() {
//				callback.onGone();
//			}
//		});
//	}
//
//	@Override
//	public void show() {
//		if(isMustUpdata || !misSilentInstall) {
//			Log.i("versionOp","VersionOp show() starUpdate()");
//			versionUpload.starUpdate(!mShowPro, silentListener);
//		}else{
//			Log.i("versionOp","VersionOp show() silentInstall()");
//			File file = new File(path + apkName + "_" + newNum + ".apk");
//			VersionUpload.silentInstall(isMustUpdata,XHActivityManager.getInstance().getCurrentActivity(),Uri.fromFile(file),
//					VersionUpload.INTALL_TYPE_NEXT_STAR,true,nowNum,newNum,appNum,hintNum,silentListener);
//		}
//	}
//
//	@Override
//	public void cancel() {
//
//	}
//
//	private void getUpdata(boolean showPro, final OnGetUpdataCallback callback){
//		mShowPro = showPro;
//		LinkedHashMap<String, String> map = new LinkedHashMap<>();
//		//手动升级
//		if(mShowPro) map.put("update", "1");
//		ReqInternet.in().doPost(StringManager.api_versionInfo, map,
//				new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
//					@Override
//					public void loaded(int flag, final String url,Object returnObj) {
//						if (flag >= ReqInternet.REQ_OK_STRING) {
//							Activity mAct = XHActivityManager.getInstance().getCurrentActivity();
//							try {
//								ArrayList<Map<String, String>> array = StringManager.getListMapByJson(returnObj);
//								//当需要升级时，服务端才返回升级数据
//								if(array.size() > 0){
//									Map<String, String> map = array.get(0);
//									nowNum = getVerName(XHActivityManager.getInstance().getCurrentActivity());
//									newNum = map.get("code");
//									String content = map.get("content");
//									String updateUrl = map.get("url");
//									appNum = Integer.parseInt(map.get("appNum"));
//									int play = Integer.parseInt(map.get("play"));
//									hintNum = Integer.parseInt(map.get("cishu"));
//									if(appNum == 0) isMustUpdata = true;
//									boolean isPlay = true;
//									if(play == 1) isPlay = false;
//									boolean isNeedUpdata = false;
//									if(!mShowPro){
//										misSilentInstall = isSilentInstall(isNeedUpdata,VersionUpload.INTALL_TYPE_NEXT_STAR,nowNum,newNum,appNum,hintNum);
//										isNeedUpdata = misSilentInstall;
//									}
//									Log.i("versionOp","isNeedUpdata:" + isNeedUpdata + "  isMustUpdata:" + isMustUpdata);
//									versionUpload = new VersionUpload(XHActivityManager.getInstance().getCurrentActivity(), content, R.drawable.ic_launcher,nowNum,newNum,
//											isMustUpdata, isPlay, hintNum,appNum, updateUrl, path,apkName,vsUpListener);
////									if(!misSilentInstall) isNeedUpdata = versionUpload.isUpdata();
//									if(!misSilentInstall) isNeedUpdata = versionUpload.isUpdata(!mShowPro);
//									Log.i("versionOp","isNeedUpdata:" + isNeedUpdata);
//									if(isNeedUpdata){
//										callback.onNeedUpdata();
//									}else {
//										callback.onNotNeed();
//									}
//								}else{
//									if(mShowPro){
//										Tools.showToast(mAct, "已是最新版本！");
//									}
//									callback.onNotNeed();
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//								UtilLog.reportError("获取版本信息:" + returnObj.toString(), e);
//								if (mShowPro)
//									Tools.showToast(mAct, "获取新版本错误，请稍后再试");
//								callback.onFail();
//								return;
//							}
//						} else {
//							callback.onFail();
//						}
//					}
//				});
//	}
//
//	/**
//	 * 检查更新
//	 * @param showPro 是否显示更新进度框
//	 */
//	public void toUpdate(final OnGetUpdataCallback callback,final boolean showPro) {
//		mShowPro = showPro;
//		if(callback != null){
//			callback.onPreUpdate();
//		}
//		getUpdata(showPro, new OnGetUpdataCallback() {
//			@Override
//			public void onPreUpdate() {
//
//			}
//
//			@Override
//			public void onNeedUpdata() {
//				versionUpload.starUpdate(!mShowPro,silentListener);
//				if(callback != null){
//					callback.onNeedUpdata();
//				}
//			}
//
//			@Override
//			public void onNotNeed() {
//				if(callback != null){
//					callback.onNotNeed();
//				}
//			}
//
//			@Override
//			public void onFail() {
//				if(callback != null){
//					callback.onFail();
//				}
//			}
//		});
//	}
//
//	private VersionUpload.VersionUpdateListener vsUpListener = new VersionUpload.VersionUpdateListener() {
//		@Override
//		public void onActionDown() {
//			super.onActionDown();
//			XHClick.onEvent(XHActivityManager.getInstance().getCurrentActivity(), "appUpdate", "立即");
//		}
//
//		@Override
//		public void onLaterUpdate() {
//			super.onLaterUpdate();
//			XHClick.onEvent(XHActivityManager.getInstance().getCurrentActivity(), "appUpdate", "稍后");
//		}
//
//		@Override
//		public void downOk(Uri uri, boolean isSilent) {
//
//		}
//
//		@Override
//		public void onUnShowDialog(int flag) {
//			super.onUnShowDialog(flag);
//		}
//
//		@Override
//		public void downError(String arg0) {
//			Tools.showToast(XHActivityManager.getInstance().getCurrentActivity(), arg0);
//		}
//	};
//
//	/**
//	 * 按照指定type提示安装框
//	 * @param type ：弹安装框的时间
//     */
//	private boolean isSilentInstall(boolean isMustUp, int type, String nowNum, String newNum, int appNum, int hintNum){
//		File file = new File(path + apkName + "_" + newNum + ".apk");
//		return VersionUpload.isSilentInstall(isMustUp,XHActivityManager.getInstance().getCurrentActivity(),Uri.fromFile(file),type,true,nowNum,newNum,appNum,hintNum,silentListener);
//	}
//
//
//	// 获取当前版本
//	public static String getVerName(Context context) {
//		String verCode = "0.0.0";
//		try {
//			if (context != null)
//				verCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
//		} catch (Exception e) {
//			UtilLog.reportError("版本号获取异常", e);
//		}
//		return verCode;
//	}
//
//	public void onDesotry(){
//		if(versionUpload != null){
//			versionUpload.cancelDownLoad();
//		}
//	}
//
//	private boolean isCancel = true;
//	private VersionUpload.VersionUpdateSilentListener silentListener = new VersionUpload.VersionUpdateSilentListener() {
//		@Override
//		public void onCancel() {
//			if(isCancel)XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "点击弹框关闭“手机返回键”", "");
//		}
//
//		@Override
//		public void onShow() {
//			isCancel = true;
//			XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "静默更新弹框次数”", "");
//		}
//
//		@Override
//		public void onSureClick() {
//			isCancel = false;
//			XHClick.mapStat(XHActivityManager.getInstance().getCurrentActivity(), tongjiId, "点击弹框确认", "");
//		}
//	};
//
//	public interface OnGetUpdataCallback{
//		void onPreUpdate();
//		void onNeedUpdata();
//		void onNotNeed();
//		void onFail();
//	}
//
//}
