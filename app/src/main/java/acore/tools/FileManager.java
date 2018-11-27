package acore.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import acore.logic.VersionOp;
import acore.override.XHApplication;
import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilLog;

public class FileManager extends UtilFile{
	public static final String save_cache = "cache";
	public static final String save_long = "long";
	public static final int file_up_version = 4;
	public static final String file_IMEI = "IMEI";
	public static final String file_appData = "appNewData" + file_up_version;
	public static final String file_myquan = "myquan";
	public static final String file_XGToken = "token";
	public static final String file_constitution = "constitution.xh";
	public static final String file_buyBurden = "buyBurden.xh";
	public static final String file_searchHis = "searchHis.xh";
	public static final String file_historyCode = "historyCode.xh";
	public static final String file_healthQuestion = "healthQuestion";
	public static final String file_healthResult = "healthResult.xh";
	public static final String file_urlRule = "urlRule";
	public static final String file_allCircle = "allCircle";
	//我的页面，内置数据
	public static final String file_indexModuleAndRecCircle = "indexModuleAndRecCircle";
	public static final String file_location = "location";
	public static final String file_config = "config";
	//随机推广
	public static final String file_randPromotionConfig = "randPromotionConfig";
	public static final String xmlFile_userInfo = "common";
	public static final String xmlFile_appInfo = "appInfo2";
	public static final String xmlFile_task = "task";
	public static final String xmlFile_appUrl = "appUrl";
	public static final String xmlFile_localPushTag = "localPushTag";
	public static final String xmlFile_adIsShow = "adIsShow";
	public static final String xmlKey_XGToken = "token";
	public static final String xmlKey_protocol = "protocol";
	public static final String xmlKey_domain = "domain";
	public static final String xmlKey_growingioopen = "growingioopen";
	public static final String xmlKey_mall_domain = "mall_domain";
	public static final String xmlKey_request_tip = "requesttip";
	public static final String xmlKey_device = "device";
	public static final String xmlKey_downDishLimit = "downDishLimit";
	public static final String xmlKey_upFavorTime = "upFavorTime";
	public static final String xmlKey_startTime = "startTime";
	public static final String xmlKey_localZhishi = "localZhishi";
	public static final String xmlKey_localIntentRequestCode = "localIntentRequestCode";
	public static final String xmlKey_locationIsShow = "locationIsShow";
	public static final String xmlKey_uploadDishSetingIsShow = "uploadDishSetingIsShow";
	public static final String xmlKey_appKillTime = "appKillTime";
	public static final String xmlKey_homeGuidanceShow = "homeGuidanceShow";

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

	public static final String BREAKPOINT_UPLOAD_DATA="breakpoint_upload_data";//上传列表数据

	public static final  String DISH_VIDEO = "dish_video";
	public static final  String HEAD_VIDEO_MD5 = "head_video_md5";
	public static final  String TAIL_VIDEO_MD5 = "tail_video_md5";
	public static final  String MUSIC_VIDEO_MD5 = "music_video_md5";
	public static final  String FONT_VIDEO_MD5 = "font_video_md5";

	public static final  String MATCH_WORDS = "match_words"; //热搜词库
	public static final  String MATCH_WORDS_CREATE_TIME = "match_words_create_time"; //热搜词库，创建时间
	public static final  String SHOW_NO_WIFI = "show_no_wifi";
	public static final  String file_homeTopModle = "homeTopModle" + VersionOp.getVerName(XHApplication.in());//首页一级导航的内置数据
	public static final String xmlKey_ds_from_show = "ds_from_show";
	public static final String dish_caipu_hint = "dish_caipu_hint";
	public static final String app_welcome = "app_welcome";
	public static final String app_notification = "app_notification";
	public static final String push_setting_state = "push_setting_state";
	public static final String push_setting_message = "push_setting_message";
	public static final String xg_config = "xg_config";
	public static final String xg_config_official = "xg_config_official";
	public static final String notification_permission = "notification_permission";
	public static final String xhmKey_shortVideoGuidanceShow = "shortVideoGuidanceShow";
	public static final String key_header_mode = "header_mode";
	public static final String video_corp_show_hint = "videoCorp_show_hint";
	public static final String xmlKey_device_statistics = "deviceStatistics";

	private FileManager() {
		throw new UnsupportedOperationException("u can't instantiate me...");
	}

	/**
	 * 删除SD卡上时间较早的文件
	 * @param completePath 完整路径
	 * @param keep 文件夹内只保留
	 *            (keep~keep*2)个文件
	 */
	public static void delDirectoryOrFile(String completePath, int keep) {
		if(isSpace(completePath)){
			return ;
		}
		File file = new File(completePath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length - keep * 2 > 0) {
				if (keep > 0) {
					System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
					try {
						Arrays.sort(files, new Comparator<Object>() {
							@Override
							public int compare(Object object1, Object object2) {
								File file1 = (File) object1;
								File file2 = (File) object2;
								long result = file1.lastModified() - file2.lastModified();
								if (result < 0) {
									return -1;
								} else if (result > 0) {
									return 1;
								} else {
									return 0;
								}
							}
						});
					} catch (Exception e) {
						UtilLog.reportError("文件排序错误", e);
					}
				}
				for (int i = 0; i < files.length - keep; i++) {
					files[i].delete();
				}
			}
		} else if (file.isFile()) {
			file.delete();
		}
	}

	public static String getSDLongDir(){
		return getSDDir()+save_long+"/";
	}
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
	
	public static String getSDCacheDir(){
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

	/**
	 * 保存图片到sd卡
	 * @param bitmap
	 * @param completePath : 完整路径
	 * @param format
	 */
	public static void saveImgToCompletePath(Bitmap bitmap, String completePath, Bitmap.CompressFormat format) {
		if(isSpace(completePath)){
			return;
		}
		File file = new File(completePath);
		try {
			File parentFile = file.getParentFile();
			if(parentFile == null){
				throw new NullPointerException("path = " + completePath);
			}
			if (!parentFile.exists())
				parentFile.mkdirs();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(format, 100, bos);
		} catch (Exception e) {
			byte[] theByte = UtilImage.bitmapToByte(bitmap, format, 0);
			if (theByte != null) {
				saveFileToCompletePath(getSDDir() + file.getAbsolutePath(), new ByteArrayInputStream(theByte),
						false);
			}
			e.printStackTrace();
		}
	}
	 
    /**
	 * 异步保存文件，子线程中
	 * @param filePath 文件路径
	 * @param content 内容
	 * @param append 是否可以追加
	 */
	public static void asyncSaveFile(final String filePath, final String content, final boolean append){
		new Thread(() -> saveFileToCompletePath(filePath,content,append)).start();
	}

	/**
	 * 异步保存文件，子线程中
	 * @param filePath 文件路径
	 * @param content 内容
	 * @param append 是否可以追加
	 */
	public static void asyncSaveFile(final String filePath, final InputStream content, final boolean append){
		new Thread(() -> saveFileToCompletePath(filePath,content,append)).start();
	}

	public static void saveSharePreference(Context context, String xmlName, @NonNull String key, @NonNull String value) {
		if(context == null || TextUtils.isEmpty(xmlName)) return;
		SharedPreferences preferences = context.getSharedPreferences(xmlName, 0);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
	public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
	public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
	public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值

	public static long getFileOrFolderSize(String filePath){
		if(TextUtils.isEmpty(filePath)) return 0;
		File file = new File(filePath);
		long size = 0;
		try {
			if(file.isDirectory()){
				size = getFolderSize(file);
			}else{
				size += getFileSize(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	public static String getFileOrFolderSize(String filePath, int sizeType){
		if(TextUtils.isEmpty(filePath)) return "";
		File file = new File(filePath);
		long size = 0;
		try {
			if(file.isDirectory()){
				size = getFolderSize(file);
			}else{
				size += getFileSize(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return formatFileSize(size,sizeType);
	}

	public static String getAutoFileOrFilesSize(String filePath){
		return getFileOrFolderSize(filePath,0);
	}

	public static long getFileSize2(String filePath) throws IOException {

		if(TextUtils.isEmpty(filePath)){
			return 0;
		}
		File file = new File(filePath);
		return getFileSize(file);
	}

	public static long getFileSize(File file) throws IOException {
		long size = 0;
		if(file != null && file.exists()){
			FileInputStream fis = new FileInputStream(file);
			size = fis.available();
			fis.close();
		}
		return size;
	}

	public static long getFolderSize(File file) throws IOException {
		long size = 0;
		if(file != null){
			if(file.exists() && file.isDirectory()){
				File[] fileList = file.listFiles();
				if (fileList == null)
					return size;
				for (File childFile:fileList){
					if(childFile.isDirectory()){
						size += getFolderSize(childFile);
					}else{
						size += getFileSize(childFile);
					}
				}
			}
		}
		return size;
	}

	/**
	 * 转换文件大小,指定转换的类型
	 *
	 * @param fileS
	 * @param sizeType
	 * @return
	 */
	public static String formatFileSize(long fileS, int sizeType) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "0B";
		switch (sizeType) {
			case SIZETYPE_B:
				fileSizeString = Double.valueOf(df.format((double) fileS)) + "B";
				break;
			case SIZETYPE_KB:
				fileSizeString = Double.valueOf(df.format((double) fileS / 1024)) + "KB";
				break;
			case SIZETYPE_MB:
				fileSizeString = Double.valueOf(df.format((double) fileS / 1048576)) + "MB";
				break;
			case SIZETYPE_GB:
				fileSizeString = Double.valueOf(df.format((double) fileS / 1073741824)) + "GB";
				break;
			default:
				fileSizeString = formatFileSize(fileS);
				break;
		}
		return fileSizeString;
	}

	/**
	 * 转换文件大小
	 *
	 * @param fileS
	 * @return
	 */
	public static String formatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (fileS == 0) {
			return wrongSize;
		}
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}

}
