package third.mall.aplug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseActivity;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.main.Main;
import aplug.basic.ReqInternet;
import aplug.web.tools.WebviewManager;
import third.mall.MainMall;
import third.mall.alipay.MallAlipay;
import third.mall.override.MallBaseActivity;
import third.mall.wx.WxPay;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;

/**
 * 商城公共部分
 * 
 * @author yu
 * 
 */
public class MallCommon {
	public static String mall_key = "";
	public static String mall_value = "";
	public static String customer_code = "";
	public static String token = "";
	public static final String file_mall = "mall_values";
	public static int deg = 0;//token循环过期
	public static int deg_past = 0;//外部接口循环过期
	public static int deg_token = 0;//外部接口循环过期
	public static boolean state_token=false;
	public static final String code_past="100002";//token 过期code
	public static final int request_num=3;//请求次数
	private Context context;
	public static int num_shopcat=0;
	public static InterfaceMallReqIntert interfaceMall;//接口回调
	public static OnRegisterSuccessCallback onRegisterSuccessCallback;
	public static int new_product = 0;//新的商品数量
	public static String ds_home_url="";//首页加载url
	public static String payment_order_id="";//支付id
	public static final int sucess_data=1000; 
	public static final int sucess_data_no=1001;
	public static boolean click_state= false;
	public static int num=0;
	public static boolean isShowMallAdvert= true;

	public MallCommon(Context context) {
		this.context = context.getApplicationContext();
	}
	public void setLoading(InterfaceMallReqIntert interfaceMall){
		MallCommon.interfaceMall=interfaceMall;
		if(num>2){
			num=0;
			return;
		}else{
			num++;
		}
		deg=0;
		deg_token=0;
		state_token=false;
		if(!TextUtils.isEmpty(mall_key)&& !TextUtils.isEmpty(mall_value)){
			setRegister(context);
		}
	}

	/**
	 * 获取token
	 * 
	 * @param context
	 */
	public static void setDsToken(final Context context){
		if(state_token){
			//失败
			onRegisterSuccessCallback = null;
			return;
		}
		String acticonUrl = MallStringManager.mall_getDsToken;
		MallReqInternet.in().doGet(acticonUrl, new MallInternetCallback(context) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					deg_token=0;
					state_token=false;
					ArrayList<Map<String, String>> listMapByJson = UtilString
							.getListMapByJson(msg);
					mall_key = listMapByJson.get(0).get("key");
					mall_value = listMapByJson.get(0).get("value");
					setRegister(context);
				}else{
					//最大请求三次
					deg_token++;
					if(deg_token>=request_num){
						state_token=true;
					}
					setDsToken(context);
				}

			}
		});
	}

	/**
	 * 注册用户
	 * 
	 * @param context
	 */
	public static void setRegister(final Context context) {
		if(state_token){
			//失败
			if(interfaceMall!=null)
				interfaceMall.setState(UtilInternet.REQ_CODE_ERROR);
			onRegisterSuccessCallback = null;
			return;
		}
		String url = MallStringManager.mall_api_register;
		MallReqInternet.in().doGet(url, new MallInternetCallback(context) {

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if (flag >= UtilInternet.REQ_OK_STRING) {
					//初始化一些参数
					deg=0;
					state_token=false;
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					customer_code = listMapByJson.get(0).get("customer_code");
					token = listMapByJson.get(0).get("token");
					setSaveMall(context);
					WebviewManager.syncDSCookie();
					if(interfaceMall!=null)
						interfaceMall.setState(flag);
					//成功
					if(onRegisterSuccessCallback != null){
						onRegisterSuccessCallback.onRegisterSuccess();
						onRegisterSuccessCallback = null;
					}
				}else{
					deg++;
					if(deg>=request_num){
						state_token=true;
					}
					setDsToken(context);
				} 
				
			
			}
		});
	}

	/**
	 * 存储数据
	 * 
	 * @param context
	 */
	private static void setSaveMall(Context context) {
		try {
			JSONObject jsonObject= new JSONObject();
			jsonObject.put("mall_key", MallCommon.mall_key);
			jsonObject.put("mall_value", MallCommon.mall_value);
			jsonObject.put("customer_code", MallCommon.customer_code);
			jsonObject.put("token", MallCommon.token);
			UtilFile.saveShared(context, file_mall, file_mall,jsonObject.toString());
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	public static void getSaveMall(Context context) {
		Object msg=UtilFile.loadShared(context, file_mall, file_mall);
		ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
		if(listMapByJson.size()>0){
			Map<String,String> map= listMapByJson.get(0);
			if (map.size() >= 4) {
				MallCommon.mall_key = map.get("mall_key");
				MallCommon.mall_value = map.get("mall_value");
				MallCommon.customer_code = map.get("customer_code");
				MallCommon.token = map.get("token");
			} else {
				state_token=false;
				setDsToken(context);
			}
		}else{
			state_token=false;
			setDsToken(context);
		}
	}

	
	/**
	 * 清除数据
	 * @param context
	 */
	public static void delSaveMall(Context context){
		UtilFile.delShared(context, file_mall, "");
		mall_key="";
		mall_value="";
		customer_code="";
		token="";
		deg=0;
		deg_token=0;
		deg_past=0;
		state_token=false;
		num_shopcat=0;
	}
	public interface InterfaceMallReqIntert {
		public abstract void setState(int state);
	}
	
	public interface InterfaceMallAddShopping {
		public abstract void addProduct(int state);
	}
	/**
	 * 获取购物车商品件数
	 * @param context
	 */
	public static void getShoppingNum(final Context context,final TextView view,final TextView view_two){
		MallReqInternet.in().doGet(MallStringManager.mall_getCartProductNum, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(flag>=ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					String num= listMapByJson.get(0).get("num");
					//保存到sp中
					num_shopcat= Integer.parseInt(num);
					if(num_shopcat>0){
						Main.setNewMsgNum(1,MallCommon.num_shopcat);
						if(num_shopcat>9){
							if(view!=null)view.setVisibility(View.GONE);
							if(view_two!=null)view_two.setVisibility(View.VISIBLE);
							if(num_shopcat>99)
								if(view_two!=null)view_two.setText("99+");
							else
								if(view_two!=null)view_two.setText(num_shopcat+"");
							
						}else{
							if(view!=null)view.setVisibility(View.VISIBLE);
							if(view_two!=null)view_two.setVisibility(View.GONE);
							if(view!=null)view.setText(num_shopcat+"");
						}
//						Main.setNewMsgNum(Integer.parseInt(CommonBottomView.BOTTOM_TWO),num_shopcat);
					}else{
						Main.setNewMsgNum(1,0);
						if(view!=null)view.setVisibility(View.GONE);
						if(view_two!=null)view_two.setVisibility(View.GONE);
					}
//					Main.setNewMsgNum(Integer.parseInt(CommonBottomView.BOTTOM_TWO),MallCommon.num_shopcat);
				}
			
			}
		});
	}
	/**
	 * 获取电商新品数量
	 * @param context
	 */
	public static void getDsInfo(final Activity context,final LoadManager loadManager){
		
		MallReqInternet.in().doGet(MallStringManager.mall_dsInfo_v3, new MallInternetCallback(context) {
			
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {

				if(loadManager!=null)
					loadManager.loadOver(flag, 1, true);
				if(flag>=ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					String num= listMapByJson.get(0).get("new_product");
					ds_home_url= listMapByJson.get(0).get("ds_home_url");
					if(!TextUtils.isEmpty(num)){
						new_product = Integer.parseInt(num);
//						Main.setNewMsgNum(Integer.parseInt(CommonBottomView.BOTTOM_TWO),new_product);
					}
					if(listMapByJson.get(0).containsKey("order_type")){
						UtilFile.saveShared(context, FileManager.MALL_ORDERLIST, FileManager.MALL_ORDERLIST, listMapByJson.get(0).get("order_type"));
					}
					click_state = listMapByJson.get(0).containsKey("click_flush") && "2".equals(listMapByJson.get(0).get("click_flush"));
					//统计规则
					if(listMapByJson.get(0).containsKey("uri_stat")){
						UtilFile.saveShared(context, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT, listMapByJson.get(0).get("uri_stat"));
						UtilFile.saveShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT, "");
					}
					//加载数据
					if(context instanceof MainMall){
						((MainMall)context).loadData();
					}
					//电商广告开关：1---不出，2---出
					isShowMallAdvert = !(listMapByJson.get(0).containsKey("advertise_dish") && "1".equals(listMapByJson.get(0).get("advertise_dish")));
					
					if(listMapByJson.get(0).containsKey("icon_stat")){
						UtilFile.saveShared(context, FileManager.MALL_STAT_BUT, FileManager.MALL_STAT_BUT, listMapByJson.get(0).get("icon_stat"));
					}
				}
			
				
			}
		});
	}
	
	/**
	 * 添加到购物车
	 * @param context
	 * @param code
	 * @param interfaceAddshop
	 */
	public  void addShoppingcat(final Context context,final String code,final InterfaceMallAddShopping interfaceAddshop){
		String param="product_code="+code+"&product_num="+1;
		MallReqInternet.in().doPost(MallStringManager.mall_addCartProduct, param, new MallInternetCallback(context) {
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				if(interfaceAddshop!=null){
					interfaceAddshop.addProduct(flag);
				}
				if(flag>=ReqInternet.REQ_OK_STRING){
					
				}else if(flag>=ReqInternet.REQ_CODE_ERROR){
					Map<String,String> map= (Map<String, String>) msg;;
					//处理code过期问题
					if(map==null)
						return;
					if(MallCommon.code_past.equals(map.get("code"))){
						setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									addShoppingcat(context, code,interfaceAddshop);
//								}else if(state==UtilInternet.REQ_CODE_ERROR){
								}
							}
						});
					}else{
						Tools.showToast(context, map.get("msg")+"");
					}
				}
			
			}
		});
	}
	/**
	 * 已有订单---去支付
	 * @param payment_order_id
	 */
	public void malldirect(final String payment_order_id,final BaseActivity contexts,final InterfaceMallPayState paystate){
		String type="";
		if("1".equals(MallPayType.pay_type)){
			type="wx";
		}else{
			type="alipay";
		}
		String actionUrl=MallStringManager.mall_api_direct;
		String param="payment_order_id="+payment_order_id+"&pay_type="+type;
		MallReqInternet.in().doPost(actionUrl, param, new MallInternetCallback(context) {
			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				paystate.payState(flag);
				if(flag>=UtilInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> listMapByJson = UtilString.getListMapByJson(msg);
					pay(listMapByJson,contexts);
				}else if(flag== UtilInternet.REQ_CODE_ERROR){
					Map<String,String> map= (Map<String, String>) msg;
					if(MallCommon.code_past.equals(map.get("code"))){
						setLoading(new InterfaceMallReqIntert() {
							
							@Override
							public void setState(int state) {
								if(state>=UtilInternet.REQ_OK_STRING){
									malldirect(payment_order_id,contexts,paystate);
//								}else if(state==UtilInternet.REQ_CODE_ERROR){
								}
							}
						});
					}
					Tools.showToast(context, map.get("msg"));
				}
			
			}
			
		});
	}
	private void pay(ArrayList<Map<String, String>> listMapByJson,BaseActivity context) {
		if("1".equals(MallPayType.pay_type)){//微信
			WxPay pay= WxPay.getInstance(context);
			pay.pay(listMapByJson);
		}else{//支付
			MallAlipay alipay= MallAlipay.getInstance();
			alipay.pay(context,listMapByJson);
		}
	}
	public interface InterfaceMallPayState {
		public abstract void payState(int state);
	}
	
	/**
	 * 对webview Url进行处理
	 * @param url
	 */
	public void setStatisticStat(String url){
		if(url.indexOf(MallStringManager.mall_web_apiUrl)>-1){//电商
			if(url.contains("?")){
				int index= url.indexOf("?");
				String param= url.substring(index+1,url.length());
				UtilFile.saveShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT, "");
			}
		}
	}
	/**
	 * 对统计路径进行处理
	 * @param url
	 * @param map_post
	 * @param isApi true是api,false为web
	 * @return
	 */
	public String setStatisticUrl(String url,LinkedHashMap<String, String> map_post,boolean isApi){
		String url_statistic=url;
		if(url.indexOf(MallStringManager.replaceUrl(MallStringManager.mall_api_register))>-1||url.indexOf(MallStringManager.replaceUrl(MallStringManager.mall_getDsToken))>-1){
			return url_statistic;
		}
		String url_temp = null;
		if(isApi){
			url_temp=url.replace(MallStringManager.mall_apiUrl, "");
		}else{
			url_temp=url.replace(MallStringManager.mall_web_apiUrl, "");
		}
		LinkedHashMap<String, String> map = null;
		if(url_temp.contains("?")){
			int  index=url_temp.indexOf("?");
			String params= url_temp.substring(index+1,url_temp.length());
			url_temp=url_temp.substring(0, index);
			 map = UtilString.getMapByString(params, "&", "=");
		}
		if(map_post!=null){
			map=map_post;
		}
		url_temp=url_temp.toLowerCase();
		Object msg=UtilFile.loadShared(context, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
		String mall_stat=(String) UtilFile.loadShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT);
		if(!TextUtils.isEmpty(mall_stat)){
			if(url_statistic.contains("?")) url_statistic+="&"+mall_stat;
			else url_statistic+="?"+mall_stat;
		}
		
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		if(list!=null&&list.size()>0&&list.get(0).containsKey(url_temp)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(url_temp));
			String[] stats=mall_stat.split("&");
			int index=1;
			if(stats.length>0){
				ArrayList<Integer> int_list= new ArrayList<Integer>();
				for (int i = 0,length=stats.length; i < length; i++) {
					if(stats[i].indexOf("fr")==0){
						String temp= stats[i].replace("fr", "").substring(0,1);
						int index_temp = Integer.parseInt(temp);
						int_list.add(index_temp);
					}
				}
				if(int_list.size()>0){
					Collections.sort(int_list);
					index+=int_list.get(int_list.size()-1);
				}
			}
			
			if(!url_statistic.contains("?")&&TextUtils.isEmpty(mall_stat)){
				url_statistic+="?fr"+index+"="+list_real.get(0).get("fr");
			}else url_statistic+="&fr"+index+"="+list_real.get(0).get("fr");
			
			if(!TextUtils.isEmpty(list_real.get(0).get("nc"))&& map!=null){
				if(list_real.get(0).get("nc").contains("|")){
					String[] strs=list_real.get(0).get("nc").split("|");
					String msg_str="";
					for (int i = 0,length=strs.length; i < length; i++) {
						msg_str+=map.get(strs[i]);
						if(i!=strs.length-1){
							msg_str+="|";
						}
					}
					url_statistic+="&fr"+index+"_msg="+msg_str;
				}else{
					url_statistic+="&fr"+index+"_msg="+map.get(list_real.get(0).get("nc"));
				}
			}
		}
		return url_statistic;
	}
	
	/**
	 * 对统计路径进行处理
	 * @param url
	 * @return
	 */
	public void setStatisticUrl(String url,String mall_stat){
		String url_temp=url.replace(MallStringManager.mall_apiUrl, "");
		LinkedHashMap<String, String> map = null;
		if(url_temp.contains("?")){
			int  index=url_temp.indexOf("?");
			String params= url_temp.substring(index+1,url_temp.length());
			url_temp=url_temp.substring(0, index);
			 map = UtilString.getMapByString(params, "&", "=");
		}
		url_temp=url_temp.toLowerCase();
		Object msg=UtilFile.loadShared(context, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
		
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		if(list!=null&&list.size()>0&&list.get(0).containsKey(url_temp)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(url_temp));
			String[] stats=mall_stat.split("&");
			int index=1;
			if(stats.length>0){
				ArrayList<Integer> int_list= new ArrayList<Integer>();
				for (int i = 0,length=stats.length; i < length; i++) {
					if(stats[i].indexOf("fr")==0){
						String temp= stats[i].replace("fr", "").substring(0,1);
						int index_temp = Integer.parseInt(temp);
						int_list.add(index_temp);
					}
				}
				if(int_list.size()>0){
					Collections.sort(int_list);
					index+=int_list.get(int_list.size()-1);
				}
			}
			
			if(TextUtils.isEmpty(mall_stat)){
				mall_stat+="?fr"+index+"="+list_real.get(0).get("fr");
			}else mall_stat+="&fr"+index+"="+list_real.get(0).get("fr");
			
			if(!TextUtils.isEmpty(list_real.get(0).get("nc"))&& map!=null){
				if(list_real.get(0).get("nc").contains("|")){
					String[] strs=list_real.get(0).get("nc").split("|");
					String msg_str="";
					for (int i = 0,length=strs.length; i < length; i++) {
						msg_str+=map.get(strs[i]);
						if(i!=strs.length-1){
							msg_str+="|";
						}
					}
					mall_stat+="&fr"+index+"_msg="+msg_str;
				}else{
					mall_stat+="&fr"+index+"_msg="+map.get(list_real.get(0).get("nc"));
				}
			}
		}
		UtilFile.saveShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT, mall_stat);
	}
	/**
	 * 更改intent
	 * @param but_tag
	 * @param intent
	 * @return
	 */
	public Intent setStatistic(String but_tag,Intent intent){
		UtilFile.saveShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT, "");
		Object msg=UtilFile.loadShared(context, FileManager.MALL_STAT_BUT, FileManager.MALL_STAT_BUT);
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		
		if(list!=null&&list.size()>0&&list.get(0).containsKey(but_tag)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(but_tag));
			intent.putExtra("fr1", list_real.get(0).get("fr"));
		}
		return intent;
	}
	/**
	 * 更改String
	 * @param but_tag
	 * @return
	 */
	public String setStatistic(String but_tag){
		UtilFile.saveShared(context, FileManager.MALL_STAT, FileManager.MALL_STAT, "");
		Object msg=UtilFile.loadShared(context, FileManager.MALL_STAT_BUT, FileManager.MALL_STAT_BUT);
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		String url = "";
		if(list!=null&&list.size()>0&&list.get(0).containsKey(but_tag)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(but_tag));
			url="fr1="+list_real.get(0).get("fr");
		}

		return url;
	}

	public interface OnRegisterSuccessCallback{
		void onRegisterSuccess();
	}
    public static String statictisFrom = "";
    public static String getStatictisFrom(){
        String data=statictisFrom;
        statictisFrom="";
        return data;
    }
    public static void setStatictisFrom(String dsfrom){
        if(TextUtils.isEmpty(dsfrom)){
            return;
        }
        MallCommon.statictisFrom+=TextUtils.isEmpty(MallCommon.statictisFrom)?dsfrom: MallBaseActivity.PAGE_LOGO+dsfrom;
		String ds_from=FileManager.loadShared(XHActivityManager.getInstance().getCurrentActivity(),FileManager.xmlFile_appInfo,FileManager.xmlKey_ds_from_show).toString();
		if("2".equals(ds_from)){
			Log.i("xianghaTag","ds_from::"+statictisFrom);
			Tools.showToast(XHActivityManager.getInstance().getCurrentActivity(),statictisFrom);
		}
    }

}
