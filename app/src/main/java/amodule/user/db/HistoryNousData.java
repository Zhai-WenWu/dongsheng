package amodule.user.db;

/**
 * PackageName : amodule.user.db
 * Created by MrTrying on 2016/8/17 13:45.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistoryNousData {
	private long browseTime = 0L;
	private String allClick;
	private String code;
	private String content;
	private String img;
	private String soruce;
	private String title;

	public long getBrowseTime() {
		return browseTime;
	}

	public void setBrowseTime(long browseTime) {
		this.browseTime = browseTime;
	}

	public String getAllClick() {
		return allClick;
	}

	public void setAllClick(String allClick) {
		this.allClick = allClick;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getSoruce() {
		return soruce;
	}

	public void setSoruce(String soruce) {
		this.soruce = soruce;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
