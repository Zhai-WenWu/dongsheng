package third.mall.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * 使用优惠券dialog
 * @author yujian
 *
 */
public class UseFavorableDialog extends SimpleDialog {

	private AdapterUseFavorable adapter;
	private Activity activity;
	private ListView listview_dialog;
	private ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
	private String product_amt;
	private String itme_code;
	private static final String FAVORABLE_STATE="favorable_state";//当前状态 1---可以使用，0---不可使用
	private changeCallBack callback;
	public UseFavorableDialog(Activity activity,ArrayList<Map<String, String>> list,String product_amt) {
		super(activity, R.layout.dialog_mall_favorable_use);
		this.activity=activity;
		this.list=list;
		this.product_amt= product_amt;
		setLatyoutHeight();
	}
	/**
	 * 设置优惠券code
	 * @param code
	 */
	public void setdata(String code,changeCallBack callback){
		this.itme_code=code;
		this.callback= callback;
		init();
	}
	private void init() {
		if(list.size()>0){
			for (int i = 0,size=list.size(); i < size; i++) {
				if(Float.parseFloat(list.get(i).get("order_amt_reach"))<=Float.parseFloat(product_amt)){
					list.get(i).put(FAVORABLE_STATE, "1");
				}else list.get(i).put(FAVORABLE_STATE, "0");
				
			}
			listview_dialog = (ListView) view.findViewById(R.id.listview_dialog);
			adapter=new AdapterUseFavorable(activity,listview_dialog, list, R.layout.item_mall_dialog_favorable_use, null, null);
			listview_dialog.setAdapter(adapter);
			this.show();
		}
		view.findViewById(R.id.save_ll).setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callback.setChangeData(adapter.code, adapter.amt);
				UseFavorableDialog.this.dismiss();
			}
		});
	}

	
	private class AdapterUseFavorable extends AdapterSimple{

		private Context  context;
		private List<? extends Map<String, ?>> data;
		public String code="";
		public String amt="";
		public AdapterUseFavorable(Context context,View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
			super(parent, data, resource, from, to);
			this.context= context;
			this.data= data;
			this.code= itme_code;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Map<String,String> map= (Map<String, String>) data.get(position);
			ViewHolder viewHolder;
			if(null==convertView){
				viewHolder=new ViewHolder();
				convertView= LayoutInflater.from(context).inflate(R.layout.item_mall_dialog_favorable_use, null);
				viewHolder.setView(convertView);
				convertView.setTag(viewHolder);
			}else viewHolder=(ViewHolder) convertView.getTag();
			viewHolder.setValues(map);
			return convertView;
		}
		class ViewHolder{
			private ImageView item_choose;
			private RelativeLayout item_rela;
			private TextView item_des,item_time,item_price,item_price_sign;
			public void setView(View view){
				item_rela=(RelativeLayout) view.findViewById(R.id.item_rela);
				item_choose=(ImageView) view.findViewById(R.id.item_choose);
				item_des=(TextView) view.findViewById(R.id.item_des);
				item_time=(TextView) view.findViewById(R.id.item_time);
				item_price=(TextView) view.findViewById(R.id.item_price);
				item_price_sign=(TextView) view.findViewById(R.id.item_price_sign);
			}
			public void setValues(final Map<String,String> map){
				item_price.setText(map.get("coupon_amt"));
				item_des.setText(map.get("desc"));
				item_time.setText("有效期："+map.get("start_time")+"-"+map.get("end_time"));
				if(code.equals(map.get("shop_coupon_code"))){
					item_choose.setImageResource(R.drawable.z_mall_shopcat_choose_green);
				}else {
					item_choose.setImageResource(R.drawable.z_mall_shopcat_no_choose);
				}
				setItemStyle(map.get(FAVORABLE_STATE));
				item_rela.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(map.containsKey(FAVORABLE_STATE)&&"1".equals(map.get(FAVORABLE_STATE))){
							if(code.equals(map.get("shop_coupon_code"))){
								code="";
								amt="";
							}else{
								code= map.get("shop_coupon_code");
								amt= map.get("coupon_amt");
							}
							notifyDataSetChanged();
						}else Tools.showToast(context, "不满足使用条件");
					}
				});
			}
			/**
			 * 设置item样式
			 * @param style
			 */
			private void setItemStyle(String style){
				if("1".equals(style)){
					String color = Tools.getColorStr(context,R.color.comment_color);
					item_price.setTextColor(Color.parseColor(color));
					item_price_sign.setTextColor(Color.parseColor(color));
					item_des.setTextColor(Color.parseColor("#333333"));
				}else if("0".equals(style)){
					item_price.setTextColor(Color.parseColor("#999999"));
					item_price_sign.setTextColor(Color.parseColor("#999999"));
					item_des.setTextColor(Color.parseColor("#999999"));
				}
			}
		}
	}
	
	public interface changeCallBack{
		/*** 回调数据*/
		public void setChangeData(String code,String amt);
	}
}
