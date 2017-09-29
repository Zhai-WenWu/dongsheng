package third.mall.bean;

import android.util.Log;

import java.util.Map;

/**
 * 商品bean
 * @author yujian
 *
 */
public class ProductBean {

	private String code;
	private String title;
	private String img;
	private String discount_price;
	private String num;
	private String max_sale_num;
	private String saleable_num;
	private String stock_flag;
	//新加的数据结构
	private String price;
	private String favor_sale_price;
	private String favor_sale_num;

	//当前是否可增加数量
	public boolean saleable_state=false;
	//获取当前商品的价格
	public void  getAllProductPrice(){
		
	}
	//当前是否可选中状态
	private boolean choose_state= true;
	public boolean getChoose_state() {
		return choose_state;
	}
	public void setChoose_state(boolean choose_state) {
		this.choose_state = choose_state;
	}
	//是否是可编辑状态
	private boolean edit_product=false;
	public boolean getEdit_product() {
		return edit_product;
	}
	public void setEdit_product(boolean edit_product) {
		this.edit_product = edit_product;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getDiscount_price() {
		return discount_price;
	}
	public void setDiscount_price(String discount_price) {
		this.discount_price = discount_price;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getMax_sale_num() {
		return max_sale_num;
	}
	public void setMax_sale_num(String max_sale_num) {
		this.max_sale_num = max_sale_num;
	}
	public String getSaleable_num() {
		return saleable_num;
	}
	public void setSaleable_num(String saleable_num) {
		this.saleable_num = saleable_num;
	}
	public String getStock_flag() {
		return stock_flag;
	}
	public void setStock_flag(String stock_flag) {
		this.stock_flag = stock_flag;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getFavor_sale_price() {
		return favor_sale_price;
	}

	public void setFavor_sale_price(String favor_sale_price) {
		this.favor_sale_price = favor_sale_price;
	}

	public String getFavor_sale_num() {
		return favor_sale_num;
	}

	public void setFavor_sale_num(String favor_sale_num) {
		this.favor_sale_num = favor_sale_num;
	}

	public ProductBean setProductData(Map<String,String> map){
		Log.i("wyl","商品数据：："+map.toString());
		setCode(map.get("code"));
		setTitle(map.get("title"));
		setImg(map.get("img"));
		setDiscount_price(map.get("discount_price"));
		setNum(map.get("num"));
		setMax_sale_num(map.get("max_sale_num"));
		setSaleable_num(map.get("saleable_num"));
		setStock_flag(map.get("stock_flag"));
		//现在新加优惠数据
		setFavor_sale_num(map.get("favor_sale_num"));
		setPrice(map.get("price"));
		setFavor_sale_price(map.get("favor_sale_price"));

//		setSale_num("2");
//		setPrice("8.8");
//		setSale_price("1.1");
		return this;
	} 
	
}
