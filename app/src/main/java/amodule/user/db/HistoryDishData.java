package amodule.user.db;

/**
 * PackageName : amodule.user.db
 * Created by MrTrying on 2016/8/17 11:59.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistoryDishData {
	private long browseTime = 0L;
	private String name;
	private String imgUrl;
	private String code;
	private String allClick;
	private String burdens;
	private String favorites;
	private String hasVideo;
	private String isFine;
	private String isMakeImg;

	public long getBrowseTime() {
		return browseTime;
	}

	public void setBrowseTime(long browseTime) {
		this.browseTime = browseTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAllClick() {
		return allClick;
	}

	public void setAllClick(String allClick) {
		this.allClick = allClick;
	}

	public String getBurdens() {
		return burdens;
	}

	public void setBurdens(String burdens) {
		this.burdens = burdens;
	}

	public String getFavorites() {
		return favorites;
	}

	public void setFavorites(String favorites) {
		this.favorites = favorites;
	}

	public String getHasVideo() {
		return hasVideo;
	}

	public void setHasVideo(String hasVideo) {
		this.hasVideo = hasVideo;
	}

	public String getIsFine() {
		return isFine;
	}

	public void setIsFine(String isFine) {
		this.isFine = isFine;
	}

	public String getIsMakeImg() {
		return isMakeImg;
	}

	public void setIsMakeImg(String isMakeImg) {
		this.isMakeImg = isMakeImg;
	}
}
