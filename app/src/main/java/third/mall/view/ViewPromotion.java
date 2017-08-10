package third.mall.view;

import acore.tools.Tools;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * 优惠view
 * 
 * @author Administrator
 *
 */
public class ViewPromotion extends RelativeLayout {
	private Context context;
	private LinearLayout ll_pingkage;
	private TextView tv_pingkage,tv_pingkage_style;
	private LinearLayout ll_reduce;
	private TextView tv_reduce,tv_reduce_style;
	public final static int style_all = 1;// 背景为全色
	public final static int style_null = 2;// 背景为半色
	private RelativeLayout root_layout;

	public ViewPromotion(Context context) {
		super(context);
		this.context = context;
		initView();
	}

	public ViewPromotion(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initView();
	}

	/**
	 * 初始化view
	 * 
	 * @param context2
	 */
	private void initView() {
		LayoutInflater.from(context).inflate(R.layout.a_mall_view_reduce, this);
		root_layout=(RelativeLayout) findViewById(R.id.root_layout);
		ll_pingkage = (LinearLayout) findViewById(R.id.ll_pingkage);
		tv_pingkage = (TextView) findViewById(R.id.tv_pingkage);
		tv_pingkage_style = (TextView) findViewById(R.id.tv_pingkage_style);
		ll_reduce = (LinearLayout) findViewById(R.id.ll_reduce);
		tv_reduce = (TextView) findViewById(R.id.tv_reduce);
		tv_reduce_style = (TextView) findViewById(R.id.tv_reduce_style);
	}

	/**
	 * 给当前view初始化数据
	 * 
	 * @param map
	 */
	public void setData(String postage,String promotion ) {
		if(!TextUtils.isEmpty(postage)){
			ll_pingkage.setVisibility(View.VISIBLE);
			tv_pingkage.setText(postage);
		}else ll_pingkage.setVisibility(View.GONE);
		
		if(!TextUtils.isEmpty(promotion)){
			ll_reduce.setVisibility(View.VISIBLE);
			tv_reduce.setText(promotion);
		}else ll_reduce.setVisibility(View.GONE);
	}
	/**
	 * 改变样式
	 */
	public void setChangeStyle(){
		tv_pingkage.setTextSize(Tools.getDimenSp(context, R.dimen.sp_13));
		tv_reduce.setTextSize(Tools.getDimenSp(context, R.dimen.sp_13));
		tv_reduce.setTextColor(Color.parseColor("#999999"));
		tv_pingkage.setTextColor(Color.parseColor("#999999"));
		int dp_30 = (int) context.getResources().getDimension(R.dimen.dp_15);
		int dp_5 = (int) context.getResources().getDimension(R.dimen.dp_5);
		int dp_2 = (int) context.getResources().getDimension(R.dimen.dp_2);
		root_layout.setPadding(0, dp_30, 0, dp_2);
		ll_reduce.setPadding(0, dp_5, 0, 0);
	}

	/**
	 * 设置样式
	 * 
	 * @param style
	 */
	public void setStyle(int style) {
		tv_pingkage_style.setText("包邮");
		tv_reduce_style.setText("满减");
		switch (style) {
		case style_all:
			tv_pingkage_style.setTextColor(Color.parseColor("#ffffff"));
			tv_reduce_style.setTextColor(Color.parseColor("#ffffff"));
			tv_pingkage_style.setBackgroundResource(R.drawable.bg_mall_view_fav_round_all_blue);
			tv_reduce_style.setBackgroundResource(R.drawable.bg_mall_view_fav_round_all_red);
			break;
		case style_null:
			tv_pingkage_style.setTextColor(Color.parseColor("#00cc33"));
			String color = Tools.getColorStr(context,R.color.comment_color);
			tv_reduce_style.setTextColor(Color.parseColor(color));
			tv_pingkage_style.setBackgroundResource(R.drawable.bg_mall_view_reduce_blue);
			tv_reduce_style.setBackgroundResource(R.drawable.bg_mall_view_reduce_red);
			break;

		}
	}

}
