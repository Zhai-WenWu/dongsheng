package third.mall.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import acore.tools.PageStatisticsUtils;
import acore.tools.Tools;
import amodule.main.Main;
import third.mall.adapter.AdapterShopRecommed;
import third.mall.adapter.AdapterShoppingNew;
import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.bean.MerchantBean;
import third.mall.bean.ProductBean;
import third.mall.override.MallBaseActivity;
import third.mall.tool.ToolView;
import third.mall.view.MallShopProductView.InterProudct;
import third.mall.widget.ListViewForScrollView;
import third.mall.widget.MyGridView;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 购物车页面
 * @author yu
 *
 */
public class ShoppingActivity extends MallBaseActivity implements OnClickListener{

	private TextView tv_modify;//编辑和完成
	private ListView shopping_list;
	private AdapterShoppingNew adapter;
	private ArrayList<MerchantBean> list;
	private ArrayList<Map<String,String>> list_none;//无效商品集合
	private int currentPage = 0;
	private TextView end_shopping_tv;
	private ImageView choose_iv_boss_all;
	private TextView money_shop_tv_all;
	private boolean choose_state=true;//false:未选中，true:选中
	private boolean edit_state=false;//false:结算，true:编辑
	private float fl_price=0;//总价
	private Handler handler;
	private MallCommon common;//100002回调
	private View view;
	private ArrayList<Map<String,String>> list_recommend= new ArrayList<Map<String,String>>();
	private ArrayList<Map<String, String>> listMapByJson_none=new ArrayList<Map<String,String>>();
	private View view_recommend;
	private View shopCatNoView;
	private RelativeLayout Layout_no;
	private ArrayList<String> list_statistic= new ArrayList<String>();
	private String url;
	private String mall_stat_statistic;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		initActivity("", 3, 0, 0, R.layout.a_mall_shoppingcat);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			for(int i= 1;i<100;i++){
				if(!TextUtils.isEmpty(bundle.getString("fr"+i))){
					list_statistic.add("fr"+i+"="+bundle.getString("fr"+i));
					if(!TextUtils.isEmpty(bundle.getString("fr"+i+"_msg"))){
						list_statistic.add("fr"+i+"_msg"+"="+bundle.getString("fr"+i+"_msg"));
					}
				}else{
					break;
				}
			}
			if(!TextUtils.isEmpty(bundle.getString("xhcode"))){
				list_statistic.add("xhcode="+bundle.getString("xhcode"));
			}
		}
		common=new MallCommon(this);
		initView();
		initData();
//		initTitle();
		XHClick.track(this,"浏览购物车页");
	}

	private void initView() {
		findViewById(R.id.modify_layout).setVisibility(View.VISIBLE);
		findViewById(R.id.modify_layout).setOnClickListener(this);
		tv_modify=(TextView) findViewById(R.id.tv_modify);
		tv_modify.setText("编辑");
		((TextView)findViewById(R.id.title)).setText("购物车");
		findViewById(R.id.title).setVisibility(View.VISIBLE);
		findViewById(R.id.back).setOnClickListener(this);

		shopping_list = (ListView) findViewById(R.id.shopping_list);
		end_shopping_tv = (TextView) findViewById(R.id.end_shopping_tv);
		end_shopping_tv.setOnClickListener(this);
		choose_iv_boss_all = (ImageView) findViewById(R.id.choose_iv_boss_all);
		findViewById(R.id.choose_iv_boss_all_tv).setOnClickListener(this);
		choose_iv_boss_all.setOnClickListener(this);
		findViewById(R.id.money_shop_rela).setVisibility(View.GONE);
		money_shop_tv_all = (TextView) findViewById(R.id.money_shop_tv_all);

		findViewById(R.id.money_shop_rela).setVisibility(View.VISIBLE);
		findViewById(R.id.shopping_bottom).setVisibility(View.GONE);
		shopping_list.setVisibility(View.GONE);
		findViewById(R.id.modify_layout).setVisibility(View.GONE);
	}

	@SuppressLint("HandlerLeak")
	private void initData() {
		list_none=new ArrayList<Map<String,String>>();
		list=new ArrayList<MerchantBean>();
		adapter= new AdapterShoppingNew(this, list);
		shopping_list.setDivider(null);

		adapter.setInterface(new InterProudct() {

			@Override
			public void setChangeSucess() {
				parseInfoList();
				setAllChangeState();
				setNewAllNumber();
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MallCommon.sucess_data:
					findViewById(R.id.modify_layout).setVisibility(View.VISIBLE);
					findViewById(R.id.shopping_bottom).setVisibility(View.VISIBLE);
					if(Layout_no.getChildCount()>0){
						Layout_no.removeAllViews();
					}
					shopping_list.setVisibility(View.VISIBLE);
					break;
				case MallCommon.sucess_data_no:
					findViewById(R.id.modify_layout).setVisibility(View.GONE);
					findViewById(R.id.shopping_bottom).setVisibility(View.GONE);
					if(Layout_no.getChildCount()<=0){
						setShopCatNoData();
					}
					shopping_list.setVisibility(View.VISIBLE);

					break;
				}
				super.handleMessage(msg);
			}
		};
		loadingListview();
	}

	/**
	 * 显示数量
	 */
	private void setNewAllNumber(){
		int num=0;
		for (int i = 0,size=list.size(); i < size; i++) {
			num+=list.get(i).getNum_product();
		}
		if(!edit_state){//结算
			end_shopping_tv.setText("结算("+num+")");
		}else{//编辑
			end_shopping_tv.setText("删除("+num+")");
		}
	}

	/**
	 * 处理当前选中状态
	 */
	private void setAllChangeState(){
		if(!edit_state){//结算
			for (int i = 0,size=list.size() ; i < size; i++) {
				if(!list.get(i).isChoose_state_shop()){
					choose_state=false;
					break;
				}else choose_state=true;
			}
		}else{//编辑

		}
		if(choose_state){//选中
			choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_choose);
		}else{//未选中
			choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_no_choose);
		}
	}

	/**
	 * 初始化请求网络数据
	 */
	private void loadingListview() {
		loadManager.setLoading(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setRequest(false);
			}
		});
	}
	/**
	 * 请求网络
	 * @param state
	 */
	public void setRequest(final boolean state){
		loadManager.changeMoreBtn(UtilInternet.REQ_OK_STRING, -1, -1, currentPage,list.size()==0);
		url = MallStringManager.mall_getCartInfo_v2;
		for (int i = 0,size= list_statistic.size(); i < size; i++) {
			if(i==0){
				url+="?"+list_statistic.get(i);
			}else url+="&"+list_statistic.get(i);
		}
		MallReqInternet.in().doGet(url, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				int loadCount = 0;
				loadManager.loadOver(flag, 1,true);
				if(flag>=UtilInternet.REQ_OK_STRING){
					if(state){
						list.clear();
					}
					ArrayList<Map<String, String>> listMapByJsonNew_one = UtilString.getListMapByJson(msg);
					if(!listMapByJsonNew_one.get(0).containsKey("cart")){
						handler.sendEmptyMessage(MallCommon.sucess_data_no);
					}
					ArrayList<Map<String, String>> listMapByJsonNew= UtilString.getListMapByJson(listMapByJsonNew_one.get(0).get("cart"));
					if(!state){
						if(listMapByJsonNew_one.get(0).containsKey("recommend_product")){
							ArrayList<Map<String, String>> listMapByJson_recommend= UtilString.getListMapByJson(listMapByJsonNew_one.get(0).get("recommend_product"));
							if(listMapByJson_recommend.size()>0){
								for (int i = 0; i < listMapByJson_recommend.size(); i++) {
									list_recommend.add(listMapByJson_recommend.get(i));
								}
							}
						}
					}
					ArrayList<Map<String, String>> listMapByJson=UtilString.getListMapByJson(listMapByJsonNew.get(0).get("merchantable"));

					if(!state){
						if(listMapByJsonNew.get(0).containsKey("invalid")){
							listMapByJson_none=UtilString.getListMapByJson(listMapByJsonNew.get(0).get("invalid"));
							if(listMapByJson_none.size()>0)
							parseStock(listMapByJson_none);
						}
						setRecommendProduct();
					}

					loadCount=listMapByJson.size();
					for (int i = 0; i < listMapByJson.size(); i++) {
						MerchantBean bean= new MerchantBean();
						bean.setData(listMapByJson.get(i));
						list.add(bean);
					}

					if(list.size()<=0&& listMapByJson_none.size()<=0){
						handler.sendEmptyMessage(MallCommon.sucess_data_no);
					}else{
						handler.sendEmptyMessage(MallCommon.sucess_data);
					}
					parseInfoList();

					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					adapter.setStatistic(url,mall_stat_statistic);
					if(state){
						adapter.notifyDataSetChanged();
					}else{
						shopping_list.setAdapter(adapter);
					}
					setNewAllNumber();
					setAllChangeState();
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map ){
					@SuppressWarnings("unchecked")
					Map<String,String> map= (Map<String, String>) msg;
					//处理code过期问题
					if(MallCommon.code_past.equals(map.get("code"))){
						common.setLoading(new InterfaceMallReqIntert() {

							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									setRequest(false);
								}else if(state==UtilInternet.REQ_CODE_ERROR){
									loadManager.loadOver(state, 1,true);
								}
							}
						});
					}
				}
				loadManager.hideProgressBar();
				shopping_list.setVisibility(View.VISIBLE);
				//处理只显示一个页面
				currentPage = loadManager.changeMoreBtn(flag, loadCount+loadCount, loadCount, currentPage,list.size()==0);
//				shopping_list.onRefreshComplete();

			}
		});
	}
	/**
	 * 解析商品数据
	 * @param listMapByJson_none
	 */
	@SuppressLint("InflateParams")
	private void parseStock(ArrayList<Map<String, String>> listMapByJson_none) {
		view=LayoutInflater.from(this).inflate(R.layout.a_mall_shopping_list_foot, null);
		//清楚数据
		ListViewForScrollView product_list_none=(ListViewForScrollView) view.findViewById(R.id.product_list_none);
		if(listMapByJson_none.size()>0){
			view.setVisibility(View.VISIBLE);
			 view.findViewById(R.id.item_root_rela).setVisibility(View.VISIBLE);
		}else{
			view.findViewById(R.id.item_root_rela).setVisibility(View.GONE);
			view.setVisibility(View.GONE);}
		for (int i = 0; i < listMapByJson_none.size(); i++) {
			ArrayList<Map<String, String>> listMapByJson= UtilString.getListMapByJson(listMapByJson_none.get(i).get("product_list"));
			for (int j = 0; j < listMapByJson.size(); j++) {
				list_none.add(listMapByJson.get(j));
			}
		}
		AdapterSimple simple= new AdapterSimple(product_list_none, list_none, R.layout.a_mall_shopping_list_none_item,
				new String[]{"img","title","error_msg","num"},
				new int[]{R.id.shop_none_iv,R.id.shop_none_text,R.id.shop_none_data,R.id.product_num});
		product_list_none.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setStatisticIndex();
				Intent intent= new Intent(ShoppingActivity.this,CommodDetailActivity.class);
				intent.putExtra("product_code", list_none.get(position).get("code"));
				intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(ShoppingActivity.this));
				intent.putExtra(MallBaseActivity.PAGE_FROM_TWO,"购物车");
				ShoppingActivity.this.startActivity(intent);
			}
		});
		if(list_none.size()>0){
			product_list_none.setAdapter(simple);
		}
		view.findViewById(R.id.product_text_none).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				deleteCartNoneProduct();
				showdelNoDataproduct();
			}
		});
		shopping_list.addFooterView(view);
	}

	private void setRecommendProduct(){
		//推荐位置
		Layout_no = new RelativeLayout(this);
		LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		Layout_no.setLayoutParams(lp);
		shopping_list.addFooterView(Layout_no);

		if(list_recommend.size()>0){
			view_recommend= LayoutInflater.from(this).inflate(R.layout.a_mall_shop_recommend, null);
			MyGridView gridview=(MyGridView) view_recommend.findViewById(R.id.gridview);
			AdapterShopRecommed recommend= new AdapterShopRecommed(this,gridview, list_recommend, R.layout.a_mall_shop_recommend_item_grid, new String[]{}, new int[]{},"a_mail_shopping_cart");
			gridview.setAdapter(recommend);
			shopping_list.addFooterView(view_recommend);
			gridview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					setStatisticIndex();
					XHClick.mapStat(ShoppingActivity.this, "a_mail_shopping_cart","你可能喜欢","点击商品");
					Intent intent = new Intent(ShoppingActivity.this, CommodDetailActivity.class);
					intent.putExtra("product_code", list_recommend.get(position).get("product_code"));
					intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(ShoppingActivity.this));
					intent.putExtra(MallBaseActivity.PAGE_FROM_TWO, "你可能喜欢");
					startActivity(intent);
				}
			});
		}
	}
	private void setShopCatNoData(){
		if(shopCatNoView==null){
			shopCatNoView=LayoutInflater.from(this).inflate(R.layout.a_shopcat_no_cat_view, null);
			shopCatNoView.findViewById(R.id.shoppingcat_go).setOnClickListener(this);
		}
		Layout_no.addView(shopCatNoView);
	}
	/**
	 * 解析获取合计价格
	 */
	private void parseInfoList(){
		fl_price=0;
		for (int i = 0,size=list.size(); i < size; i++) {
				fl_price+=Float.parseFloat(list.get(i).getMerchantPrice());
		}
		fl_price=ToolView.getTwoFloat(fl_price);
		money_shop_tv_all.setText("合计:￥"+ToolView.getNumberPart(fl_price+""));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.modify_layout://编辑
			if(edit_state){//编辑状态--显示为完成
				XHClick.mapStat(this, "a_mail_shopping_cart","完成","");
				tv_modify.setText("编辑");
				edit_state=false;
				fl_price=0;
				choose_state= true;
				choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_choose);
				findViewById(R.id.money_shop_rela).setVisibility(View.VISIBLE);
			}else{//非编辑状态--显示编辑
				XHClick.mapStat(this, "a_mail_shopping_cart","编辑","");
				tv_modify.setText("完成");
				choose_state= false;
				edit_state=true;
				choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_no_choose);
				findViewById(R.id.money_shop_rela).setVisibility(View.GONE);
			}
			setAllChangeData();
			adapter.notifyDataSetChanged();
			break;
		case R.id.end_shopping_tv://结算和删除
			if(edit_state){
				XHClick.mapStat(this, "a_mail_shopping_cart","删除","");
				if(getNumChooseProduct()>0){
					showdelproduct();
				}else{
					Tools.showToast(this, "您还没有选择商品");
				}
			}else {
				XHClick.mapStat(this, "a_mail_shopping_cart","结算","");
				setCreateOrder();
			}

			break;
		case R.id.choose_iv_boss_all_tv:
		case R.id.choose_iv_boss_all://全选
			if(choose_state){
				choose_state= false;
				choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_no_choose);
			}else{
				fl_price=0;
				choose_state= true;
				choose_iv_boss_all.setImageResource(R.drawable.z_mall_shopcat_choose);
			}
			setAllChangeData();
			adapter.notifyDataSetChanged();
			break;
		case R.id.shoppingcat_go:
			Main.colse_level=3;
			this.finish();
			break;
		}
	}

	/**
	 * 设置当前数据
	 */
	private void setAllChangeData(){
		for (int i = 0,size= list.size(); i < size; i++) {
			list.get(i).setAllChangeState(choose_state,edit_state);
		}
		parseInfoList();
		setNewAllNumber();

	}
	/**
	 * 判断商家的商品是否都是无货，false:都是无货，true:表示有货
	 * @param list_product
	 */
	private boolean setMainProduct(ArrayList<ProductBean> list_product){
		boolean state= true;
		for (int i = 0; i < list_product.size(); i++) {
			int saleable_num= Integer.parseInt(list_product.get(i).getSaleable_num());
			if("1".equals(list_product.get(i).getEdit_product())){
				state=true;
				break;
			}else{
				if(saleable_num<=0){
					state=false;
				}else{
					state=true;
					break;
				}
			}
		}
		return state;
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
	}
	/**
	 * 删除商品
	 */
	private void deleteCartProudct(){
		if(getNumChooseProduct()<=0){
			Tools.showToast(this, "您还没有选择商品");
			return;
		}
		setStatisticIndex();
		JSONArray json_code= new JSONArray();
		for (int i = 0,size= list.size(); i < size; i++) {
			if(list.get(i).isEdit_shop())
				for (int j = 0,length=list.get(i).list_product.size(); j < length; j++) {
					if(list.get(i).list_product.get(j).getChoose_state())
						json_code.put(list.get(i).list_product.get(j).getCode());
				}
		}
		String param= "product_code="+json_code.toString();
		MallReqInternet.in().doPost(MallStringManager.mall_delCartProudct, param, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					deleteShopData();
				}

			}
		});
	}

	/**
	 * 删除选中数据
	 */
	private void deleteShopData() {
		for (int i = 0; i < list.size();) {
			if(list.get(i).isChoose_state_shop()){
				list.remove(i);
			}else{
				list.get(i).delChooseProduct();
				i++;
			}
		}
		if(list.size()<=0){
			if(listMapByJson_none.size()<=0)
				handler.sendEmptyMessage(MallCommon.sucess_data_no);
		}
		setNewAllNumber();
		adapter.notifyDataSetChanged();
	}

	/**
	 * 清除无效商品
	 */
	private void deleteCartNoneProduct(){
		setStatisticIndex();
		JSONArray json_code= new JSONArray();
		for (int i = 0; i < list_none.size(); i++) {
			json_code.put(list_none.get(i).get("code"));
		}
		String param= "product_code="+json_code.toString();
		MallReqInternet.in().doPost(MallStringManager.mall_delCartProudct, param, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					handler.post(new Runnable() {

						@Override
						public void run() {
							list_none.clear();
							view.setVisibility(View.GONE);
							shopping_list.removeFooterView(view);
							adapter.notifyDataSetChanged();
							if(list.size()<=0){
								handler.sendEmptyMessage(MallCommon.sucess_data_no);
							}
						}
					});
				}

			}
		});
	}
	/**
	 * 创建订单
	 */
	private void setCreateOrder(){
		if(getNumChooseProduct()<=0){
			Tools.showToast(this, "您还没有选择商品");
			return;
		}
		setStatisticIndex();
		end_shopping_tv.setEnabled(false);
		String param="order_info="+setListUseToJson().toString();
		MallReqInternet.in().doPost(MallStringManager.mall_checkoutOrder_v2, param, new MallInternetCallback(this) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=UtilInternet.REQ_OK_STRING){
					ArrayList<Map<String,String>> list_data=UtilString.getListMapByJson(UtilString.getListMapByJson(msg).get(0).get("sub_order"));
					if(list_data.size()>0){
						Intent intent = new Intent(ShoppingActivity.this,ShoppingOrderActivity.class);
						intent.putExtra("msg_order", msg.toString());
						intent.putExtra("order_info", setListUseToJson().toString());
						intent.putExtra("url", MallStringManager.mall_checkoutOrder_v2);
						intent.putExtra(MallBaseActivity.PAGE_FROM, PageStatisticsUtils.getPageName(ShoppingActivity.this));
						if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
							intent.putExtra("stat", (String) stat[0]);
						}
						ShoppingActivity.this.startActivityForResult(intent, 10000);
					}else{
						showNOProduct();
					}
				}else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
					@SuppressWarnings("unchecked")
					Map<String,String> map= (Map<String, String>) msg;
					if("6000008".equals(map.get("code"))){
						showNOProduct();
					}else{
						Tools.showToast(ShoppingActivity.this, map.get("msg")+"");
					}
				}
				end_shopping_tv.setEnabled(true);

			}
		});
	}
	/**
	 * 删除商品dialog
	 */
	private void showNOProduct(){
		final Dialog dialog= new Dialog(this,R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window=dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message= (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText("您选购的部分商品库存不足，是否继续购买");
		TextView dialog_cancel= (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure= (TextView) window.findViewById(R.id.dialog_sure);
		dialog_cancel.setText("取消");
		dialog_sure.setText("确定");
		dialog_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
		}
		});
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				//清楚数据
				handler.post(new Runnable() {

					@Override
					public void run() {
						if(view!=null){
							view.setVisibility(View.GONE);
							shopping_list.removeFooterView(view);
							view=null;
						}
						if(view_recommend!=null){
							view_recommend.setVisibility(View.GONE);
							shopping_list.removeFooterView(view_recommend);
							view_recommend=null;
						}
						list.clear();
						list_none.clear();
						list_recommend.clear();
						loadingListview();
					}
				});
			}
		});
		dialog.show();
	}
	private void initTitle() {
		if(Tools.isShowTitle()) {
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}
	/**
	 * 删除商品dialog
	 */
	private void showdelproduct(){
		final Dialog dialog= new Dialog(this,R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window=dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message= (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText("确认要删除这"+getNumChooseProduct()+"种商品吗？");
		TextView dialog_cancel= (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure= (TextView) window.findViewById(R.id.dialog_sure);
		dialog_cancel.setText("取消");
		dialog_sure.setText("确定");
		dialog_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteCartProudct();
				dialog.cancel();
			}
		});
		dialog.show();
	}
	/**
	 * 删除无效商品dialog
	 */
	private void showdelNoDataproduct(){
		final Dialog dialog= new Dialog(this,R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window=dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message= (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText("确认删除全部无效商品吗？");
		TextView dialog_cancel= (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure= (TextView) window.findViewById(R.id.dialog_sure);
		dialog_cancel.setText("取消");
		dialog_sure.setText("确定");
		dialog_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog_sure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteCartNoneProduct();
				dialog.cancel();
			}
		});
		dialog.show();
	}
	/**
	 * list_use转json
	 * @return
	 */
	private JSONArray setListUseToJson(){
		JSONArray jsonArray= new JSONArray();
		try{
			for (int i = 0,size= list.size(); i < size; i++) {
				for (int j = 0,length=list.get(i).list_product.size(); j < length; j++) {
					if(list.get(i).list_product.get(j).getChoose_state()){
						JSONObject object = new JSONObject();
						object.put("product_code", list.get(i).list_product.get(j).getCode());
						object.put("product_num", list.get(i).list_product.get(j).getNum());
						jsonArray.put(object);
					}
				}
			}
		return jsonArray;
		}catch(Exception e){
			e.printStackTrace();
		}
		return jsonArray;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==Activity.RESULT_OK){
			if(view!=null){
				view.setVisibility(View.GONE);
				shopping_list.removeFooterView(view);
				view=null;
			}
			if(view_recommend!=null){
				view_recommend.setVisibility(View.GONE);
				shopping_list.removeFooterView(view_recommend);
				view_recommend=null;
			}
			list.clear();
			list_none.clear();
			list_recommend.clear();
			money_shop_tv_all.setText("");
			loadingListview();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 获取当前选中的商品数量
	 * @return
	 */
	private int getNumChooseProduct(){
		int num=0;
		for (int i = 0,size= list.size(); i < size; i++) {
			num+=list.get(i).getNumChooseProduct();
		}
		return num;
	}
	/**
	 * 对电商按钮进行统计
	 */
	private void setStatisticIndex(){
		MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, this);
	}
}
