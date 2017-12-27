package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.ReqInternet;
import third.mall.MainMall;
import third.mall.adapter.AdapterShopRecommed;
import third.mall.alipay.MallPayActivity;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;
import third.mall.widget.MyGridView;
import third.mall.wx.WxPay;
import xh.basic.tool.UtilString;

/**
 * 支付成功
 * @author yu
 *
 */
public class PaySuccedActvity extends MallBaseActivity implements OnClickListener{

	private String amt;
	private ArrayList<Map<String,String>> list_recommend;
	private View view_recommend;
	private RelativeLayout recommend_rela;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Bundle bundle= getIntent().getExtras();
		if(bundle!=null){
			amt= bundle.getString("amt");
		}
		initActivity("", 3, 0, 0, R.layout.a_mall_pay_succee);
		initView();
//		initTitle();
		XHClick.track(this,"支付成功页");
	}
	private void initTitle() {
		if(Tools.isShowTitle()) {
			int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
			int height = topbarHeight + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}

	private void initView() {
		findViewById(R.id.pay_order).setOnClickListener(this);
		findViewById(R.id.pay_mall).setOnClickListener(this);
		findViewById(R.id.back_tv).setVisibility(View.GONE);
		TextView pay_price=(TextView) findViewById(R.id.pay_price);
		recommend_rela=(RelativeLayout) findViewById(R.id.recommend_rela);
		if(TextUtils.isEmpty(amt)){
			pay_price.setText("¥"+WxPay.amt);
		}else{
			pay_price.setText("¥"+amt);
		}
		setRequest();
	}

	private void setRequest() {
		String url=MallStringManager.mall_getHotRecommend;
		MallReqInternet.in().doGet(url, new MallInternetCallback() {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					list_recommend=UtilString.getListMapByJson(msg);
					setRecommendProduct();
				}else{
					
				}
			
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pay_order:
			MallPayActivity.pay_state=true;
			Main.colse_level=2;
			PaySuccedActvity.this.finish();
			break;
		case R.id.pay_mall:
			MallPayActivity.mall_state=true;
			Main.colse_level=3;
			PaySuccedActvity.this.finish();
			//切换到商城首页
			Main.allMain.setCurrentTabByClass(MainMall.class);
			break;
		case R.id.back_tv:
			Main.colse_level=3;
			PaySuccedActvity.this.finish();
			break;

		}
	}
	@Override
	public void onBackPressed() {
		MallPayActivity.pay_state=true;
		Main.colse_level=3;
		PaySuccedActvity.this.finish();
	}
	private void setRecommendProduct(){
		//推荐位置
		if(list_recommend!=null&&list_recommend.size()>0){
			view_recommend= LayoutInflater.from(this).inflate(R.layout.a_mall_shop_recommend, null);
			MyGridView gridview=(MyGridView) view_recommend.findViewById(R.id.gridview);
			AdapterShopRecommed recommend= new AdapterShopRecommed(this,gridview, list_recommend, R.layout.a_mall_shop_recommend_item_grid, new String[]{}, new int[]{},"");
			gridview.setAdapter(recommend);
			recommend_rela.addView(view_recommend);
			gridview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(PaySuccedActvity.this, CommodDetailActivity.class);
					intent.putExtra("product_code", list_recommend.get(position).get("product_code"));
					intent.putExtra(MallBaseActivity.PAGE_FROM,"猜你喜欢");
					startActivity(intent);
				}
			});
		}
	}

}
