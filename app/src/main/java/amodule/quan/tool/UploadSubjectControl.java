package amodule.quan.tool;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.data.UploadData;
import acore.override.helper.UploadHelper;
import acore.tools.StringManager;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.quan.view.ImgTextCombineLayout;
import xh.basic.internet.UtilInternet;

/**
 * @author Eva
 * 美食贴发布控制
 */
public class UploadSubjectControl extends UploadHelper{
	public static final String IMAGE_TYPE_SUBJECT = "subject";
	public static UploadSubjectControl uploadSubjectControl =null;
	private ReplyCallback mReplyCallback = null;
	
	/**
	 * 单例模式
	 * @return
	 */
	public static UploadSubjectControl getInstance(){
		if(uploadSubjectControl==null){
			uploadSubjectControl= new UploadSubjectControl();
		}
		return uploadSubjectControl;
	}
	
	/**
	 * 初始化必要参数 
	 */
	private UploadSubjectControl(){
		UPLOADTYPE = "Subject";
	}
	
	/**
	 * 开始发贴时需要实现的代码
	 */
	@Override
	public void startUploadHandle(long uploadTimeCode, UploadData uploadData) {
		SubjectData subjectData = (SubjectData) uploadData;
		subjectData.setUploadState(SubjectData.UPLOAD_ING);
		SubjectSqlite uploadSubjectSqlite = SubjectSqlite.getInstance(XHApplication.in());
		uploadSubjectSqlite.updateById(subjectData.getId(), SubjectData.UPLOAD_ING);
		uploadDataMap.put(uploadTimeCode, subjectData);
		if(mUploadCallback != null && SubjectData.TYPE_UPLOAD.equals(subjectData.getType())){
			mUploadCallback.uploading(uploadData.getId());
		}
	}
	
	/**
	 * 发贴结束时需要实现的代码
	 */
	@Override
	public void endUploadHandle(UploadData uploadData, int flag, Object msg) {
		SubjectData subjectData = (SubjectData) uploadData;
		if(subjectData != null){
			SubjectSqlite uploadSubject = SubjectSqlite.getInstance(XHApplication.in());
			int id = subjectData.getId();
			if (flag >= UtilInternet.REQ_OK_STRING) {
				// 发布成功后跟新状态;
				if(id > 0){
					uploadSubject.deleteById(id);
					subjectData.setUploadState(SubjectData.UPLOAD_SUCCESS);
					if(msg != null){
						subjectData.setCode(msg.toString());
					}
				}
				XHClick.onEventValue(XHApplication.in(), "uploadQuanRes", "uploadQuanRes", "成功", 100);
			}else {
				// 发送失败时更改状态;
				subjectData.setUploadState(SubjectData.UPLOAD_FAIL);
				uploadSubject.updateById(id, SubjectData.UPLOAD_FAIL);
				XHClick.onEventValue(XHApplication.in(), "uploadQuanRes", "uploadQuanRes", msg.toString(), 0);
			}
			String type = subjectData.getType();
			if(SubjectData.TYPE_UPLOAD.equals(type)){
				if(mUploadCallback != null) 
					mUploadCallback.uploadOver(subjectData, flag, msg);
			}else if(SubjectData.TYPE_REPLY.equals(type)){
				if (flag >= UtilInternet.REQ_OK_STRING){
					if(mReplyCallback != null)
						mReplyCallback.onReplySuccess(subjectData, flag, msg);
				}else {
					if(mReplyCallback != null) 
						mReplyCallback.onReplyFailed(subjectData);
				}
			}
		}
	}
	
	/**
	 * 状态是否处于上传中
	 */
	@Override
	public boolean ifUploadingHandle(long uploadTimeCode) {
		SubjectData subjectData = (SubjectData) uploadDataMap.get(uploadTimeCode);
		int subjectType = subjectData.getUploadState();
		return subjectType == SubjectData.UPLOAD_ING;
	}
	
	/**
	 * 拼接发布需要的参数
	 */
	@Override
	public LinkedHashMap<String, String> combineParameter(UploadData uploadData) {
		SubjectData subjectData = (SubjectData) uploadData;
		LinkedHashMap<String, String> pamras = new LinkedHashMap<>();
		pamras.put("sign", String.valueOf(subjectData.getUploadTimeCode()));
		pamras.put("title", subjectData.getTitle());
		pamras.put("cid", subjectData.getCid());
		pamras.put("location", subjectData.getLocation());
		pamras.put("isLocation", subjectData.getIsLocation());
		pamras.put("video", subjectData.getVideo());
		pamras.put("videoType", subjectData.getVideoType());
		String videoSImg = getImgFromPool(subjectData.getUploadTimeCode(), IMAGE_TYPE_SUBJECT , subjectData.getVideoSImg());
		if(videoSImg.equals(STATE_UPLOADING)){
			pamras.put(STATE_UPLOADING, "视频封面图正在上传");
		}else if(videoSImg.equals(STATE_FAILED)) {
			pamras.put(STATE_FAILED, "视频封面图上传失败，请重新发布");
		}
		pamras.put("videoSImg", videoSImg);
		if(!TextUtils.isEmpty(subjectData.getDishCode())){
			pamras.put("dishCode", subjectData.getDishCode());
			pamras.put("fraction", String.valueOf(subjectData.getScoreNum()));
		}
		if(SubjectData.TYPE_REPLY.equals(subjectData.getType())){
			pamras.put("code", subjectData.getCode());
		}
		//内容拼接
		ArrayList<Map<String, String>> contentArray = subjectData.getContentArray();
		for(int index = 0 ; index < contentArray.size() ; index ++){
			Map<String, String> contentMap = contentArray.get(index);
			String dir = contentMap.get(ImgTextCombineLayout.IMGEURL);
			String content = contentMap.get(ImgTextCombineLayout.CONTENT);
			if(TextUtils.isEmpty(dir) && TextUtils.isEmpty(content)){
				contentArray.remove(index--);
				continue;
			}
			String img = getImgFromPool(subjectData.getUploadTimeCode(), IMAGE_TYPE_SUBJECT , dir);
			if(img.equals(STATE_UPLOADING)){
				pamras.put(STATE_UPLOADING, "第"+(index+1)+"张图正在上传");
			}
			if(img.equals(STATE_FAILED)) {
				pamras.put(STATE_FAILED, "第"+(index+1)+"张图上传失败，请重新发布");
			}
			pamras.put("img[" + index + "]", img);
			pamras.put("text[" + index + "]", content);
		}
		
		return pamras;
	}

	public ReplyCallback getmReplyCallback() {
		return mReplyCallback;
	}

	public void setReplyCallback(ReplyCallback mReplyCallback) {
		this.mReplyCallback = mReplyCallback;
	}

	/**
	 * 获取上传的api
	 */
	@Override
	public String getUploadAPi(UploadData data) {
		SubjectData subjectData = (SubjectData) data;
		String type = subjectData.getType();
		String uploadApi = StringManager.api_uploadSubject;
		if(SubjectData.TYPE_UPLOAD.equals(type)){
			uploadApi = StringManager.api_uploadSubject;
		}else if(SubjectData.TYPE_REPLY.equals(type)){
			uploadApi = StringManager.api_uploadFloor;
		}
		return uploadApi;
	}
	
	public interface ReplyCallback{
		public void onReplySuccess(SubjectData uploadData, int flag, Object msg);
		public void onReplyFailed(SubjectData uploadData);
	}
	
	

}
