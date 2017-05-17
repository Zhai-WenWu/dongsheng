package amodule.user.db;

/**
 * PackageName : amodule.user.db
 * Created by MrTrying on 2016/8/17 13:51.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistoryData {
	public static final String _id = "id";
	public static final String _browseTime = "browseTime";
	public static final String _dataJson = "dataJson";
	public static final String _code = "code";


	private int id;
	private long browseTime = 0L;
	private String code;
	private String dataJson;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public long getBrowseTime() {
		return browseTime;
	}

	public void setBrowseTime(long browseTime) {
		this.browseTime = browseTime;
	}

	public String getDataJson() {
		return dataJson;
	}

	public void setDataJson(String dataJson) {
		this.dataJson = dataJson;
	}
}
