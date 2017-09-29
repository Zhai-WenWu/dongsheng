package third.mall.bean;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;

public class MerchantBean {

	//选择状态
	private boolean choose_state_shop=true;//是否选中 false--未选中 true--选中
	private boolean edit_shop= false;// false--结算 true--编辑
	public ArrayList<ProductBean> list_product;
	public ShopBean shop_bean;
	private int num_product;
	public int getNum_product() {
		return num_product;
	}

	public void setNum_product(int num_product) {
		this.num_product = num_product;
	}
	public boolean isChoose_state_shop() {
		return choose_state_shop;
	}

	public void setChoose_state_shop(boolean choose_state_shop) {
		this.choose_state_shop = choose_state_shop;
	}

	public boolean isEdit_shop() {
		return edit_shop;
	}

	public void setEdit_shop(boolean edit_shop) {
		this.edit_shop = edit_shop;
	}

	/**
	 * 初始化数据
	 * @param map
	 */
	public void setData(Map<String,String> map){
		list_product= new ArrayList<ProductBean>();
		ArrayList<Map<String,String>> list= UtilString.getListMapByJson(map.get("product_list"));
		for (int i = 0,size=list.size(); i < size; i++) {
			ProductBean bean= new ProductBean();
			bean.setProductData(list.get(i));
			list_product.add(bean);
		}
		ArrayList<Map<String,String>> list_shop= UtilString.getListMapByJson(map.get("shop_info"));
		shop_bean= new ShopBean();
		shop_bean.setDataShop(list_shop.get(0));
		getListNumProduct();
	}
	
	/**
	 * 获取数量
	 */
	public void getListNumProduct(){
		int num= 0;
		if(list_product.size()>0){
			for (int i = 0,size= list_product.size(); i < size; i++) {
				if(edit_shop){//编辑状态下
					if(list_product.get(i).getChoose_state())
						num+=1;
				}else{//结算状态下
					if(list_product.get(i).getChoose_state())
						num+=Integer.parseInt(list_product.get(i).getNum());
				}
			}
		}
		setNum_product(num);
	}
	/**
	 * 获取当前数据
	 * @return
	 */
	public String getMerchantPrice(){
		float price = 0;
		if(list_product.size()>0){
			for (int i = 0,size=list_product.size(); i < size; i++) {
				if(list_product.get(i).getChoose_state()){
					if(!TextUtils.isEmpty(list_product.get(i).getFavor_sale_num())){//当前有优惠数量
						float sale_num = Float.parseFloat(list_product.get(i).getFavor_sale_num());
						float num = Float.parseFloat(list_product.get(i).getNum());
						if(sale_num>=num||"0".equals(list_product.get(i).getFavor_sale_num())){//优惠数量小于当前选择数量 或 当数据为0不限优惠价格
							price += Float.parseFloat(list_product.get(i).getFavor_sale_price()) * num;
						}else {
							price += Float.parseFloat(list_product.get(i).getFavor_sale_price()) * sale_num +Float.parseFloat(list_product.get(i).getPrice())*(num-sale_num);
						}
					}else {
						price += Float.parseFloat(list_product.get(i).getPrice()) * Float.parseFloat(list_product.get(i).getNum());
					}

				}

			}
		}
		return ToolView.getNumberPart(String.valueOf(price));
	}
	/**
	 * 设置当前状态
	 */
	public void setNowChangeState(){
		if(list_product.size()>0){
			for (int i = 0,size= list_product.size(); i < size; i++) {
				if(!list_product.get(i).getChoose_state()){
					setChoose_state_shop(false);
					break;
				}else setChoose_state_shop(true);
			}
		}
	}
	/**
	 * 设置当前状态
	 */
	public void setClickChangeState(){
		if(choose_state_shop){//当前为选中
			choose_state_shop=false;
			if(list_product.size()>0){
				for (int i = 0,size=list_product.size(); i < size; i++) {
					list_product.get(i).setChoose_state(false);
				}
			}
			
		}else{//当前为不为选中
			choose_state_shop=true;
			if(list_product.size()>0){
				for (int i = 0,size=list_product.size(); i < size; i++) {
					list_product.get(i).setChoose_state(true);
				}
			}
		}
	}
	/**
	 * 设置当前选择状态 true全选，false 全不选
	 * @param state
	 */
	public  void setAllChangeState(boolean state,boolean edit_state){
		choose_state_shop=state;
		edit_shop= edit_state;
		if(list_product.size()>0){
			for (int i = 0,size=list_product.size(); i < size; i++) {
				list_product.get(i).setChoose_state(state);
			}
		}
		getListNumProduct();
	}
	/**
	 * 获取当前选中商品数量
	 */
	public int getNumChooseProduct(){
		int num= 0;
		if(list_product.size()>0){
			for (int i = 0,size=list_product.size(); i < size; i++) {
				if(list_product.get(i).getChoose_state())
					num+=1;
			}
		}
		return num;
	}
	/**
	 * 删除选中商品
	 */
	public void delChooseProduct(){
		if(list_product.size()>0){
			for (int i = 0; i < list_product.size();) {
				if(list_product.get(i).getChoose_state())
					list_product.remove(i);
				else i++;
			}
		}
	}
}
