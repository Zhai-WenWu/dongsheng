package third.mall.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.XHClick;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.activity.CommodDetailActivity;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.bean.ProductBean;
import third.mall.override.MallBaseActivity;
import xh.basic.internet.UtilInternet;

import static com.xiangha.R.id.shopping_item_commod_rela;

public class MallShopProductView extends ViewItemBase{

	private ImageView shopping_item_commod_choose;
	private ImageView shopping_item_commod_iv;
	private TextView shopping_item_commod_text;
	private TextView shopping_item_commod_price;
	private TextView shopping_item_commod_none;
	private RelativeLayout shopping_item_commod_cut;
	private TextView shopping_item_commod_num;
	private RelativeLayout shopping_item_commod_add;
	private InterProudct interProudct;
	private int max_sale_num=0;
	private  int saleable_num=0;
	private ProductBean bean_product;
	private Context context;
	private String url="";
	private String mall_stat_statistic;
	public MallShopProductView(Context context) {
		super(context);
		this.context= context;
		LayoutInflater.from(context).inflate(R.layout.a_mall_shopping_listview_item_new, this, true);
		setView();
	}
	
	/**
	 * 设置接口回调
	 */
	public void setInterface(InterProudct interProudct){
		this.interProudct= interProudct;
	}
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	public void setView( ){
		shopping_item_commod_choose = (ImageView) findViewById(R.id.shopping_item_commod_choose);
		shopping_item_commod_iv = (ImageView) findViewById(R.id.shopping_item_commod_iv);
		shopping_item_commod_text = (TextView) findViewById(R.id.shopping_item_commod_texts);
		shopping_item_commod_price = (TextView) findViewById(R.id.shopping_item_commod_price);
		shopping_item_commod_none = (TextView) findViewById(R.id.shopping_item_commod_none);
		shopping_item_commod_cut = (RelativeLayout) findViewById(R.id.shopping_item_commod_cut);
		shopping_item_commod_num = (TextView) findViewById(R.id.shopping_item_commod_num);
		shopping_item_commod_add = (RelativeLayout) findViewById(R.id.shopping_item_commod_add);
		findViewById(shopping_item_commod_rela).setVisibility(View.VISIBLE);
		
	}
	public void setValue(ProductBean bean){
		this.bean_product= bean;
		if("2".equals(bean_product.getStock_flag())){
			int num= Integer.parseInt(bean_product.getNum());
			int saleable_num= Integer.parseInt(bean_product.getSaleable_num());
			if(num>=saleable_num){
				bean_product.setNum(String.valueOf(saleable_num));
				shopping_item_commod_none.setText("(仅剩"+saleable_num+"件)");
				shopping_item_commod_none.setVisibility(View.VISIBLE);
			}else{
				shopping_item_commod_none.setVisibility(View.GONE);
			}
		}else{
			shopping_item_commod_none.setVisibility(View.GONE);
		}
		//设置图片
		setViewImage(shopping_item_commod_iv, bean_product.getImg());
		shopping_item_commod_text.setText(bean_product.getTitle());
		shopping_item_commod_price.setText("¥"+bean_product.getDiscount_price());
		shopping_item_commod_num.setText(bean_product.getNum());
		max_sale_num= Integer.parseInt(bean_product.getMax_sale_num());
		//商品状态
		saleable_num= Integer.parseInt(bean_product.getSaleable_num());
		
//		setCommodState(saleable_num);
		if(bean_product.getEdit_product()){
			shopping_item_commod_choose.setEnabled(true);
		}else {
		}
		if(bean_product.getChoose_state()){
			shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
		}else {
			shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
		}
		//设置当前状态
		
		//选中点击
		shopping_item_commod_choose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(bean_product.getChoose_state()){//取消
					bean_product.setChoose_state(false);
					shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
				}else {//选中
					bean_product.setChoose_state(true);
					shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
				}
				interProudct.setChangeSucess();
			}
		});
		//减少数量
		shopping_item_commod_cut.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int num_now=Integer.parseInt(bean_product.getNum());
				if(num_now<=1){
				}else{
					num_now--;
					bean_product.setNum(String.valueOf(num_now));
					updateCartInfo(false);
				}
				
			}
		});
		//增加数量
		shopping_item_commod_add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int num_now=Integer.parseInt(bean_product.getNum());
				if(saleable_num>0&&num_now<saleable_num){//达到最大购买数量
					if(max_sale_num>0&&num_now>=max_sale_num){
						Tools.showToast(context, "该单品最大可购买"+num_now+"件");
					}else{
						num_now++;
						bean_product.setNum(String.valueOf(num_now));
						updateCartInfo(true);
					}
					
				}else{
					Tools.showToast(context, "该单品最大可购买"+num_now+"件");
				}
			}
		});
		//点击事件
		shopping_item_commod_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 setStatisticIndex();
				 XHClick.mapStat(context, "a_mail_shopping_cart","商品","");
				 Intent intent= new Intent(context,CommodDetailActivity.class);
				 intent.putExtra("product_code", bean_product.getCode());
				if(context instanceof MallBaseActivity) {
					intent.putExtra(MallBaseActivity.PAGE_FROM, ((MallBaseActivity) context).getNowFrom());
				}
				context.startActivity(intent);
			}
		});
		shopping_item_commod_text.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 setStatisticIndex();
				 XHClick.mapStat(context, "a_mail_shopping_cart","商品","");
				 Intent intent= new Intent(context,CommodDetailActivity.class);
				 intent.putExtra("product_code", bean_product.getCode());
				if(context instanceof MallBaseActivity) {
					intent.putExtra(MallBaseActivity.PAGE_FROM, ((MallBaseActivity) context).getNowFrom());
				}
				 context.startActivity(intent);
			}
		});
	}

	/**
	 * 修改商品数量
	 */
	private void updateCartInfo(final boolean state){
		setStatisticIndex();
		shopping_item_commod_cut.setEnabled(false);
		shopping_item_commod_add.setEnabled(false);
		String param="product_code="+bean_product.getCode()+"&product_num="+bean_product.getNum();
		MallReqInternet.in().doPost(MallStringManager.mall_updateCartInfo, param, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					XHClick.mapStat(context, "a_mail_shopping_cart","数量 + -","");
					shopping_item_commod_none.setVisibility(View.GONE);
					if(!bean_product.getChoose_state()){
						if(bean_product.getChoose_state()){//取消
							bean_product.setChoose_state(false);
							shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
						}else {//选中
							bean_product.setChoose_state(true);
							shopping_item_commod_choose.setImageResource(R.drawable.z_mall_shopcat_choose);
						}
					}
					if(interProudct!=null){
						interProudct.setChangeSucess();
					}
					
					
				}else if(flag==UtilInternet.REQ_CODE_ERROR){
					Map<String,String> map= (Map<String, String>) msg;
					Tools.showToast(context, map.get("msg")+"");
					int num_now=Integer.parseInt(bean_product.getNum());
					if(state){
						num_now--;
					}else{
						num_now++;
					}
					bean_product.setNum(String.valueOf(num_now));
				}else{
					int num_now=Integer.parseInt(bean_product.getNum());
					if(state){
						num_now--;
					}else{
						num_now++;
					}
					bean_product.setNum(String.valueOf(num_now));
				}
				shopping_item_commod_num.setText(bean_product.getNum());
				shopping_item_commod_cut.setEnabled(true);
				shopping_item_commod_add.setEnabled(true);
			
			}
		});
	}
	/**
	 * 商品回调接口
	 * @author Administrator
	 *
	 */
	public interface InterProudct{
		/**
		 * 商品回调
		 */
		public abstract void setChangeSucess();
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		if(!TextUtils.isEmpty(url)){
			MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
		}
	}
}
