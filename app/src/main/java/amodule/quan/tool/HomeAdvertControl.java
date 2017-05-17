package amodule.quan.tool;

import android.content.Context;

import java.util.Map;

import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 首页数据控制
 * --单例模式
 *
 * @author yujian
 */
public class HomeAdvertControl {

	private volatile static HomeAdvertControl instance;

	public static HomeAdvertControl getInstance() {
		if (instance == null) {
			synchronized (HomeAdvertControl.class) {
				if (instance == null) {
					instance = new HomeAdvertControl();
				}
			}
		}
		return instance;
	}

	/**
	 * 广告统计
	 *
	 * @param context
	 * @param map
	 * @param onClickSite
	 */
	public void advertStatisticRequest(Context context, Map<String, String> map, String onClickSite) {
		String url = StringManager.api_monitoring_5;
		ReqInternet.in().doGet(url + "?adType=" + map.get("type") + "&adid=" + map.get("showAdid") + "&cid=" + map.get("showCid") +
				"&mid=" + map.get("showMid") + "site=" + map.get("showSite") + "&event=click&clickSite=" + onClickSite, new InternetCallback(context) {

			@Override
			public void loaded(int flag, String url, Object msg) {
			}
		});
	}
}
