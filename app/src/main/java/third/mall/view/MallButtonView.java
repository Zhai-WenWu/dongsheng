package third.mall.view;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import third.mall.aplug.MallClickContorl;
import third.mall.aplug.MallCommon;
import third.mall.aplug.MallCommon.InterfaceMallReqIntert;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import acore.tools.Tools;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Button的View 集合
 * 
 * @author yujian
 *
 */
public class MallButtonView {

	private Context context;
	private MallCommon common;
	/*** 订单列表页——未拆单数据结构*/
	public static String list_state_payment="list_payment_order";
	/*** 订单列表页——拆单数据结构*/
	public static String list_state_order="list_order";
	/*** 订单详情页--未拆单数据结构*/
	public static String detail_state_payment="detail_payment_order";
	/*** 订单详情页----拆单数据*/
	public static String detail_state_order="detail_order";
	

	public MallButtonView(Context context) {
		this.context = context;
		common = new MallCommon(context);
	}

	/****************************************** 创建view**************************************************8 */
	/**
	 * 取消订单-----只有未拆单一种情况
	 * 
	 * @return
	 */
	public View createViewCancelOrder(final InterfaceViewCallback callback, final Map<String, String> map,final String url,final String mall_stat_statistic) {
		final String actionUrl = MallStringManager.mall_cancelOrder;
		final String param = "payment_order_id=" + map.get("payment_order_id");
		final View view = createViewStyle_2("取消订单");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDilalog("确定取消该订单吗？", actionUrl, param,callback,url,mall_stat_statistic);
			}
		});
		return view;
	}

	/**
	 * 去支付----只有未拆单一种情况
	 * 
	 * @return
	 */
	public View createViewToPay(final InterfaceViewCallback callback) {
		View view = createViewStyle_1("去支付");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.sucessCallBack();
			}
		});
		return view;
	}

	/**
	 * 查看物流
	 * 
	 * @return
	 */
	public View createViewGoLog(final InterfaceViewCallback callback) {
		View view = createViewStyle_2("查看物流");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.sucessCallBack();
			}
		});
		return view;
	}

	/**
	 * 评价
	 * @param callback
	 * @return
	 */
	public View createViewComment(final InterfaceViewCallback callback){
		View view = createViewStyle_1("评价");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.sucessCallBack();
			}
		});
		return view;
	}

	/**
	 * 已评价
	 * @return
	 */
	public View createViewCommented(){
		return createViewStyle_2("已评价");
	}

	/**
	 * 确认收货----只有拆单一种情况
	 * 
	 * @return
	 */
	public View createViewReceipt(final InterfaceViewCallback callback, final Map<String, String> map,final String url,final String mall_stat_statistic) {
		final String actionUrl = MallStringManager.mall_api_orderComplete;
		final String param = "order_id=" + map.get("order_id");
		final View view = createViewStyle_1("确认收货");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDilalog( "是否确认收货？", actionUrl, param,callback,url,mall_stat_statistic);
			}
		});
		return view;
	}

	/**
	 * 删除订单----只有未拆单一种情况
	 * 
	 * @return
	 */
	public View createViewDelOrder(final InterfaceViewCallback callback, final Map<String, String> map,final String state,final String url,final String mall_stat_statistic) {
		//---7
		final View view = createViewStyle_2("删除订单");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String param = null;
				if(!TextUtils.isEmpty(state)){
					param = "id=" + map.get("payment_order_id") + "&type=payment_order";
				}
				String actionUrl = MallStringManager.mall_api_delOrder;
				showDilalog( "确认删除订单吗？", actionUrl,param,callback,url,mall_stat_statistic);
			}
		});
		return view;
	}

	/**
	 * 再次购买
	 * 再次购买 两种状态1---payment_order订单    2--- order_id
	 * @return
	 */
	public View createViewRepeatOrder(final InterfaceViewCallback callback, final Map<String, String> map,final String state,final String url,final String mall_stat_statistic) {
		View view = createViewStyle_2("再次购买");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String actionUrl = MallStringManager.mall_addCartProductList;
				String params = "";
				try {
					if(!TextUtils.isEmpty(state)){
						if(state.equals(list_state_payment))
							params = "product_list=" +getJsonArrayListPayment(map).toString();
						else if(state.equals(list_state_order))
							params = "product_list=" +getJsonArrayListOrder(map).toString();
						else if(state.equals(detail_state_payment))
							params = "product_list="+getJsonArrayDetailsPayment(map).toString();
						else if(state.equals(detail_state_order))
							params = "product_list="+getJsonArrayDetailsOrder(map).toString() ;
					}
					MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
					postRequest(actionUrl,params,callback);
				} catch (Exception e) {
				}
			}
		});
		return view;
	}

	/**
	 * 支付
	 * 
	 * @return
	 */
	public View createViewPay(final InterfaceViewCallback callback, Map<String, String> map) {
		View view = createViewStyle_1("支付");
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.sucessCallBack();
			}
		});
		return view;
	}

	/****************************************** 业务**************************************************8 */
	/**
	 * 弹出dialog
	 */
	private void showDilalog( String des, final String actionUrl,final String param,final InterfaceViewCallback callback,final String url,final String mall_stat_statistic) {
		final Dialog dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.a_mall_alipa_dialog);
		Window window = dialog.getWindow();
		window.findViewById(R.id.dialog_title).setVisibility(View.GONE);
		TextView dialog_message = (TextView) window.findViewById(R.id.dialog_message);
		dialog_message.setText(des);
		TextView dialog_cancel = (TextView) window.findViewById(R.id.dialog_cancel);
		TextView dialog_sure = (TextView) window.findViewById(R.id.dialog_sure);
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
				MallClickContorl.getInstance().setStatisticUrl(url, null,mall_stat_statistic, context);
				postRequest(actionUrl, param,callback);
				dialog.cancel();
			}
		});
		dialog.show();
	}

	/**
	 * post请求----
	 * @param actionUrl
	 * @param param
	 * @param callback
	 */
	private void postRequest(final String actionUrl,final String param,final InterfaceViewCallback callback) {
		
		MallReqInternet.in().doPost(actionUrl, param, new MallInternetCallback(context) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//统计
					String mall_stat_statistic = null;
					if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
						mall_stat_statistic=(String) stat[0];
					}
					MallClickContorl.getInstance().setStatisticUrl(actionUrl, param,mall_stat_statistic, context);
					callback.sucessCallBack();
				} else if (flag == UtilInternet.REQ_CODE_ERROR&& msg instanceof Map) {
					Map<String, String> map_code = (Map<String, String>) msg;
					if (MallCommon.code_past.equals(map_code.get("code"))) {
						common.setLoading(new InterfaceMallReqIntert() {
							@Override
							public void setState(int state) {
								if (state >= UtilInternet.REQ_OK_STRING) {
									postRequest(actionUrl, param,callback);
								} else if (state == UtilInternet.REQ_CODE_ERROR) {
								}
							}
						});
					}else Tools.showToast(context, map_code.get("msg"));
				}
			
			}
		});
	}
	/**
	 * 未拆单数据---再次购买-列表
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getJsonArrayListPayment(Map<String, String> map) throws JSONException{
		JSONArray jsonArray= new JSONArray();
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
		for (int i = 0,size=listMapByJson.size(); i < size; i++) {
			ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(listMapByJson.get(i).get("order_product"));
			for (int j = 0,product_size=listMapByJson_product.size(); j < product_size; j++) {
				JSONObject jsonObject= new JSONObject();
				Log.i("product_code", listMapByJson_product.get(j).get("proudct_code"));
				Log.i("product_num", listMapByJson_product.get(j).get("num"));
				jsonObject.put("product_code", listMapByJson_product.get(j).get("proudct_code"));
				jsonObject.put("product_num", listMapByJson_product.get(j).get("num"));
				jsonArray.put(jsonObject);
			}
		}
		return jsonArray;
	}

	/**
	 * 未拆单数据---再次购买--详情
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getJsonArrayDetailsPayment(Map<String, String> map) throws JSONException{
		JSONArray jsonArray= new JSONArray();
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(map.get("order_list"));
		for (int i = 0,size=listMapByJson.size(); i < size; i++) {
			ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(listMapByJson.get(i).get("order_product"));
			for (int j = 0,product_size=listMapByJson_product.size(); j < product_size; j++) {
				JSONObject jsonObject= new JSONObject();
				Log.i("product_code", UtilString.getListMapByJson(listMapByJson_product.get(j).get("info")).get(0).get("product_code"));
				Log.i("product_num", listMapByJson_product.get(j).get("num"));
				jsonObject.put("product_code", UtilString.getListMapByJson(listMapByJson_product.get(j).get("info")).get(0).get("product_code"));
				jsonObject.put("product_num", listMapByJson_product.get(j).get("num"));
				jsonArray.put(jsonObject);
			}
		}
		return jsonArray;
	}
	/**
	 * 拆单数据---再次购买--列表
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getJsonArrayListOrder(Map<String, String> map) throws JSONException{
		JSONArray jsonArray= new JSONArray();
		ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(map.get("order_product"));
		for (int j = 0,product_size=listMapByJson_product.size(); j < product_size; j++) {
			JSONObject jsonObject= new JSONObject();
			jsonObject.put("product_code", listMapByJson_product.get(j).get("proudct_code"));
			jsonObject.put("product_num", listMapByJson_product.get(j).get("num"));
			jsonArray.put(jsonObject);
		}
		return jsonArray;
	}
	/**
	 * 拆单数据---再次购买--详情
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	private JSONArray getJsonArrayDetailsOrder(Map<String, String> map) throws JSONException{
		JSONArray jsonArray= new JSONArray();
		ArrayList<Map<String, String>> listMapByJson_product = UtilString.getListMapByJson(map.get("order_product"));
		for (int j = 0,product_size=listMapByJson_product.size(); j < product_size; j++) {
			JSONObject jsonObject= new JSONObject();
			Log.i("product_code", UtilString.getListMapByJson(listMapByJson_product.get(j).get("info")).get(0).get("product_code"));
			Log.i("product_num", listMapByJson_product.get(j).get("num"));
			jsonObject.put("product_code", UtilString.getListMapByJson(listMapByJson_product.get(j).get("info")).get(0).get("product_code"));
			jsonObject.put("product_num", listMapByJson_product.get(j).get("num"));
			jsonArray.put(jsonObject);
		}
		return jsonArray;
	}
	/****************************************** View**************************************************8 */
	/**
	 * 创建view
	 * 
	 * @return
	 */
	public View createBaseView() {
		View view = LayoutInflater.from(context).inflate(R.layout.view_order_button, null);
		return view;
	}

	/**
	 * 创建view的样式 文字为红色，背景为红色线框
	 * 
	 * @param des
	 */
	private View createViewStyle_1(String des) {
		View view = createBaseView();
		TextView tv = (TextView) view.findViewById(R.id.textview);
		tv.setText(des);
		tv.setTextColor(Color.parseColor("#ffffff"));
		tv.setBackgroundResource(R.drawable.mall_buycommod_buy);
		return view;
	}

	/**
	 * 创建view的样式 文字为灰色色，背景为灰色线框
	 * 
	 * @param des
	 */
	private View createViewStyle_2(String des) {
		View view = createBaseView();
		TextView tv = (TextView) view.findViewById(R.id.textview);
		tv.setText(des);
		tv.setTextColor(Color.parseColor("#666666"));
		tv.setBackgroundResource(R.drawable.mall_order_item_button_style_1);
		return view;
	}

	/**
	 * view的点击必要回调---view的业务逻辑已经处理完成
	 * 
	 * @author Administrator
	 *
	 */
	public interface InterfaceViewCallback {
		/*** 取消订单回调 */
		public void sucessCallBack();
	}
}
