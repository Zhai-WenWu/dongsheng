package third.mall.aplug;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.XHApplication;
import xh.basic.internet.InterCallback;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import acore.tools.FileManager;
import acore.tools.StringManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class MallReqInternet extends UtilInternet{
	private static MallReqInternet instance=null;
	public static String time_mall="";
	public static String dsHmac="";
	
	private MallReqInternet(){
		super();
	}
	public static MallReqInternet init() {
		return in();
	}

	public static MallReqInternet in() {
		if(instance==null) 
			instance=new MallReqInternet();
		return instance;
	}
	
	/**
	 * 单独获取标准header头
	 * @return
	 */
	public Map<String,String> getHeader(Context context){
		MallInternetCallback callback=new MallInternetCallback(){

			@Override
			public void loadstat(int flag, String url, Object msg, Object... stat) {
				
			}
		};
		Map<String,String> header=callback.getReqHeader(new HashMap<String, String>(),"",new LinkedHashMap<String,String>());
		callback.finish();
		return header;
	}
	
	@Override
	public void doGet(String url, InterCallback callback) {
		url = MallStringManager.replaceUrl(url);
		Log.i("wyl","url:doGet:"+url);
		if(!url.equals(MallStringManager.mall_getDsToken))
			setMD5(url);
		url=setStatisticUrl(url);
		super.doGet(url, callback);
	}

	@Override
	public void doPost(String actionUrl, String param, InterCallback callback) {
		actionUrl = MallStringManager.replaceUrl(actionUrl);
		Log.i("wyl","actionUrl:doPost:"+actionUrl);
		setMD5(actionUrl);
		actionUrl=setStatisticUrl(actionUrl);
		super.doPost(actionUrl, param, callback);
	}

	@Override
	public void doPost(String actionUrl, LinkedHashMap<String, String> map,InterCallback callback) {
		actionUrl = MallStringManager.replaceUrl(actionUrl);
		Log.i("wyl","actionUrl:doPost::22:"+actionUrl);
		setMD5(actionUrl);
		actionUrl=setStatisticUrl(actionUrl);
		super.doPost(actionUrl, map, callback);
	}

	private void setMD5(String url){
		url=url.replace(MallStringManager.mall_apiUrl, "");
		if(url.contains("?")){
			int  index=url.indexOf("?");
			 url= url.substring(0, index);
		}
		url= url.toLowerCase();
		String code=StringManager.stringToMD5(MallCommon.customer_code+MallCommon.token).toLowerCase();
		long time=System.currentTimeMillis()/1000;
		time_mall=String.valueOf(time);
		
		dsHmac=StringManager.stringToMD5(code+(url+time_mall).toLowerCase()).toLowerCase();
	}
	private String setStatisticUrl(String url){
		if(url.indexOf(MallStringManager.replaceUrl(MallStringManager.mall_api_register))>-1||url.indexOf(MallStringManager.replaceUrl(MallStringManager.mall_getDsToken))>-1){
			return url;
		}
		String mall_stat=(String) UtilFile.loadShared(XHApplication.in(), FileManager.MALL_STAT, FileManager.MALL_STAT);
//		mall_stat=setStatisticUrl(url, mall_stat);
		if(url.contains("?")){
			url+="&"+mall_stat;
		}else{
			url+="?"+mall_stat;
		}
		return url;
	}
}
