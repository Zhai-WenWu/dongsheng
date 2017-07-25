package amodule.dish.db;

public class DishOffData {
	//healthStr健康功效,isFav,burden用料<content,name,code>,remark小贴士,subject大家正在发布的美食<>
	//--主数据库字段名;
	public static final  String bd_id = "id";
	public static final String bd_code="code";
	public static final String bd_name="name";
	public static final String bd_addTime="addTime";
	public static final String bd_json="json";
	
	private String id="";
	private String code="";
	private String name="";
	private String addTime="";
	private String json="";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}

}

