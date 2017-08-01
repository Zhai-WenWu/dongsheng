package amodule.dish.tools;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.data.UploadData;
import acore.override.helper.UploadHelper;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.PopWindowDialog;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import third.share.BarShare;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

public class UploadDishControl extends UploadHelper{
	 public static final String imgType_bigImg="bigImg";
	 public static final String imgType_makeImg="makeImg";
	 
	 private static  UploadDishControl mUploadDishTest;

	 public static UploadDishControl getInstance(){
		 if(mUploadDishTest == null){
			 mUploadDishTest = new UploadDishControl();
		 }
		 return mUploadDishTest;
	 }
	 
	 public UploadDishControl(){
		 UPLOADTYPE="Dish";
	 }

	@Override
	public void startUploadHandle(long uploadTimeCode, UploadData uploadData) {
		UploadDishData dishData = (UploadDishData) uploadData;
		if(TextUtils.isEmpty(dishData.getCode())) 
			dishData.setDishType(UploadDishData.UPLOAD_ING);
		else 
			dishData.setDishType(UploadDishData.UPLOAD_ING_BACK);
		UploadDishSqlite mDishSqlite=new UploadDishSqlite(XHApplication.in());
		mDishSqlite.update(dishData.getId(), dishData);
		uploadDataMap.put(uploadTimeCode, dishData);
	}

	@Override
	public void endUploadHandle(UploadData uploadData, int flag, Object msg) {
		UploadDishData uploadDishData = (UploadDishData) uploadData;
		if(uploadDishData!=null){
			//删除或更新草稿
			UploadDishSqlite uploadDishSqlite=new UploadDishSqlite(XHApplication.in());
			if(flag>=UtilInternet.REQ_OK_STRING){
				uploadDishData.setCode(String.valueOf(msg));
				boolean isShow = PopWindowDialog.isShowPop(FileManager.xmlKey_shareShowPopDataUpDish, FileManager.xmlKey_shareShowPopNumUpDish);
				if(isShow){
					BaseActivity.mUpDishPopWindowDialog = new PopWindowDialog(XHApplication.in(), "菜谱发布成功", "分享给你的朋友们，让他们也看看吧：",null);
					String clickUrl = StringManager.wwwUrl + "caipu/" + uploadDishData.getCode() + ".html";
					BaseActivity.mUpDishPopWindowDialog.show(BarShare.IMG_TYPE_LOC, "我做了[" + uploadDishData.getName() + "]，超好吃哦~", clickUrl, "独门秘籍都在这里，你也试试吧！", uploadDishData.getCover(), "菜谱发布成功后", "强化分享");
				}
				int id=uploadDishData.getId();
				if(id>0) uploadDishSqlite.deleteById(id);

				XHClick.mapStat(XHApplication.in(), "a_share400", "强化分享", "菜谱发布成功后");
				XHClick.onEventValue(XHApplication.in(), "uploadDishRes", "uploadDishRes", "成功", 100);
			}
			else{
				uploadDishData.setDishType(UploadDishData.UPLOAD_FAIL);
				uploadDishSqlite.update(uploadDishData.getId(), uploadDishData);
				Tools.showToast(XHApplication.in(), msg.toString());
				XHClick.onEventValue(XHApplication.in(), "uploadDishRes", "uploadDishRes", msg.toString(), 0);
			}
			if(mUploadCallback!=null) 
				mUploadCallback.uploadOver(uploadDishData, flag, msg);
		}
	}

	@Override
	public boolean ifUploadingHandle(long uploadTimeCode) {
		UploadDishData dishData = (UploadDishData) uploadDataMap.get(uploadTimeCode);
		String subjectType = dishData.getDishType();
		return subjectType==UploadDishData.UPLOAD_ING
				||subjectType==UploadDishData.UPLOAD_ING_BACK;
	}

	@Override
	public LinkedHashMap<String, String> combineParameter(UploadData data) {
		UploadDishData dishData = (UploadDishData) data;
		LinkedHashMap<String, String> uploadData = new LinkedHashMap<>();
		String img=getImgFromPool(dishData.getUploadTimeCode(),imgType_bigImg,dishData.getCover());
		if(img.equals(STATE_UPLOADING)){
			uploadData.put(STATE_UPLOADING, "菜谱大图正在上传");
		}
		if(img.equals(STATE_EMPTY)||img.equals(STATE_FAILED)) {
			uploadData.put(STATE_FAILED, "菜谱大图上传失败，请重新发布");
		}
		uploadData.put("sign", String.valueOf(dishData.getUploadTimeCode()));
		uploadData.put("code", dishData.getCode());
		uploadData.put("name", dishData.getName());
		uploadData.put("activityId", dishData.getActivityId());
		uploadData.put("img[0]", img);
		uploadData.put("info", dishData.getStory());
		uploadData.put("remark", dishData.getTips());
		
		uploadData.put("readyTime", dishData.getReadyTime());
		uploadData.put("cookTime", dishData.getCookTime());
		uploadData.put("taste", dishData.getTaste());
		uploadData.put("diff", dishData.getDiff());
		uploadData.put("exclusive", dishData.getExclusive());
		
		ArrayList<Map<String, String>> food= UtilString.getListMapByJson(dishData.getFood());
		for(int i=0;i<food.size();i++) {
			uploadData.put("ingredients[" + i + "]", food.get(i).get("name"));
			uploadData.put("content[" + i + "]", food.get(i).get("number"));
		}
		ArrayList<Map<String, String>> burden= UtilString.getListMapByJson(dishData.getBurden());
		for(int i=0;i<burden.size();i++) {
			uploadData.put("seasoning[" + i + "]", burden.get(i).get("name"));
			uploadData.put("content2[" + i + "]", burden.get(i).get("number"));
		}
		ArrayList<Map<String, String>> makes= UtilString.getListMapByJson(dishData.getMakes());
		for(int i=0;i<makes.size();i++) {
			img=getImgFromPool(dishData.getUploadTimeCode(),imgType_makeImg,makes.get(i).get("makesImg"));
			if(img.equals(STATE_UPLOADING)){
				uploadData.put(STATE_UPLOADING, "第"+(i+1)+"步骤图正在上传");
			}
			if(img.equals(STATE_FAILED)) {
				uploadData.put(STATE_FAILED, "菜谱第"+(i+1)+"步步骤图上传失败，请重新发布");
			}
			uploadData.put("makeId[" + i + "]", makes.get(i).get("makesStep"));
			uploadData.put("makeInfo[" + i + "]", makes.get(i).get("makesInfo"));
			uploadData.put("makeImg[" + i + "]", img);
		}
		return uploadData;
	}
	
	/**
	 * 把所有发布中的普通菜谱变为草稿
	 * 把所有发布中的视频菜谱变为暂停
	 */
	public void updataAllUploadingDish(final Context context){
		new Thread(){
			@Override
			public void run() {
				super.run();
				// 更新uploadDish中发菜谱的状态
				UploadDishSqlite sqlite = new UploadDishSqlite(context);
				ArrayList<Map<String, String>> listDrafts = sqlite.getAllIngDataInDB();
				// 判断是否有菜谱在发布
				for (int i = 0; i < listDrafts.size(); i++) {
					Map<String, String> maps = listDrafts.get(i);
					if("2".equals(maps.get("videoType")))
						sqlite.update(Integer.parseInt(maps.get("id")), UploadDishData.ds_dishType, UploadDishData.UPLOAD_PAUSE);
					else
						sqlite.update(Integer.parseInt(maps.get("id")), UploadDishData.ds_dishType, UploadDishData.UPLOAD_DRAF);
				}
			}
		}.start();
	}

	@Override
	public String getUploadAPi(UploadData data) {
		return StringManager.api_uploadDish;
	}

}
