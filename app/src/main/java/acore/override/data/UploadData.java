package acore.override.data;

import java.io.Serializable;

public class UploadData implements Serializable{
	private static final long serialVersionUID = -3714351142032325435L;
	/** 数据库primary key*/
	public int id = -1;
	/** code */
	public String code = "";
	/** 时间戳 */
	public long uploadTimeCode=0;
	
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
	public long getUploadTimeCode() {
		return uploadTimeCode;
	}
	public void setUploadTimeCode(long uploadTimeCode) {
		this.uploadTimeCode = uploadTimeCode;
	}
	
}
