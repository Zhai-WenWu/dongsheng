package amodule.quan.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilString;
import acore.logic.LoginManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.quan.db.CircleSqlite.CircleDB;
import android.content.Context;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

/**
 * 我的圈子数据管理
 * 
 * @author Administrator
 *
 */
public class MyQuanDataControl {
	
	/**
	 * 通过网络获取数据
	 * @param context
	 * @param dataCallback
	 */
	public static void getNewMyQuanData(Context context,final DataCallback dataCallback) {
		ReqInternet.in().doGet(StringManager.api_circleMyQuan, new InternetCallback(context) {

			@Override
			public void loaded(int flag, String url, Object msg) {
				ArrayList<Map<String, String>> list_map;
				if(LoginManager.isLogin()){
					list_map=getMsgData(flag, msg);
				}else{
					list_map=getQuanListData(context, getMsgData(flag, msg));
				}
				
				if(dataCallback!=null)
					dataCallback.setMyQuanData(list_map);
			}
		});
	}
	private static ArrayList<Map<String, String>> getMsgData(int flag,  Object msg){
		ArrayList<Map<String, String>> list_map;
		if (flag >= ReqInternet.REQ_OK_STRING) {//成功--取网络
			list_map = UtilString.getListMapByJson(msg);
			UtilFile.saveFileToCompletePath(UtilFile.getDataDir() + FileManager.file_myquan, msg.toString(), true);//存储到本地
		}else{ 
			list_map=UtilString.getListMapByJson(getMyQuanData());//失败取本地
		}
		return list_map;
	}

	/**
	 * 获取本地数据
	 * 
	 * @return
	 */
	public static String getMyQuanData() {
		String myQuan = UtilFile.readFile(UtilFile.getDataDir() + FileManager.file_myquan);
		return myQuan;
	}

	public interface DataCallback {
		public void setMyQuanData(ArrayList<Map<String,String>> myQuanDataList);
	}
	
	public static ArrayList<Map<String,String>> getQuanListData(Context context){
		return getQuanListData(context, null);
	}
	/**
	 * 获得我的圈子数据
	 * @return
	 */
	private static ArrayList<Map<String,String>> getQuanListData(Context context,ArrayList<Map<String,String>> MyQuanDataList){
		if(MyQuanDataList==null)
		MyQuanDataList = UtilString.getListMapByJson(getMyQuanData());
		int myQuanDataSize = MyQuanDataList.size();
		CircleSqlite sqlite = new CircleSqlite(context);
		if(myQuanDataSize < 6){
			String moundAndCircle = FileManager.readFile(FileManager.getDataDir() + FileManager.file_indexModuleAndRecCircle);
			 ArrayList<Map<String, String>> array = StringManager.getListMapByJson(moundAndCircle);
			 if(array != null && array.size() > 0){
				 Map<String,String> map = array.get(0);
				 String recQuan = map.get("recQuan");
				 String[] resQuans = recQuan.split(",");
				 for(String rq : resQuans){
					 boolean isHave = false;
					 int size= MyQuanDataList.size();
					 for(int i=0; i<size;i++){
						 String cid= MyQuanDataList.get(i).get("cid");
						 if(rq.equals(cid)){
							 isHave = true;
							 CircleData circleData = sqlite.select(CircleDB.db_cid, cid);
							 MyQuanDataList.get(i).put("skip", circleData.getSkip());
							 break;
						 }
					 }
					 if(!isHave){
						 CircleData circleData = sqlite.select(CircleDB.db_cid , rq);
						 if(circleData != null){
							 Map<String,String> newMap = new HashMap<String, String>();
							 newMap.put("cid", rq);
							 newMap.put("name", circleData.getName());
							 newMap.put("skip", circleData.getSkip());
							 newMap.put("info", circleData.getInfo());
							 newMap.put("img", circleData.getImg());
							 newMap.put("customerNum", circleData.getCustomerNum());
							 newMap.put("dayHotNum", circleData.getDayHotNum());
							 MyQuanDataList.add(newMap);
							 myQuanDataSize++;
						 }
					 }
					 if(myQuanDataSize == 6) break;
				 }
			 }
				 
		}
		return MyQuanDataList;
	}
}
