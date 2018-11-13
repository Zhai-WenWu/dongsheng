package amodule.upload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.LogManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

public class UploadImg {
	private int uploadNum = 0;
	//给失败的图片三次机会
	public int FAIL_NUM = 3;
	private InternetCallback callback;
	public String mPath;
	public String url;
	public int state;
	public String mType="";
	
	public UploadImg(String type,String path, InternetCallback uploadSubImgCallback){
		mType = type;
		this.mPath = path;
		this.callback = uploadSubImgCallback;
		state = UPLOAD;
	}
	
	public void uploadImg(){
		uploadNum ++;
		UtilLog.print("d","上传图片:" + uploadNum);
		// 上传使用的参数
		LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
		fileMap.put("uploadImg_imgs_1", mPath);
		ReqInternet.in().doPostImg(StringManager.api_uploadImg, fileMap,
				new InternetCallback() {
					@Override
					public void loaded(int flag, String url,Object returnObj) {
						if (flag >= UtilInternet.REQ_OK_STRING) { //成功,回调成功
							ArrayList<Map<String, String>> listObj = UtilString.getListMapByJson(returnObj);
							if(listObj.size() > 0){
								UploadImg.this.url = listObj.get(0).get("");
								state = SUCCES;
								UtilLog.print("d","上传图片成功:" + UploadImg.this.url);
								callback.loaded(flag, mPath, UploadImg.this.url);
								return;
							}
						}
						//失败,是否还有上传机会
						if(uploadNum < FAIL_NUM){
							UtilLog.print("d","上传图片失败：" + uploadNum);
							uploadImg();
						}
						else{ //没有机会,回调失败
//								String data = Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0) + ":" + returnObj.toString() + "\n\n";
//								UtilFile.saveFileToCompletePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/发贴失败日志.txt",  data, true);
							uploadLog("上传图片失败",returnObj);
							state = FAIL;
							UtilLog.print("d","上传图片失败：" + returnObj);
							callback.loaded(flag, mPath, returnObj);
						}
					}
				});
	}
	
	/**
	 * 上传日志
	 * @param status
	 */
	protected void uploadLog(String status,Object msg){
		LinkedHashMap<String, String> map = LogManager.getReportLog("uploadSubject",status,Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0),msg);
		ReqInternet.in().doPost(StringManager.api_uploadUserLog, map , new InternetCallback() {
			@Override
			public void loaded(int flag, String url, Object returnObj) {
				
			}
		});
	}
	
	/**
	 * 图片上传状态
	 * @author xiangha01
	 */
	/**
	 * 上传中
	 */
	public static final int UPLOAD = 10001;
	/**
	 * 上传失败
	 */
	public static final int FAIL = 10002; 
	/**
	 * 上传成功
	 */
	public static final int SUCCES = 10003;
}
