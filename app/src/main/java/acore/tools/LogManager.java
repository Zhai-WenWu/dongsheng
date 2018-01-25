package acore.tools;

import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aplug.basic.XHConf;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;
import acore.override.XHApplication;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

public class LogManager extends UtilLog {
	/**
	 * 获取要上报的日志信息
	 * @param type
	 * @param status
	 * @param addTime
	 * @param content
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, String> getReportLog(String type,String status,String addTime,Object content){
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("type", type);
		map.put("status", status);
		map.put("addTime", addTime);
		if(content instanceof Map){
			JSONArray jsonArray = new JSONArray();
			Map<String,Object> contentMap = (Map<String, Object>) content;
			for(String key : contentMap.keySet()){
				Object value = contentMap.get(key);
				if(value instanceof Map){
					Map<String,String> valueMap = (Map<String, String>) value;
					JSONObject jsonObject = new JSONObject();
					JSONObject jsonObject2 = new JSONObject(valueMap);
					try {
						jsonObject.put(key, jsonObject2.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonArray.put(jsonObject);
				}else{
					JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put(key, value);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					jsonArray.put(jsonObject);
				}
			}
			map.put("content", jsonArray.toString());
		}else if(content instanceof JSONArray){
			map.put("content", content.toString());
		}else if(content instanceof JSONObject){
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(content);
			map.put("content", jsonArray.toString());
		}else{
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("content", content.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonObj);
			map.put("content", jsonArray.toString());
		}
		return map;
	}

	public static void printStartTime(String tag,String text){
		if(XHApplication.in() != null && Tools.isDebug(XHApplication.in())){
			if(TextUtils.isEmpty(tag)){
				tag = XHConf.log_tag_default;
			}
			long endTime=System.currentTimeMillis();
			Log.i(tag,text + (endTime - XHApplication.in().startTime));
		}
	}
}
