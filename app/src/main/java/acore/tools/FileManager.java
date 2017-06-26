/**
 * @author Jerry
 * 2012-12-30 上午10:17:48
 * Copyright: Copyright (c) xiangha.com 2011
 */
package acore.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import acore.dialogManager.VersionOp;
import acore.override.XHApplication;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilLog;

public class FileManager extends UtilFile{
	public static final String save_cache = "cache";
	public static final int file_up_version = 4;
	public static final String file_IMEI = "IMEI";
	public static final String file_appData = "appNewData" + file_up_version;
	public static final String file_myquan = "myquan";
	public static final String file_XGToken = "token";
	public static final String file_indexData = "indexNewXiangHa_v3";
	public static final String file_welcome = "welcomeData";
	public static final String file_constitution = "constitution.xh";
	public static final String file_buyBurden = "buyBurden.xh";
	public static final String file_searchHis = "searchHis.xh";
	public static final String file_historyCode = "historyCode.xh";
	public static final String file_healthQuestion = "healthQuestion";
	public static final String file_healthResult = "healthResult.xh";
	public static final String file_nousLocalPushData = "nousLocalPushData";
	public static final String file_urlRule = "urlRule";
	public static final String file_allCircle = "allCircle";
	//我的页面，内置数据
	public static final String file_myself = "myself";
	public static final String file_indexModuleAndRecCircle = "indexModuleAndRecCircle";
	public static final String file_location = "location";
	public static final String file_ad = "ad";
	public static final String file_andfix = "andFix";
	public static final String file_config = "config";
	//搜索默认页，热搜词
	public static final String file_hotwords = "hotWords";

	public static final String xmlFile_userInfo = "common";
	public static final String xmlFile_wake = "wake2";
	public static final String xmlFile_appInfo = "appInfo2";
	public static final String xmlFile_appUrl = "appUrl";
	public static final String xmlFile_localPushTag = "localPushTag";
	public static final String xmlFile_adIsShow = "adIsShow";
	public static final String xmlKey_XGToken = "token";
	public static final String xmlKey_showNum = "showNum";
	public static final String xmlKey_protocol = "protocol";
	public static final String xmlKey_domain = "domain";
	public static final String xmlKey_growingioopen = "growingioopen";
	public static final String xmlKey_mall_domain = "mall_domain";
	public static final String xmlKey_isIndexData = "isIndexData";
	public static final String xmlKey_device = "device";
	public static final String xmlKey_device_statictis = "device_statictis";
	public static final String xmlKey_firstStart_v2 = "firstStart_v2";
	public static final String xmlKey_downDishLimit = "downDishLimit";
	public static final String xmlKey_upFavorTime = "upFavorTime";
	public static final String xmlKey_crashTime = "crashTime";
	public static final String xmlKey_notifycationId = "notifycationId";
	public static final String xmlKey_startTime = "startTime";
	public static final String xmlKey_localZhishi = "localZhishi";
	public static final String xmlKey_localCaidan = "localCaidan";
	public static final String xmlKey_confirnCount = "confirnCount";
	public static final String xmlKey_confirnLastShowTime = "confirnLastShowTime";
	public static final String xmlKey_locationIsShow = "locationIsShow";
	public static final String xmlKey_uploadDishSetingIsShow = "uploadDishSetingIsShow";
	public static final String xmlKey_appKillTime = "appKillTime";

	public static final String xmlFile_shareShowPop = "ShareShowPop"; //发菜谱时弹分享弹框，今天提升了几次
	public static final String xmlKey_shareShowPopDataUpDish = "shareShowPopDataUpDish"; //发菜谱时弹分享弹框是哪天
	public static final String xmlKey_shareShowPopNumUpDish = "shareShowPopNumUpDish"; //发菜谱时弹分享弹框，提示了几次
	public static final String xmlKey_shareShowPopDataFavDish = "shareShowPopDataFavDish";//发菜谱时弹分享弹框是哪天
	public static final String xmlKey_shareShowPopNumFavDish = "shareShowPopNumFavDish";//发菜谱时弹分享弹框，提升了几次
	
	public static final String msgInform = "msgInform";
	public static final String newMSG = "newMSG";
	public static final String quan = "quan";
	public static final String zhishi = "zhishi";
	public static final String zan = "zan";
	public static final String menu = "menu";
	public static final String caipu = "caipu";
	public static final String informSing = "informSing";
	public static final String informShork = "informShork";
	public static final String USERCHECK="userCheck";//sp记录数据状态
	public static final String MALL_PAYTYPE="mall_paytype";//用户支付方式
	public static final String MALL_ORDERLIST="mall_orderlist";//订单类型方式
	public static final String MALL_URI_STAT="uri_stat";//统计的规则
	public static final String MALL_STAT="mall_stat";//统计的路径
	public static final String MALL_STAT_BUT="mall_stat_but";//统计的按钮路径
	public static final String MALL_ADDRESS="mall_address";//临时地址
	public static final String MALL_SEARCH_HISTORY="mall_search_history";//搜索历史记录
	public static final String CIRCLE_HOME_SHOWDIALOG="circle_home_showDialog";//社区页面展示dialog
	public static final String MAIN_HOME_GUIDANCEPAGE="main_home_guidancepage";//MainHome引导页
	//好评弹框
	public static final String GOODCOMMENT_INFO="goodcomment_info";//好评信息
	public static final String GOODCOMMENT_TIME="goodcomment_time";//点击去好评时时间
	public static final String GOODCOMMENT_SHOW_TIME_NUM="goodcomment_show_time_num";//好评出现次数，当出现过2次，则3个月之内不再显示
	public static final String GOODCOMMENT_SHOW_NUM="goodcomment_show_num";//好评弹框显示次数,当在线开关变化后，此数据清空
	public static final String GOODCOMMENT_SHOW_TIME="goodcomment_show_time";//好评弹框上次显示时间
	public static final String GOODCOMMENT_SHOW_NUM_ALL="goodcomment_show_num_all";//好评弹框显示总次数
	public static final String GOODCOMMENT_TYPE="goodcomment_type";//好评弹框类型
	//推送
	public static final String PUSH_INFO="push_info";//推送文件名
	public static final String PUSH_TIME="push_time";//上次弹推送的时间
	public static final String PUSH_TAG="push_tag";//上次弹推送的时间

	public static final String BREAKPOINT_UPLOAD_DATA="breakpoint_upload_data";//上传列表数据

	public static final  String DISH_VIDEO = "dish_video";
	public static final  String HEAD_VIDEO_MD5 = "head_video_md5";
	public static final  String TAIL_VIDEO_MD5 = "tail_video_md5";
	public static final  String MUSIC_VIDEO_MD5 = "music_video_md5";
	public static final  String FONT_VIDEO_MD5 = "font_video_md5";

	public static final  String MATCH_WORDS = "match_words"; //热搜词库
	public static final  String MATCH_WORDS_CREATE_TIME = "match_words_create_time"; //热搜词库，创建时间
	public static final  String SHOW_NO_WIFI = "show_no_wifi";
	public static final  String STATICTIS_S6 = "statictis_s6";//s6统计数据体
	public static final  String file_homeTopModle = "homeTopModle" + VersionOp.getVerName(XHApplication.in());//首页一级导航的内置数据


	/**
	 * 获取字符串数组
	 */
	public static String[] getSharedPreference(Context mContext, String key) {
		String regularEx = "#";
		String[] str = null;	
		SharedPreferences sp = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
		String values;
		values = sp.getString(key, "");
		str = values.split(regularEx);

		return str;
	}

	/**
	 * 保存字符数组
	 * 
	 * @param mContext
	 * @param key
	 * @param name
	 */
	public static boolean setSharedPreference(Context mContext, String key, String name) {
		String regularEx = "#";
		String str = "";
		boolean state = true;
		SharedPreferences sp = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
		String[] datas = FileManager.getSharedPreference(mContext, FileManager.USERCHECK);
		if (datas.length >= 100) {
			deleteIndexSharedPreference(mContext, FileManager.USERCHECK, 0);
			datas = FileManager.getSharedPreference(mContext, FileManager.USERCHECK);
		}

		if (datas != null && datas.length > 0) {
			for (int i = 0; i < datas.length; i++) {
				if (!datas[i].equals(name)) {
					str += datas[i];
					str += regularEx;
				} else {
					str += name;
					str += regularEx;
					state = false;
				}
			}
			if (state) {
				str += name;
				str += regularEx;
			}
		} else {
			str += name;
			str += regularEx;
		}
		Editor et = sp.edit();
		et.putString(key, str);
		return et.commit();
	}

	// 改变shareprefences中的数据(数组)
	public static void setSharedPreference(Context mContext, String key, String[] datas) {
		String regularEx = "#";
		String str = "";
		SharedPreferences sp = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
		if (datas != null && datas.length > 0) {
			for (String value : datas) {
				str += value;
				str += regularEx;
			}
			Editor et = sp.edit();
			et.putString(key, str);
			et.commit();
		}
	}

	/**
	 * 删除数组中的特定字符串字符
	 * @param mContext
	 * @param key
	 * @param index
	 */
	public static void deleteIndexSharedPreference(Context mContext, String key, int index) {
		String[] datas = FileManager.getSharedPreference(mContext, FileManager.USERCHECK);
		Hashtable<String, String> hash = new Hashtable<String, String>();
		for (int i = 0; i < datas.length; i++) {
			if (i!=index)
				hash.put(datas[i], datas[i]);
		}
		// 生成一个新的数组
		String[] str_new = new String[hash.size()];
		int i = 0;
		Enumeration<String> enumeration = hash.keys();
		while (enumeration.hasMoreElements()) {
			str_new[i] = enumeration.nextElement().toString();
			i++;
		}
		// 改变数组数据
		FileManager.setSharedPreference(mContext, FileManager.USERCHECK, str_new);
	}
	
	public static String getCameraDir(){
		return getSDDir()+save_cache+"/";
	}
	
	 public static File createTmpFile(Context context){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            // 已挂载
            File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_"+timeStamp+"";
            File tmpFile = new File(pic, fileName+".jpg");
            return tmpFile;
        }else{
            File cacheDir = context.getCacheDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_"+timeStamp+"";
            File tmpFile = new File(cacheDir, fileName+".jpg");
            return tmpFile;
        }
    }
	 
	 public static List<String> GetFiles(String Path, String Extension)
		{
			int getFile=10;
			List<String> lstFile =new ArrayList<String>(); //结果 List
			File file = new File(Path);
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				//崩溃文件太多，就删了不上传了
				if(files.length>500){
					FileManager.delDirectoryOrFile(Path);
					return lstFile;
				}
				System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
				try{
					Arrays.sort(files, new Comparator<Object>() {
						@Override
						public int compare(Object object1, Object object2) {
							File file1 = (File) object1;
							File file2 = (File) object2;
							long result = file1.lastModified() - file2.lastModified();
							if (result < 0) {
								return 1;
							} else if (result > 0) {
								return -1;
							} else {
								return 0;
							}
						}
					});
				}
				catch (Exception e){
					UtilLog.reportError("文件排序错误", e);
				}
				//得到最新的文件
				for (int i = 0; i<files.length&&i < getFile; i++) {
					File f = files[i];
					if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension)) //判断扩展名
	    				lstFile.add(f.getPath());
				}
				//删除老的文件
				for (int i = getFile; i<files.length; i++) {
					files[i].delete();
				}
			}
			return lstFile;
		}

	/**
	 * 复制单个文件
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @param isOverrite ：是否覆盖
	 */
	public static void copyFile(String oldPath, String newPath,boolean isOverrite) {
		File file = new File(newPath);
		//如果不覆盖，并且此文件又存在目标路径下，则无需重复拷贝
		if(!isOverrite && file.exists()) return;
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}
		}
		catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}
}
