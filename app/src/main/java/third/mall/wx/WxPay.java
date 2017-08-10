package third.mall.wx;

import android.app.Activity;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xianghatest.wxapi.WXPayEntryActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import acore.logic.PayCallback;
import acore.tools.Tools;
import xh.basic.tool.UtilString;

/**
 * 微信支付
 * 
 * @author Administrator
 *
 */
public class WxPay {

	private static WxPay wxpay = null;
	private Activity context;
	private IWXAPI api;
	public static String amt;

	private WxPay(Activity context) {
		this.context = context;
		api = WXAPIFactory.createWXAPI(context, WXPayEntryActivity.app_id);
	}

	public static synchronized WxPay getInstance(Activity context) {
		if (wxpay == null) {
			wxpay = new WxPay(context);
		}
		return wxpay;
	}

	/**
	 * 调用支付 "1279bc2c2758c2f3b03a5df958097606"; "Sign=WXPay";
	 * @param listMapByJson
	 */
	public void pay(ArrayList<Map<String, String>> listMapByJson) {
		boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;//微信是否支持支付
		if(!isPaySupported){
			Tools.showToast(context,"您的微信版本不支持支付功能，请升级微信~");
			return;
		}
		amt= listMapByJson.get(0).get("amt");
		ArrayList<Map<String, String>> listMapByJson_wx = UtilString.getListMapByJson(listMapByJson.get(0).get("wx_prepay"));
		PayReq req = new PayReq();
		req.appId = WXPayEntryActivity.app_id;
		req.partnerId = listMapByJson_wx.get(0).get("partnerId");
		req.prepayId = listMapByJson_wx.get(0).get("prepayId");
		req.nonceStr = listMapByJson_wx.get(0).get("nonceStr");
		req.timeStamp = listMapByJson_wx.get(0).get("timeStamp");
		req.packageValue = listMapByJson_wx.get(0).get("package");
		req.sign = setMd5Sign(listMapByJson_wx.get(0));
		api.registerApp(WXPayEntryActivity.app_id);
		Toast.makeText(context, "正常调起支付", Toast.LENGTH_SHORT).show();
		context.finish();
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		api.sendReq(req);
	}

	/**
	 * 调用支付 "1279bc2c2758c2f3b03a5df958097606"; "Sign=WXPay";
	 * @param mapByJson
	 */
	public void pay(Map<String, String> mapByJson) {
		boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;//微信是否支持支付
		if(!isPaySupported){
			if(PayCallback.getPayCallBack() != null){
				PayCallback.getPayCallBack().onPay(false,"您的微信版本不支持支付功能，请升级微信~");
			}else{
				Tools.showToast(context,"您的微信版本不支持支付功能，请升级微信~");
			}
			return;
		}
		PayReq req = new PayReq();
		req.appId = WXPayEntryActivity.app_id;
		req.partnerId = mapByJson.get("partnerId");
		req.prepayId = mapByJson.get("prepayId");
		req.nonceStr = mapByJson.get("nonceStr");
		req.timeStamp = mapByJson.get("timeStamp");
		req.packageValue = mapByJson.get("package");
		req.sign = setMd5Sign(mapByJson);
		api.registerApp(WXPayEntryActivity.app_id);
//		Toast.makeText(context, "正常调起支付", Toast.LENGTH_SHORT).show();
		// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
		api.sendReq(req);
	}

	private String setMd5Sign(Map<String, String> listMapByJson_wx) {
		// 签名：
		Map<String, String> map = new HashMap<String, String>();
		map.put("appid", WXPayEntryActivity.app_id);
		map.put("partnerid", listMapByJson_wx.get("partnerId"));
		map.put("prepayid", listMapByJson_wx.get("prepayId"));
		map.put("noncestr", listMapByJson_wx.get("nonceStr"));
		map.put("timestamp", listMapByJson_wx.get("timeStamp"));
		map.put("package", listMapByJson_wx.get("package"));

		Map<String, String> resultMap = sortMapByKey(map);
		String m = "";
		for (Map.Entry<String, String> entry : resultMap.entrySet()) {
			if (!entry.getKey().equals("") && !entry.getKey().equals("sign") && !entry.getKey().equals("key"))
				m += entry.getKey() + "=" + entry.getValue() + "&";
		}
		m+="key=201511252253XiangHaDs0518TianJin";
		String sign = null;
		try {
			sign = MD5.getMessageDigest(m.getBytes("UTF-8")).toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sign;
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	// 比较器类
	public static class MapKeyComparator implements Comparator<String> {
		public int compare(String str1, String str2) {
			return str1.compareTo(str2);
		}
	}
}
