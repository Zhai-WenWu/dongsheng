package third.mall.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallStringManager;
import third.mall.bean.MerchantBean;
import third.mall.dialog.FavorableDialog;
import third.mall.dialog.FavorableDialog.showCallBack;
import third.mall.view.MallShopProductView.InterProudct;
import xh.basic.tool.UtilFile;

public class MallShopMerchantView extends RelativeLayout{

	private ImageView choose_iv_boss;
	private ImageView shopping_logo_iv;
	private TextView shopping_logo_tv,get_favorable_tv;
	private RelativeLayout listview_money_rela;
	private TextView money_num_tv;
	private ViewPromotion view_promotion;
	private FavorableDialog dialog;
	private static final String SHOP_TYPE="shop_type";
	private static final String DIALOG_TYPE="dialog_type";
	private MerchantBean bean_merchant;
	private Activity context;
	private MallShopProductAllView productAllView;
	private InterProudct interProudct;
	private String url="";
	private String mall_stat_statistic;
	public MallShopMerchantView(Activity context) {
		super(context);
		this.context= context;
		LayoutInflater.from(context).inflate(R.layout.a_mall_shopping_listview_new, this, true);
		setView();
	}
	private void setView(){
		choose_iv_boss=(ImageView) findViewById(R.id.choose_iv_boss);
		shopping_logo_iv=(ImageView)findViewById(R.id.shopping_logo_iv);
		shopping_logo_tv = (TextView) findViewById(R.id.shopping_logo_tv);
		listview_money_rela = (RelativeLayout) findViewById(R.id.listview_money_rela);
		money_num_tv = (TextView) findViewById(R.id.money_num_tv);
		view_promotion=(ViewPromotion)findViewById(R.id.view_promotion);
		get_favorable_tv =(TextView) findViewById(R.id.get_favorable_tv);
		productAllView=(MallShopProductAllView) findViewById(R.id.productallview);
//		listview_shopping=(ListViewForScrollView) findViewById(R.id.listview_shopping);
	}
	public void setInterface(InterProudct interProudct){
		this.interProudct= interProudct;
	}
	
	public void setData(MerchantBean bean,String url,String mall_stat_statistic){
		this.bean_merchant= bean;
		this.url= url;
		this.mall_stat_statistic=mall_stat_statistic;
		//展示商家
		String shop_name=bean_merchant.shop_bean.getShop_name();
		if( "2".equals(bean_merchant.shop_bean.getShop_has_coupon())){
			get_favorable_tv.setVisibility(View.VISIBLE);
			findViewById(R.id.favorable_line).setVisibility(View.VISIBLE);
		}else{ 
			get_favorable_tv.setVisibility(View.GONE);
			findViewById(R.id.favorable_line).setVisibility(View.GONE);
		}
		
		view_promotion.setStyle(ViewPromotion.style_all);
		view_promotion.setChangeStyle();
		String shop_postage_desc= bean_merchant.shop_bean.getShop_postage_desc();
		String shop_promotion_desc= bean_merchant.shop_bean.getShop_promotion_desc();
		if(!TextUtils.isEmpty(shop_postage_desc)||!TextUtils.isEmpty(shop_promotion_desc)){
			view_promotion.setVisibility(View.VISIBLE);
			view_promotion.setData(shop_postage_desc, shop_promotion_desc);
		}else view_promotion.setVisibility(View.GONE);
		
		shopping_logo_tv.setText(shop_name);
		setShopLogo(shop_name, shopping_logo_iv);
		
		setChangeState();
		
		if(bean_merchant.isEdit_shop()){
			listview_money_rela.setVisibility(View.GONE);
		}else{
			listview_money_rela.setVisibility(View.VISIBLE);
		}
		if(bean_merchant.isEdit_shop()){//删除状态
			choose_iv_boss.setEnabled(true);
		}
		setProductView();
		//选中点击
		choose_iv_boss.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				bean_merchant.setClickChangeState();
				setProductView();
				setChangeState();
				bean_merchant.getListNumProduct();
				if(interProudct!=null){
					interProudct.setChangeSucess();
				}
			}
		});
		
		setClickListener(shopping_logo_iv, SHOP_TYPE);
		setClickListener(shopping_logo_tv, SHOP_TYPE);
		setClickListener(get_favorable_tv, DIALOG_TYPE);
	}
	/**
	 * 设置商品view
	 */
	private void setProductView(){
//		if(productAllView.getTag()==null||!productAllView.getTag().equals(bean_merchant.shop_bean.getShop_code())){
			
			productAllView.setTag(bean_merchant.shop_bean.getShop_code());
			productAllView.setInterface(new InterProudct() {
				@Override
				public void setChangeSucess() {
					money_num_tv.setText("¥"+bean_merchant.getMerchantPrice());
					bean_merchant.setNowChangeState();
					bean_merchant.getListNumProduct();
					setChangeState();
					if(interProudct!=null){
						interProudct.setChangeSucess();
					}
					
				}
			});
			productAllView.setData(bean_merchant.list_product,url,mall_stat_statistic);
//		}else{
//			productAllView.setChangeView(bean_merchant.list_product);
//		}
		money_num_tv.setText("¥"+bean_merchant.getMerchantPrice());
		
	}
	/**
	 * 设置当前选中状态
	 */
	private void setChangeState(){
		if(bean_merchant.isChoose_state_shop()){
			choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_choose);
		}else {
			choose_iv_boss.setImageResource(R.drawable.z_mall_shopcat_no_choose);
		}
	}
	/**
	 * 设置商家头像
	 * @param name
	 * @param view
	 */
	private void setShopLogo(String name,ImageView view){
		if(name.contains("香哈")){
			view.setBackgroundResource(R.drawable.mall_myorder_myself);
		}else{
			view.setBackgroundResource(R.drawable.mall_buycommod_commod_merchant_iv);
		}
	}
	
	private void setClickListener(View view,final String type){
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(type.equals(SHOP_TYPE)){//商家
					XHClick.mapStat(context, "a_mail_shopping_cart","店铺","");
					MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
					String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
					String url=MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home)+"?shop_code="+bean_merchant.shop_bean.getShop_code()+"&"+mall_stat;
					AppCommon.openUrl(context, url, true);
				}else if(type.equals(DIALOG_TYPE)){//领券
					XHClick.mapStat(context, "a_mail_shopping_cart","领券","");
					if(dialog==null){
						dialog= new FavorableDialog(context,bean_merchant.shop_bean.getShop_code());
						dialog.setCallBack(new showCallBack() {
							
							@Override
							public void setShow() {
								dialog.show();
							}
						});
					}else{
						dialog.show();
					}
				}
			}
		});
	}

}
