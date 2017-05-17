package third.mall.bean;

import java.util.Map;

/**
 * 店铺bean
 * @author yujian
 *
 */
public class ShopBean {
	private String shop_code;
	private String shop_name;
	private String shop_logo;
	//包邮描述
	private String shop_postage_desc;
	//满减描述
	private String shop_promotion_desc;
	//是否有优惠券可以领  1--没有，2---有
	private String shop_has_coupon;
	
	public String getShop_code() {
		return shop_code;
	}
	public void setShop_code(String shop_code) {
		this.shop_code = shop_code;
	}
	public String getShop_name() {
		return shop_name;
	}
	public void setShop_name(String shop_name) {
		this.shop_name = shop_name;
	}
	public String getShop_logo() {
		return shop_logo;
	}
	public void setShop_logo(String shop_logo) {
		this.shop_logo = shop_logo;
	}
	public String getShop_postage_desc() {
		return shop_postage_desc;
	}
	public void setShop_postage_desc(String shop_postage_desc) {
		this.shop_postage_desc = shop_postage_desc;
	}
	public String getShop_promotion_desc() {
		return shop_promotion_desc;
	}
	public void setShop_promotion_desc(String shop_promotion_desc) {
		this.shop_promotion_desc = shop_promotion_desc;
	}
	public String getShop_has_coupon() {
		return shop_has_coupon;
	}
	public void setShop_has_coupon(String shop_has_coupon) {
		this.shop_has_coupon = shop_has_coupon;
	}
	
	public ShopBean setDataShop(Map<String,String> map){
		setShop_code(map.get("shop_code"));
		setShop_name(map.get("shop_name"));
		setShop_logo(map.get("shop_logo"));
		setShop_has_coupon(map.get("shop_has_coupon"));
		setShop_postage_desc(map.get("shop_postage_desc"));
		setShop_promotion_desc(map.get("shop_promotion_desc"));
		return this;
	}
	
}
