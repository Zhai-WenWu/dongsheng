package third.mall.tool;

import java.util.Enumeration;
import java.util.Hashtable;

import acore.tools.FileManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ToolFile {

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
	 * @param values
	 */
	public static void setSharedPreference(Context mContext, String key, String name) {
		String regularEx = "#";
		String str = "";
		boolean state = true;
		SharedPreferences sp = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
		String[] datas = FileManager.getSharedPreference(mContext, key);
		if (datas.length >= 50) {
			deleteIndexSharedPreference(mContext, key, 0);
			datas = FileManager.getSharedPreference(mContext, key);
		}

		if (datas != null && datas.length > 0) {
			for (int i = 0; i < datas.length; i++) {
				if (!datas[i].equals(name)) {
					str += datas[i];
					str += regularEx;
				} 
			}
			str += name;
			str += regularEx;
		} else {
			str += name;
			str += regularEx;
		}
		Editor et = sp.edit();
		et.putString(key, str);
		et.commit();
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
	 * @param name
	 */
	public static void deleteIndexSharedPreference(Context mContext, String key, int index) {
		String[] datas = FileManager.getSharedPreference(mContext, key);
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
		FileManager.setSharedPreference(mContext, key, str_new);
	}
	
}
