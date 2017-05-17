package third.mall.aplug;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.tools.FileManager;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;


/**
 * 电商点击控制
 * 
 * @author yujian ---单例模式
 */
public class MallClickContorl {

	private volatile static MallClickContorl instance = null;

	public static MallClickContorl getInstance() {
		if (instance == null) {
			synchronized (MallClickContorl.class) {
				if (instance == null) {
					instance = new MallClickContorl();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 对统计路径进行处理
	 * @param url
	 * @return
	 */
	public void setStatisticUrl(String url,String url_params,String mall_stat,Context context){
		if(TextUtils.isEmpty(url)||TextUtils.isEmpty(mall_stat))
			return;
		
		String url_temp=url.replace(MallStringManager.replaceUrl(MallStringManager.mall_apiUrl), "");
		LinkedHashMap<String, String> map = null;
		if(url_temp.contains("?")){
			int  index=url_temp.indexOf("?");
			String params= url_temp.substring(index+1,url_temp.length());
			url_temp=url_temp.substring(0, index);
			 map = UtilString.getMapByString(params, "&", "=");
		}
		if(!TextUtils.isEmpty(url_params)){
			if(map==null){
				map = UtilString.getMapByString(url_params, "&", "=");
			}else{
				LinkedHashMap<String, String> map_url = UtilString.getMapByString(url_params, "&", "=");
				for (String key : map_url.keySet()) {
						map.put(key, map_url.get(key));
					}
			}
		}
		url_temp=url_temp.toLowerCase();
		Object msg=UtilFile.loadShared(context, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
		System.out.println("");
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		if(list!=null&&list.size()>0&&list.get(0).containsKey(url_temp)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(url_temp));
			String[] stats=mall_stat.split("&");
			int index=1;
			if(stats.length>0){
				LinkedHashMap<String, String> list_map= UtilString.getMapByString(mall_stat, "&", "=");
				ArrayList<Integer> int_list= new ArrayList<Integer>();
				for (int i = 0,length=stats.length; i < length; i++) {
					if(stats[i].indexOf("fr")==0&&stats[i].contains("=")){
						try{
							String[] temps=stats[i].split("=");
							String temp=temps[0].replace("fr", "");
							int index_temp = Integer.parseInt(temp);
							int_list.add(index_temp);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
				if(int_list.size()>0){
					Collections.sort(int_list);
					index+=int_list.get(int_list.size()-1);
				}
//				if(list_map!=null&&!TextUtils.isEmpty(list_map.get("fr"+(index-1)))&&list_map.get("fr"+(index-1)).equals(list_real.get(0).get("fr"))){
//					return;
//				}
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
	 * 对统计路径进行处理---web 判断
	 * @param url
	 * @return
	 */
	public void setStatisticUrl(String url,String url_params,String mall_stat,Context context,boolean isShowWeb){
		if(TextUtils.isEmpty(url)||TextUtils.isEmpty(mall_stat))
			return;
		
		String url_temp=url.replace(MallStringManager.replaceUrl(MallStringManager.mall_web_apiUrl), "");
		LinkedHashMap<String, String> map = null;
		if(url_temp.contains("?")){
			int  index=url_temp.indexOf("?");
			String params= url_temp.substring(index+1,url_temp.length());
			url_temp=url_temp.substring(0, index);
			 map = UtilString.getMapByString(params, "&", "=");
		}
		if(!TextUtils.isEmpty(url_params)){
			if(map==null){
				map = UtilString.getMapByString(url_params, "&", "=");
			}else{
				LinkedHashMap<String, String> map_url = UtilString.getMapByString(url_params, "&", "=");
				for (String key : map_url.keySet()) {
						map.put(key, map_url.get(key));
					}
			}
		}
		url_temp=url_temp.toLowerCase();
		Object msg=UtilFile.loadShared(context, FileManager.MALL_URI_STAT, FileManager.MALL_URI_STAT);
		System.out.println("");
		ArrayList<Map<String,String>> list=UtilString.getListMapByJson(msg);
		if(list!=null&&list.size()>0&&list.get(0).containsKey(url_temp)){
			ArrayList<Map<String,String>> list_real=UtilString.getListMapByJson(list.get(0).get(url_temp));
			String[] stats=mall_stat.split("&");
			int index=1;
			if(stats.length>0){
				LinkedHashMap<String, String> list_map= UtilString.getMapByString(mall_stat, "&", "=");
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
//				if(list_map!=null&&!TextUtils.isEmpty(list_map.get("fr"+(index-1)))&&list_map.get("fr"+(index-1)).equals(list_real.get(0).get("fr"))){
//					return;
//				}
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
}
