package amodule.quan.db;

/**
 * Circle的信息数据
 * @author Eva
 *
 */
public class CircleData {
	public static final String TYPE_SKIP_TEXT = "1";
	public static final String TYPE_SKIP_PIC = "2";
	
	private String cid = "";
	private String name = "";
	private String rule = "";
	
	/**
	 * 发贴跳转位置
	 *  1、填写内容 
	 *  2、选择图片
	 *  */
	private String skip = "";
	private String img = "";
	private String info = "";
	private String customerNum = "";
	private String dayHotNum = "";
	
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public String getSkip() {
		return skip;
	}
	public void setSkip(String skip) {
		this.skip = skip;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getCustomerNum() {
		return customerNum;
	}
	public void setCustomerNum(String customerNum) {
		this.customerNum = customerNum;
	}
	public String getDayHotNum() {
		return dayHotNum;
	}
	public void setDayHotNum(String dayHotNum) {
		this.dayHotNum = dayHotNum;
	}
	
}
