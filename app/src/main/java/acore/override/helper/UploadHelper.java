package acore.override.helper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.logic.MessageTipController;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.data.UploadData;
import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.UploadImg;
import amodule.main.Main;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.basic.XHConf;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;

@SuppressLint("UseSparseArrays")
public abstract class UploadHelper {
	protected String UPLOADTYPE = "";
	public static final String STATE_UPLOADING="uploading";
    public static final String STATE_FAILED="failed";
    public static final String STATE_EMPTY="";
	/** 单位ms */
	private int TIMEOUT = 6 * 60 * 1000;
	
	//图片池，已dishUploadCode为key建立的KV结构的本地路径和线上路径对应关系池
	protected Map<Long,Map<String,Map<String,String>>> imgPool = new HashMap<Long,Map<String,Map<String,String>>>();
	protected Map<Long,Timer> timerMap = new HashMap<Long, Timer>();
	protected Map<Long,UploadData> uploadDataMap = new HashMap<Long, UploadData>();
	/** 上传用到的callback */
	protected UploadCallback mUploadCallback = null;
	
	/**
	 * 上传图片
	 * @param uploadTimeCode	上传标示
	 * @param type 类型
	 * @param dir 本地图片地址
	 */
	public void uploadImg(final long uploadTimeCode,final String type,final String dir) {
		if(dir.length()==0) return;
		String img=getImgFromPool(uploadTimeCode,type,dir);
		if(img.equals(STATE_EMPTY)||img.equals(STATE_FAILED)) {
			putImgPool(uploadTimeCode, type, dir, STATE_UPLOADING);
			UploadImg uploadImg = new UploadImg(type,dir, new InternetCallback() {
				
				@Override
				public void loaded(int flag, String url, Object msg) {
					if (flag >= UtilInternet.REQ_OK_STRING) {
						putImgPool(uploadTimeCode, type, dir, (String)msg);
					} else {
						putImgPool(uploadTimeCode, type, dir, STATE_FAILED);
					}
					if(ifUploading(uploadTimeCode)){
						UploadData uploadData = uploadDataMap.get(uploadTimeCode);
						if(uploadData==null) 
							endUpload(uploadTimeCode,UtilInternet.REQ_FAILD,"意外丢失上传数据");
						else
							startUpload(uploadData);
					}
				}
			});
			uploadImg.uploadImg();
		}
	}

	/**
	 * 从imgPool中获取img
	 * 没有此图片：返回state_empty
	 * 正在上传：返回state_uploading
	 * 上传失败：返回state_failed
	 * @param dir
	 */
	public String getImgFromPool(long dishUploadCode,String type, String dir){
		if(dir.indexOf("http")==0) return dir;
		String url=STATE_EMPTY;
		Map<String,Map<String,String>> imgList=imgPool.get(dishUploadCode);
		if(imgList!=null){
			Map<String,String> imgTypeList=imgList.get(type);
			if(imgTypeList!=null){
				url=imgTypeList.containsKey(dir)?imgTypeList.get(dir):STATE_EMPTY;
			}
		}
		return url;
	}
	
	/**
	 * 放入图片池
	 * @param uploadTimeCode	菜谱上传标示
	 * @param type 类型
	 * @param dir 本地图片地址
	 * @param url 网络图片地址
	 */
	private void putImgPool(long uploadTimeCode,String type, String dir, String url) {
		Map<String,Map<String,String>> imgList=imgPool.containsKey(uploadTimeCode)?imgPool.get(uploadTimeCode):new HashMap<String, Map<String,String>>();
		Map<String,String> imgTypeList=imgList.containsKey(type)?imgList.get(type):new HashMap<String, String>();
		imgTypeList.put(dir, url);
		imgList.put(type, imgTypeList);
		imgPool.put(uploadTimeCode, imgList);
	}
	
	/** 
	 * 开始发布
	 * @param dishData
	 */
	public void startUpload(final UploadData dishData) {
		final LinkedHashMap<String, String> uploadData = combineParameter(dishData);
		UtilLog.print(XHConf.log_tag_upload,"d", "------------图片合并后的数据-----------\n"+uploadData.toString());
		if(uploadData.containsKey(STATE_FAILED)) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					endUpload(dishData.getUploadTimeCode(), UtilInternet.REQ_FAILD,uploadData.get(STATE_FAILED));
				}
			}, 500);
		}else{
			long uploadTimeCode=dishData.getUploadTimeCode();
			timeoutTimer(uploadTimeCode,true);
			if(!ifUploading(uploadTimeCode)) 
				uploadLog("开始发布", uploadData);
			//设置发布时候状态
			startUploadHandle(uploadTimeCode,dishData);
			if(uploadData.containsKey(STATE_UPLOADING)) 
				uploadLog("有图片正在发布", uploadData.get(STATE_UPLOADING));
			else {
				uploadLog("正式发布", uploadData);
				doUpload(uploadTimeCode,uploadData);
			}
		}
	}

	/**
	 * 正式发布
	 * @param uploadTimeCode
	 * @param uploadData
     */
	protected void doUpload(final long uploadTimeCode,final LinkedHashMap<String, String> uploadData) {
		timeoutTimer(uploadTimeCode,false);
		MessageTipController.newInstance().getCommonData(new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				if(flag >= ReqInternet.REQ_OK_STRING){
					if(uploadData!=null&&uploadDataMap.containsKey(uploadTimeCode)){
						UploadData data = uploadDataMap.get(uploadTimeCode);
						ReqInternet.in().doPost(getUploadAPi(data), uploadData, new InternetCallback() {

							@Override
							public void loaded(int flag, String url, Object msg) {
								Log.i("zhangyujian","url::::"+url);
								//统计
								if(StringManager.replaceUrl(StringManager.api_uploadFloor).equals(url)){
									XHClick.track(XHApplication.in(),"美食贴评论");
								}else if(StringManager.replaceUrl(StringManager.api_uploadSubject).equals(url)){
									if(uploadData.containsKey("video")&&uploadData.containsKey("videoSImg")&&uploadData.containsKey("videoType")
											&&!TextUtils.isEmpty(uploadData.get("video"))&&!TextUtils.isEmpty(uploadData.get("videoSImg"))
											&&!TextUtils.isEmpty(uploadData.get("videoType"))){
										XHClick.track(XHApplication.in(),"发小视频贴成功");
									}else{
                                    	XHClick.track(XHApplication.in(),"发美食贴成功");
									}
                                }else if(StringManager.replaceUrl(StringManager.api_uploadDish).equals(url)){
									if (FriendHome.isAlive) {
										Intent broadIntent = new Intent();
										broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
										broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "0");
										broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.STATE_KEY, UploadStateChangeBroadcasterReceiver.STATE_SUCCESS);
										broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.SECONDE_EDIT, TextUtils.isEmpty(uploadData.get("code")) ? "1" : "2");
										if (Main.allMain != null)
											Main.allMain.sendBroadcast(broadIntent);
									}
									XHClick.track(XHApplication.in(),"发菜谱成功");
								}
								endUpload(uploadTimeCode,flag,msg);
							}
						});
					}
					else{
						endUpload(uploadTimeCode,ReqInternet.REQ_FAILD,"未检测到数据，请重试");
					}
				}else{
					endUpload(uploadTimeCode,ReqInternet.REQ_FAILD,returnObj);
				}
			}
		});
		//测试延时代码
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				
//			}
//		}, 5 * 1000);
	}
	
	/** 
	 * 发布结束
	 * @param uploadTimeCode	菜谱上传标示
	 * @param flag
	 * @param msg
	 */
	protected void endUpload(long uploadTimeCode,int flag, Object msg) {
		timeoutTimer(uploadTimeCode,false);
		uploadLog("发布结束", msg);
		UploadData uploadData = uploadDataMap.get(uploadTimeCode);
		endUploadHandle(uploadData, flag, msg);
		imgPool.remove(uploadTimeCode);
		uploadDataMap.remove(uploadTimeCode);
	}
	
	/**
	 * 是否正在上传
	 * @param uploadTimeCode
	 * @return
	 */
	public boolean ifUploading(long uploadTimeCode){
		if(uploadDataMap.containsKey(uploadTimeCode)){
			return ifUploadingHandle(uploadTimeCode);
		}
		else 
			return false;
	}
	
	/** 
	 * 开启或关闭超时timer
	 * @param uploadTimeCode	菜谱上传标示
	 * @param isOpen
	 */
	protected void timeoutTimer(final long uploadTimeCode,boolean isOpen) {
		if(isOpen){
			if(timerMap.containsKey(uploadTimeCode)) 
				return;
			final Handler handler=new Handler(){
				@Override
				public void handleMessage(Message msg) {
					endUpload(uploadTimeCode,UtilInternet.REQ_FAILD,"等待图片上传超时");
				}
			};
			TimerTask overTast = new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(0);
				}
			};
			Timer timer=new Timer();
			timer.schedule(overTast,TIMEOUT);
			timerMap.put(uploadTimeCode, timer);
		}
		else if(timerMap.containsKey(uploadTimeCode)){
			Timer timer =timerMap.get(uploadTimeCode);
			timer.cancel();
			timer.purge();
			timerMap.remove(uploadTimeCode);
		}
	}
	
	/**
	 * 上传日志
	 * @param status
	 */
	protected void uploadLog(String status,Object msg){
		LinkedHashMap<String, String> map = LogManager.getReportLog("upload" + UPLOADTYPE,status,Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0),msg);
		ReqInternet.in().doPost(StringManager.api_uploadUserLog, map , new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				
			}
		});
	}
	/**
	 * 上传过程中协助处理
	 * 上传菜谱 和 上传美食贴 实现不同的数据处理
	 */
	public abstract void startUploadHandle(long uploadTimeCode,final UploadData uploadData);
	public abstract void endUploadHandle(UploadData uploadData,int flag, Object msg);
	public abstract boolean ifUploadingHandle(long uploadTimeCode);
	public abstract LinkedHashMap<String, String> combineParameter(UploadData dishData);
	public abstract String getUploadAPi(UploadData data);
	
	public interface UploadCallback{
		/** 上传中 */
		public void uploading(int id);
		/** 上传完成 */
		public void uploadOver(UploadData uploadData,int flag,Object msg);
	}

	public UploadCallback getUploadCallback() {
		return mUploadCallback;
	}

	public void setUploadCallback(UploadCallback mUploadCallback) {
		this.mUploadCallback = mUploadCallback;
	}
	
}
