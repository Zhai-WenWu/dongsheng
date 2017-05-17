package amodule.quan.db;

import java.io.Serializable;

/**
 * circle中的板块data
 * @author Eva
 *
 */
public class PlateData implements Serializable{
	private static final long serialVersionUID = 5289902545856633545L;
	private String cid = "";
	private String mid = "";
	private String name = "";
	private int position = -1;
	private boolean isShowAd = false;
	private boolean isShowAllQuan = false;
	private boolean isShowScrollTop = false;
	private boolean isShowRecUser = false;
	private String stiaticID = "";

	/**
	 * 1、不定位
	 *	2、定位
	 */
	private boolean isLocation = false;
	@SuppressWarnings("unused")
	public static final String UNLOCATION = "1";
	public static final String LOCATION = "2";
	
	public String getCid(){
		return cid;
	}
	public void setCid(String cid){
		this.cid = cid;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isShowAd() {
		return isShowAd;
	}

	public void setShowAd(boolean showAd) {
		isShowAd = showAd;
	}

	public boolean isShowAllQuan() {
		return isShowAllQuan;
	}

	public void setShowAllQuan(boolean showAllQuan) {
		isShowAllQuan = showAllQuan;
	}

	public boolean isShowScrollTop() {
		return isShowScrollTop;
	}

	public void setShowScrollTop(boolean showScrollTop) {
		isShowScrollTop = showScrollTop;
	}

	public boolean isShowRecUser() {
		return isShowRecUser;
	}

	public void setShowRecUser(boolean showRecUser) {
		isShowRecUser = showRecUser;
	}

	public String getStiaticID() {
		return stiaticID;
	}

	public void setStiaticID(String stiaticID) {
		this.stiaticID = stiaticID;
	}

	/**
	 * true定位 	false 不定位
	 * @return
	 */
	public boolean isLocation() {
		return isLocation;
	}
	/**
	 * 1、不定位  	2、定位 
	 * @param isLocation
	 */
	public void setLocation(String isLocation){
		this.isLocation = LOCATION.equals(isLocation);
	}
	/**
	 * true定位 	false 不定位
	 * @param isLocation
	 */
	public void setLocation(boolean isLocation) {
		this.isLocation = isLocation;
	}
}
