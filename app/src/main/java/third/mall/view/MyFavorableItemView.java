package third.mall.view;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.tool.UtilFile;

public class MyFavorableItemView extends ViewItemBase{

	private Activity context;
	private Map<String,String> map;
	private RelativeLayout item_rela,item_rela_line;
	private ImageView item_rela_line_iv,image_state;
	private TextView item_money_tv,item_money_rule_tv,item_tv_money_sign;
	private TextView item_tv_shop_name,item_tv_shop_time,item_show_line_tv;
	private View image_line;
	private RelativeLayout item_rela_shop;
	private String url;
	private String mall_stat_statistic;
	public MyFavorableItemView(Activity context) {
		super(context);
		this.context=context;
		initView();
	}
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.item_mall_favorable_my, this, true);
		item_rela=(RelativeLayout) findViewById(R.id.item_rela);
		item_rela_shop=(RelativeLayout) findViewById(R.id.item_rela_shop);
		item_rela_line=(RelativeLayout) findViewById(R.id.item_rela_line);
		item_rela_line_iv = (ImageView) findViewById(R.id.item_rela_line_iv);
		//显示金额
		item_tv_money_sign=(TextView) findViewById(R.id.item_tv_money_sign);
		item_money_tv=(TextView) findViewById(R.id.item_money_tv);
		item_money_rule_tv=(TextView) findViewById(R.id.item_money_rule_tv);
		//竖线
//		item_imageview_line=(ImageView) findViewById(R.id.item_imageview_line);
		image_line=findViewById(R.id.image_line);
		//商家名称
		item_tv_shop_name=(TextView) findViewById(R.id.item_tv_shop_name);
		item_tv_shop_time= (TextView) findViewById(R.id.item_tv_shop_time);
		image_state=(ImageView) findViewById(R.id.image_state);
		item_show_line_tv=(TextView) findViewById(R.id.item_show_line_tv);
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRequest();
			}
		});
		setFavorableState(false);
	}
	
	public void setUrl(String url,String mall_stat_statistic){
		this.url= url;
		this.mall_stat_statistic= mall_stat_statistic;
	}
	public void setData(final Map<String,String> map, final String id){
		this.map=map;
		item_money_tv.setText(map.get("coupon_amt"));
		item_money_rule_tv.setText("满"+map.get("order_amt_reach")+"元可用");
		item_tv_shop_name.setText(map.get("shop_name"));
		item_tv_shop_time.setText(map.get("start_time")+"-"+map.get("end_time"));
		if("3".equals(id)){
			setFavorableState(false);
		}else{
			setFavorableState(true);
		}
		item_rela_shop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setStatictis(id);
				MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
				String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code")+"&"+mall_stat;
				AppCommon.openUrl(context, url, true);
			}
		});
		findViewById(R.id.back_go).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setStatictis(id);
				MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
				String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
				String url = MallStringManager.replaceUrl(MallStringManager.mall_web_shop_home) + "?shop_code=" + map.get("shop_code")+"&"+mall_stat;
				AppCommon.openUrl(context, url, true);
			}
		});
	}
	
	private void setRequest(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
		String param="shop_coupon_package_code="+map.get("shop_coupon_package_code");
		MallReqInternet.in().doPost(MallStringManager.mall_getAShopCoupon, param, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					Tools.showToast(context, "领取成功");
				}
			
			}
		});
	}

	/**
	 * 友盟统计
	 * @param id
     */
	private void setStatictis(String id){
		String state_index="";
		if(id.equals("1")){
			state_index= "未使用";
		}else if(id.equals("2")){
			state_index="已使用";
		}else if(id.equals("3")){
			state_index="已过期";
		}
		XHClick.mapStat(context, "a_mail_coupon",state_index,"点击优惠券");
	}
	/**
	 * 优惠券不同状态
	 * @param state
	 */
	private void setFavorableState(boolean state){
		RelativeLayout.LayoutParams layout= new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		if(state){
//			int dp_34= 
//			layout.setMargins(left, top, right, bottom);
//			item_rela_shop.r
			String color = Tools.getColorStr(getContext(),R.color.comment_color);
			item_rela.setBackgroundResource(R.drawable.bg_mall_shop_favorable_item_my);
			item_rela_line.setBackgroundColor(Color.parseColor("#fffffe"));
			item_rela_line_iv.setBackgroundResource(R.drawable.mall_favorable_dialog_normal_bg);
			item_tv_money_sign.setTextColor(Color.parseColor(color));
			item_money_tv.setTextColor(Color.parseColor(color));
			item_money_rule_tv.setTextColor(Color.parseColor("#333333"));
			image_line.setBackgroundResource(R.drawable.mall_favorable_dialog_line_void_bg);
			item_tv_shop_name.setTextColor(Color.parseColor("#333333"));
			item_tv_shop_time.setTextColor(Color.parseColor("#333333"));
			item_show_line_tv.setBackgroundColor(Color.parseColor("#fffffe"));
		}else{
			item_rela.setBackgroundResource(R.drawable.bg_mall_shop_favorable_item_my);
			item_rela_line.setBackgroundColor(Color.parseColor("#fffffe"));
			item_rela_line_iv.setBackgroundResource(R.drawable.mall_favorable_dialog_void_bg);
			item_tv_money_sign.setTextColor(Color.parseColor("#cccccc"));
			item_money_tv.setTextColor(Color.parseColor("#cccccc"));
			item_money_rule_tv.setTextColor(Color.parseColor("#cccccc"));
			image_line.setBackgroundResource(R.drawable.mall_favorable_dialog_line_void_bg);
			item_tv_shop_name.setTextColor(Color.parseColor("#cccccc"));
			item_tv_shop_time.setTextColor(Color.parseColor("#cccccc"));
			item_show_line_tv.setBackgroundColor(Color.parseColor("#fffffe"));
		}
	}
}
