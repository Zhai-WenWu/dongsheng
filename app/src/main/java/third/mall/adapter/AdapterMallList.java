package third.mall.adapter;

import java.util.List;
import java.util.Map;

import third.mall.activity.CommodDetailActivity;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;
/**
 * 商城主adapter
 * @author yu
 *
 */
public class AdapterMallList extends MallAdapterSimple{
	private List<? extends Map<String, ?>> data;
	private Context mall_act;
	private int waith;
	private int distance;
	private int tv_distance;
	private int num;
	public AdapterMallList(Context mall,View parent, List<? extends Map<String, ?>> data,int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.data= data;
		this.mall_act=mall;
		WindowManager wm= (WindowManager) mall.getSystemService(Context.WINDOW_SERVICE);
		waith=wm.getDefaultDisplay().getWidth();
		distance= (int) mall_act.getResources().getDimension(R.dimen.dp_15);
		tv_distance= (int) mall_act.getResources().getDimension(R.dimen.sp_15);
		num=setTextViewNum();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view=super.getView(position, convertView, parent);
		final Map<String,String> map = (Map<String, String>) data.get(position);
		String discount_price = map.get("discount_price");
		String price= map.get("price");
		String imageurl= map.get("img");
		String title= map.get("title");
		String number=UtilString.getListMapByJson(map.get("stock")).get(0).get("outstock_num");
		String saleable_number=UtilString.getListMapByJson(map.get("stock")).get(0).get("saleable_num");//可销售数量
		
		TextView tv_now_price=(TextView) view.findViewById(R.id.mall_item_tv_now_price);
		tv_now_price.setText("¥"+discount_price);
		
		TextView tv_before_price=(TextView) view.findViewById(R.id.mall_item_tv_before_price);
		tv_before_price.setText("¥"+price);
		if(discount_price.equals(price)){
			tv_before_price.setVisibility(View.GONE);
		}else{
			tv_before_price.setVisibility(View.VISIBLE);
		}
		tv_before_price.getPaint().setAntiAlias(true);
		tv_before_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
		TextView tv_number=(TextView) view.findViewById(R.id.mall_item_tv_number);
		tv_number.setText(number+"件已卖");
		ImageView item_iv=(ImageView) view.findViewById(R.id.mall_item_iv);
		setViewImage(item_iv, imageurl);
		TextView tv_content=(TextView) view.findViewById(R.id.mall_item_tv_content);
		//设置1.6行显示
		int now_num=(int) (num*1.6);
		if(title.length()>now_num){
			title= title.substring(0, now_num);
			title+="...";
		}
		tv_content.setText(title);
		//剩余数据处理
		TextView mall_item_state_rela_tv=(TextView) view.findViewById(R.id.mall_item_state_rela_tv);
		RelativeLayout mall_item_state_rela=(RelativeLayout) view.findViewById(R.id.mall_item_state_rela);
		View view_backgroup_item=view.findViewById(R.id.view_backgroup_item);//没有数据显示背景
		if(!TextUtils.isEmpty(saleable_number)){
		int saleable=Integer.parseInt(saleable_number);
//			if(saleable<=50&&saleable>0){//剩余
//				mall_item_state_rela.setVisibility(View.VISIBLE);
//				mall_item_state_rela.setBackground(mall_act.getResources().getDrawable(R.drawable.mall_home_item_state));
//				view.findViewById(R.id.mall_item_state_rela_item).setVisibility(View.VISIBLE);
//				mall_item_state_rela_tv.setText("剩"+saleable_number+"件");
//				setTextcolorSpan(tv_number, number.length());
//				setSaleableNum(false, tv_content, view_backgroup_item);
//			}else 
			if(saleable==0){//抢光
				mall_item_state_rela.setVisibility(View.VISIBLE);
				view.findViewById(R.id.mall_item_state_rela_item).setVisibility(View.GONE);
				mall_item_state_rela.setBackgroundResource(R.drawable.mall_item_state_rela_iv);
				setSaleableNum(true, tv_content, view_backgroup_item);
			}else{
				mall_item_state_rela.setVisibility(View.GONE);
				setTextcolorSpan(tv_number, number.length());
				setSaleableNum(false, tv_content, view_backgroup_item);
				
			}
		}else{
			mall_item_state_rela.setVisibility(View.GONE);
			setTextcolorSpan(tv_number, number.length());
			setSaleableNum(false, tv_content, view_backgroup_item);
		}
		
		
		view.findViewById(R.id.mall_item_rela).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mall_act,CommodDetailActivity.class);
				intent.putExtra("product_code",map.get("product_code"));
				mall_act.startActivity(intent);
			}
		});
		return view;
	}
	
	/**
	 * 设置部分字变色
	 * 
	 * @param text
	 */
	private void setTextcolorSpan(TextView text,int size) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text.getText().toString());
		ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor("#f86e6e"));
		builder.setSpan(redSpan, 0, size, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		text.setText(builder);
	}
	/**
	 * 设置数据
	 * @param state
	 * @param tv
	 * @param view
	 */
	private void setSaleableNum(boolean state,TextView tv,View view){
		if(state){//显示抢光了
			view.setVisibility(View.VISIBLE);
			tv.setTextColor(Color.parseColor("#919191"));
		}else{
			view.setVisibility(View.GONE);
			tv.setTextColor(Color.parseColor("#333333"));
		}
	}
	private int setTextViewNum(){
		int tv_waith= waith-distance*2;
		int tv_pad=ToolView.dip2px(mall_act, 1.0f);
		int num= (tv_waith+tv_pad)/(tv_distance+tv_pad);
		return num;
	}
}
