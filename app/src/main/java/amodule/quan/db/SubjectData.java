package amodule.quan.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.override.data.UploadData;
import acore.tools.StringManager;
import amodule.quan.view.ImgTextCombineLayout;

/**
 * 美食贴数据
 */
public class SubjectData extends UploadData {
	private static final long serialVersionUID = 3685228578020151014L;
	
	/** 发布成功 */
	public static final int UPLOAD_SUCCESS = 3000;
	/** 发布中 */
	public static final int UPLOAD_ING = 3001;
	/** 后台发布 */
	public static final int UPLOAD_BACKSTAGE_ING = 3002;
	/** 草稿 */
	public static final int UPLOAD_DRAF = 3003;
	/** 发布失败 */
	public static final int UPLOAD_FAIL = 3004;
	/** 发贴 */
	public static final String TYPE_UPLOAD = "upload";
	/** 带图回复 */
	public static final String TYPE_REPLY = "reply";
	/** 表示不可以 */
	private static final int FALSE = 0;
	/** 表示可以 */
	private static final int TRUE = 1;

	/** 标题*/
	private String title = "";
	/** 内容图文混排内容 */
	private String contentJson = "";
	/** 内容图文混排内容的数据集合 */
	private ArrayList<Map<String,String>> contentArray = new ArrayList<>();

	private String video = "";

	private String videoLocalPath = "";

	private String videoSImg = "";

	private String videoSImgLocal = "";

	private String videoType = "";
	/** 所属圈子 */
	private String cid = "";
	/** 所属圈子的板块 */
	private String mid = "";
	/** 上传状态 */
	private int uploadState = 0;
	/** 插入数据的时间,单位ms */
	private long addTime = 0;
	private int titleCanModify = TRUE;
	/** 位置 */
	private String location = "";
	/** 发布贴子类型
	 * 发贴 upload
	 * 带图回复 reply
	 * */
	private String type = "";
	/** 圈子的name */
	private String circleName = "";
	/** 关联菜谱的code */
	private String dishCode = "";
	/**
	 * 是否显示定位
	 * 1.不显示
	 * 2.显示
	 */
	private String isLocation = "2";
	
	public SubjectData(){
		this.uploadTimeCode = System.currentTimeMillis();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContentJson() {
		return contentJson;
	}
	public void setContentJson(String contentJson) {
		this.contentJson = contentJson;
		contentArray = StringManager.getListMapByJson(contentJson);
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public int getUploadState() {
		return uploadState;
	}
	public void setUploadState(int uploadState) {
		this.uploadState = uploadState;
	}
	public long getAddTime() {
		return addTime;
	}
	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}
	public int getTitleCanModifyInteger(){
		return titleCanModify;
	}
	public boolean getTitleCanModify() {
		return titleCanModify == TRUE;
	}
	public void setTitleCanModify(int titleCanModify) {
		this.titleCanModify = titleCanModify;
	}
	public void setTitleCanModify(boolean titleCanModify) {
		this.titleCanModify = titleCanModify ? TRUE : FALSE;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public ArrayList<Map<String, String>> getContentArray() {
		return contentArray;
	}
	public void setContentArray(ArrayList<Map<String, String>> contentArray) {
		this.contentArray = contentArray;
		contentJson = getjsonArray(contentArray);
	}
	
	/**
	 * 数据转json.toString
	 * 
	 * @param list
	 */
	private String getjsonArray(ArrayList<Map<String, String>> list) {
		JSONArray jsonArray = new JSONArray();
		try {
			for (int i = 0; i < list.size(); i++) {
				Map<String, String> map= list.get(i);
				JSONObject jsonObject= new JSONObject();
				jsonObject.put(ImgTextCombineLayout.IMGEURL, map.get(ImgTextCombineLayout.IMGEURL));
				jsonObject.put(ImgTextCombineLayout.CONTENT, map.get(ImgTextCombineLayout.CONTENT));
				jsonArray.put(jsonObject);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray.toString();
	}
	/**
	 * 获取上传类型 
	 * @return
	 */
	public String getType() {
		return type;
	}
	/** 设置上传类型 */
	public void setType(String type) {
		this.type = type;
	}
	public String getCircleName() {
		return circleName;
	}
	public void setCircleName(String circleName) {
		this.circleName = circleName;
	}
	public void setDishCode(String dishCode){
		this.dishCode = dishCode;
	}
	public String getDishCode(){
		return dishCode;
	}
	public String getIsLocation(){
		return isLocation;
	}
	/**
	 * 设置定位是否显示
	 * @param isLocation
	 * 1.不显示    2.显示
	 */
	public void setIsLocation(String isLocation){
		this.isLocation = isLocation;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getVideoSImg() {
		return videoSImg;
	}

	public void setVideoSImg(String videoSImg) {
		this.videoSImg = videoSImg;
	}

	public String getVideoType() {
		return videoType;
	}

	public void setVideoType(String videoType) {
		this.videoType = videoType;
	}

	public String getVideoLocalPath() {
		return videoLocalPath;
	}

	public void setVideoLocalPath(String videoLocalPath) {
		this.videoLocalPath = videoLocalPath;
	}

	public String getVideoSImgLocal() {
		return videoSImgLocal;
	}

	public void setVideoSImgLocal(String videoSImgLocal) {
		this.videoSImgLocal = videoSImgLocal;
	}
}
