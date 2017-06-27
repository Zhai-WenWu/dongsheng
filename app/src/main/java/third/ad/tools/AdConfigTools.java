package third.ad.tools;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.dialogManager.ADPopwindiwManager;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.ad.AdParent;
import xh.basic.tool.UtilString;

public class AdConfigTools {
	private volatile static AdConfigTools mAdConfigTools;

	private String showAdId = "cancel";

	private AdConfigTools(){}
	public ArrayList<Map<String, String>> list = new ArrayList<>();//服务端广告集合

	public static AdConfigTools getInstance(){
		if(mAdConfigTools == null){
			synchronized (AdConfigTools.class) {
				if(mAdConfigTools == null){
					mAdConfigTools = new AdConfigTools();
				}
			}
		}
		return mAdConfigTools;
	}

	public void getAdConfigInfo(){
		// 请求网络信息
		ReqInternet.in().doGet(StringManager.api_adData, new InternetCallback(XHApplication.in()) {
			@Override
			public void loaded(int flag, String url, final Object returnObj) {
				if (flag >= ReqInternet.REQ_OK_STRING) {
					FileManager.saveFileToCompletePath(FileManager.getDataDir() + FileManager.file_ad, (String) returnObj, false);
					ArrayList<Map<String, String>> list = StringManager.getListMapByJson(returnObj);
					Map<String,String> map;
					if(list != null && list.size() > 0){
						map = list.get(0);
						list = StringManager.getListMapByJson(map.get(AdPlayIdConfig.FULLSCREEN));
						if(list != null && list.size() > 0){
							map = list.get(0);
							String banner = map.get("banner");
							list = StringManager.getListMapByJson(banner);
							if(list != null && list.size() > 0){
								map = list.get(0);
								ArrayList<Map<String, String>> imgsList = StringManager.getListMapByJson(map.get("imgs"));
								JSONArray array = new JSONArray();
								JSONObject object = new JSONObject();
								if(imgsList != null && imgsList.size() > 0){
									Map<String, String> imgsMap = imgsList.get(0);
									DisplayMetrics dm = ToolsDevice.getWindowPx(Main.allMain);
									float beishu = (float) (dm.heightPixels * 1.0) / dm.widthPixels;
									try{
										if(Math.abs(beishu - (2900 / 1700)) > Math.abs(beishu - (2730 / 2000))){
											object.put("img",imgsMap.get("indexImg2"));
										}else{
											object.put("img",imgsMap.get("indexImg1"));
										}
										object.put("url", map.get("url"));
										object.put("showNum", map.get("showNum"));
										object.put("times", map.get("times"));
										object.put("delay", map.get("delay"));
										array.put(object);
									}catch(Exception ignored){

									}
									ADPopwindiwManager.saveWelcomeInfo(array.toString());
								}
							}
						}
					}
				}
			}
		});
	}

	/**
	 * 请求美食圈列表广告
	 * @param context
	 */
	public void setRequest(Context context) {
		String url = StringManager.api_getQuanList;
		ReqInternet.in().doGet(url,new InternetCallback(context) {
			@Override
			public void loaded(int flag, String url, Object msg) {
				if (flag >= ReqInternet.REQ_OK_STRING) {
					list = UtilString.getListMapByJson(msg);
				}
			}
		});
//		QuanAdvertControl.getInstance().getGdtData(context);
//		HomeAdvertControl.getInstance().getGdtData(context);
	}

	public Map<String,String> getAdConfigData(String adPlayId){
		String data = FileManager.readFile(FileManager.getDataDir() + FileManager.file_ad);
		ArrayList<Map<String, String>> list = StringManager.getListMapByJson(data);
		Map<String,String> map = new HashMap<String, String>();
		if(list != null && list.size() > 0){
			map = list.get(0);
			list = StringManager.getListMapByJson(map.get(adPlayId));
			if(list != null && list.size() > 0){
				map = list.get(0);
			}else{
				map = new HashMap<String, String>();
			}
		}
		return map;
	}

	/**
	 * 通过搜菜谱，输入指定指令显示对应广告
	 * @param ad
	 */
	public void changeAd(String ad){
		if("gdt".equals(ad)){
			showAdId = AdParent.ADKEY_GDT;
		}else if("jd".equals(ad)){
			showAdId = AdParent.ADKEY_JD;
		}else if("banner".equals(ad)){
			showAdId = AdParent.ADKEY_BANNER;
		}else if("cancel".equals(ad)){
			showAdId = "cancel";
		}
	}

	public boolean isShowAd(String adPlayId,String adKey){
		if(showAdId.equals("cancel")){
			String isGourmet = LoginManager.userInfo.get("isGourmet");
			//是美食家，但不是banner广告则返回不显示广告
			if(!TextUtils.isEmpty(isGourmet) && Integer.parseInt(isGourmet) == 2 && !AdParent.ADKEY_BANNER.equals(adKey)){
				return false;
			}

			Map<String,String> mData = getAdConfigData(adPlayId);
			String value = mData.get(adKey);
			if("2".equals(value)){
				return true;
			}
		}else if(showAdId.equals("level")){
			return true;
		}else{
			return adKey.equals(showAdId);
		}
//		if(adKey.equals(AdParent.ADKEY_JD))
//			return true;
//		else if(adKey.equals(AdParent.ADKEY_BANNER))
//			return true;
		return false;
	}

	public void onAdShow(Context context,String channel,String twoLevel, String threeLevel){
		if(TextUtils.isEmpty(twoLevel)) return;
		if(AdParent.TONGJI_TX_API.equals(channel))
			XHClick.mapStat(context, "ad_show", twoLevel, threeLevel);
	}

	public void onAdClick(Context context,String channel,String twoLevel, String threeLevel){
		if(TextUtils.isEmpty(twoLevel)) return;
		if(AdParent.TONGJI_TX_API.equals(channel))
			XHClick.mapStat(context, "ad_click", twoLevel, threeLevel);
	}

	/**
	 * 普通广告位统计
	 * @param adPlayId ： 广告位id
	 * @param channel ：渠道 baidu、jingdong、banner
	 * @param bannerId ：bannerId
	 * @param event 事件  click：点击   show：展现
     * @param adType 广告类型  普通广告位，开屏广告位
     */
	public void postTongji(String adPlayId,String channel,String bannerId,String event,String adType){
//		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//		map.put("adType", adType);
//		map.put("id", adPlayId);
//		map.put("channel", channel);
//		map.put("bannerId", bannerId);
//		map.put("event", event);
//		Log.i("FRJ","普通广告位统计:" + "adType=" + adType +"&id=" + adPlayId + "&channel="+ channel +
//						"&bannerId=" + bannerId + "&event=" + event);
		ReqInternet.in().doGet(StringManager.api_monitoring_5 + "?adType=" + adType +"&id=" + adPlayId + "&channel="+ channel +
				"&bannerId=" + bannerId + "&event=" + event,new InternetCallback(XHApplication.in()) {
			@Override
			public void loaded(int flag, String url,Object returnObj) {

			}
		});
	}

	/**
	 * 美食圈列表广告统计
	 * @param context
	 * @param map
	 * @param onClickSite 点击的位置 overall：整体、user：用户、time：时间、quanName：圈子名称、content：评论、like：赞
	 */
	public void postTongjiQuan(Context context, Map<String, String> map, String onClickSite,String event) {
		String url = StringManager.api_monitoring_5;
		if(TextUtils.isEmpty(onClickSite)) onClickSite = "overall";
		else if("用户头像".equals(onClickSite)){
			onClickSite = "user";
		}else if("用户昵称".equals(onClickSite)){
			onClickSite = "user";
		}else if("贴子内容".equals(onClickSite)){
			onClickSite = "overall";
		}else if("评论".equals(onClickSite)){
			onClickSite = "content";
		}else{
			onClickSite = "overall";
		}

//		Log.i("FRJ", "美食圈列表广告统计:" + url + "?adType=圈子广告位" + "&adid=" + map.get("showAdid") + "&cid=" + map.get("showCid") +
//				"&mid=" + map.get("showMid") + "site=" + map.get("showSite") + "&event=" + event + "&clickSite=" + onClickSite);
//		String param="monitoringData="+getJsonParam(map, onClickIndex);
		ReqInternet.in().doGet(url + "?adType=圈子广告位" + "&adid=" + map.get("showAdid") + "&cid=" + map.get("showCid") +
				"&mid=" + map.get("showMid") + "site=" + map.get("showSite") + "&event=" + event + "&clickSite=" + onClickSite, new InternetCallback(context) {

			@Override
			public void loaded(int flag, String url, Object msg) {
			}
		});
	}

	/**
	 * 广告位 点击
	 * @param id 广告位id
	 * @param adType baidu jingdong banner
	 * @param adTypeId 广告类型id，第三方广告可传0
	 */
	public void clickAds(String id,String adType,String adTypeId){
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("type", "position");
		map.put("id", id);
		map.put("adType", adType);
		map.put("adTypeId", adTypeId);
		ReqInternet.in().doPost(StringManager.api_clickAds, map,new InternetCallback(XHApplication.in()) {
			@Override
			public void loaded(int flag, String url,Object returnObj) {

			}
		});
	}
	/**
	 * 生活圈列表 广告点击
	 * @param id 广告id
	 */
	public void clickAds(String id){
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("type", "quanList");
		map.put("id", id);
		ReqInternet.in().doPost(StringManager.api_clickAds, map,new InternetCallback(XHApplication.in()) {
			@Override
			public void loaded(int flag, String url,Object returnObj) {

			}
		});
	}

	//广告位假数据
//	Map<String,String> map = new HashMap<String, String>();
//	JSONArray array = new JSONArray();
//	JSONObject object = new JSONObject();
//	try {
//		object.put("name", "电商专题标题");
//		object.put("subhead", "电商专题内容");
//    	JSONArray array2 = new JSONArray();
//    	JSONObject object2 = new JSONObject();
//    	object2.put("appImg", "http://s1.cdn.xiangha.com/zhishi/201604/081538238234.jpg/OTAweDYwMA");
//    	array2.put(object2);
//    	object.put("imgs", array2.toString());
//    	array.put(object);
//    	map.put("banner", array.toString());
//	} catch (JSONException e) {
//		e.printStackTrace();
//	}
	//全屏广告假数据
//    JSONArray array00 = new JSONArray();
//	JSONObject object00 = new JSONObject();
//	JSONArray array = new JSONArray();
//	JSONObject object = new JSONObject();
//	JSONArray array0 = new JSONArray();
//	JSONObject object0 = new JSONObject();
//	try {
//		object.put("name", "电商专题标题");
//		object.put("subhead", "电商专题内容");
//    	JSONArray array2 = new JSONArray();
//    	JSONObject object2 = new JSONObject();
//    	object2.put("appImg", "http://s1.cdn.xiangha.com/zhishi/201604/081538238234.jpg/OTAweDYwMA");
//    	array2.put(object2);
//    	object.put("imgs", array2.toString());
//    	JSONArray array3 = new JSONArray();
//    	JSONObject object3 = new JSONObject();
//    	object3.put("img", "http://static.cnbetacdn.com/thumb/article/2016/0505/8d0a5d28f0cdc3a.jpg_600x600.jpg");
//    	object3.put("showNum","3");
//    	object3.put("times","3");
//    	object3.put("delay","5");
//    	object3.put("url","http://www.baidu.com");
//    	array3.put(object3);
//    	object.put("proBitBox", array3);
//    	array0.put(object);
//    	object0.put("banner", array0.toString());
//    	array.put(object0);
//    	object00.put(AdPlayIdConfig.FULLSCREEN, array.toString());
//    	array00.put(object00);
//	} catch (JSONException e) {
//		e.printStackTrace();
//	}

}
