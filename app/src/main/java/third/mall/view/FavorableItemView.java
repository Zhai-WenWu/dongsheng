package third.mall.view;

import java.util.Map;

import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import acore.tools.Tools;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aplug.basic.ReqInternet;

import com.xiangha.R;

public class FavorableItemView extends ViewItemBase{

	private Context context;
	private Map<String,String> map;
	private RelativeLayout item_rela,item_rela_line;
	private ImageView item_rela_line_iv,image_state;
	private TextView item_money_tv,item_money_rule_tv,item_tv_money_sign;
	private TextView item_tv_shop_name,item_tv_shop_time,item_show_line_tv;
	private View image_line;
	private interfaceCallBack callback;
	public FavorableItemView(Context context) {
		super(context);
		this.context=context;
		initView();
	}
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.item_mall_favorable_dialog, this, true);
		item_rela=(RelativeLayout) findViewById(R.id.item_rela);
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
	
	public void setData(Map<String,String> map,interfaceCallBack callback){
		this.map=map;
		this.callback= callback;
		item_money_tv.setText(map.get("coupon_amt"));
		item_money_rule_tv.setText("满"+map.get("order_amt_reach")+"元可用");
		item_tv_shop_name.setText(map.get("shop_name"));
		item_tv_shop_time.setText(map.get("start_time")+"-"+map.get("end_time"));
		image_state.setVisibility(View.GONE);
		
		if(map.containsKey("remain_amount")&&!TextUtils.isEmpty(map.get("remain_amount"))&&Integer.parseInt(map.get("remain_amount"))<=0){
			setFavorableState(false);
			image_state.setVisibility(View.VISIBLE);
			image_state.setBackgroundResource(R.drawable.mall_favorable_dialog_hasgone);
		}else{
			if(map.containsKey("already_have")&&"2".equals(map.get("already_have"))){
				setFavorableState(false);
				image_state.setVisibility(View.VISIBLE);
				image_state.setBackgroundResource(R.drawable.mall_favorable_dialog_have);
			}else {
				setFavorableState(true);
			}
		}
		
		
	}
	
	private void setRequest(){
		String param="shop_coupon_package_code="+map.get("shop_coupon_package_code");
		MallReqInternet.in().doPost(MallStringManager.mall_getAShopCoupon, param, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					Tools.showToast(context, "领取成功");
					if(callback!=null){
						callback.sucess();
					}
				}else if (flag==ReqInternet.REQ_CODE_ERROR && msg instanceof Map ){
					Map<String,String> map=(Map<String, String>) msg;
					Tools.showToast(context, map.get("msg"));
				}
			
			}
		});
	}
	/**
	 * 优惠券不同状态
	 * @param state
	 */
	private void setFavorableState(boolean state){
		String color = Tools.getColorStr(context,R.color.comment_color);
		if(state){
			item_rela.setBackgroundResource(R.drawable.bg_mall_shop_favorable_item_n);
			item_rela_line.setBackgroundColor(Color.parseColor("#fff8f8"));
			item_rela_line_iv.setBackgroundResource(R.drawable.mall_favorable_dialog_normal_bg);
			item_tv_money_sign.setTextColor(Color.parseColor(color));
			item_money_tv.setTextColor(Color.parseColor(color));
			item_money_rule_tv.setTextColor(Color.parseColor(color));
			image_line.setBackgroundResource(R.drawable.mall_favorable_dialog_line_normal_bg);
			item_tv_shop_name.setTextColor(Color.parseColor("#333333"));
			item_tv_shop_time.setTextColor(Color.parseColor("#333333"));
			item_show_line_tv.setBackgroundColor(Color.parseColor("#fff8f8"));
		}else{
			item_rela.setBackgroundResource(R.drawable.bg_mall_shop_favorable_item_p);
			item_rela_line.setBackgroundColor(Color.parseColor("#f0f0f0"));
			item_rela_line_iv.setBackgroundResource(R.drawable.mall_favorable_dialog_void_bg);
			item_tv_money_sign.setTextColor(Color.parseColor("#cccccc"));
			item_money_tv.setTextColor(Color.parseColor("#cccccc"));
			item_money_rule_tv.setTextColor(Color.parseColor("#cccccc"));
			image_line.setBackgroundResource(R.drawable.mall_favorable_dialog_line_void_bg);
			item_tv_shop_name.setTextColor(Color.parseColor("#cccccc"));
			item_tv_shop_time.setTextColor(Color.parseColor("#cccccc"));
			item_show_line_tv.setBackgroundColor(Color.parseColor("#f0f0f0"));
		}
	}
	public interface interfaceCallBack{
		public void sucess();
	}
}
