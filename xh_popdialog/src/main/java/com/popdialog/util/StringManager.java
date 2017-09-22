package com.popdialog.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/19 16:41.
 * E_mail : ztanzeyu@gmail.com
 */

public class StringManager {

    /**
     * 获取md5值
     *
     * @param str  加密字符串
     * @param if32 是否要32位
     *
     * @return
     */
    public static String toMD5(String str, boolean if32) {
        try {
            if (str.length() == 0)
                str = Math.random() + "";
            StringBuffer sb = new StringBuffer(if32 ? 32 : 16);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] array = md5.digest(str.getBytes("utf-8"));

            for (int i = 0; i < array.length; i++) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, if32 ? 3 : 2));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.w("tzy", "md5错误");
            return "";
        }
    }

    public static Map<String, String> getFirstMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        ArrayList<Map<String, String>> returnList = StringManager.getListMapByJson(obj);
        if (returnList.size() > 0) {
            map = returnList.get(0);
        }
        return map;
    }

    /**
     * 从json中获取MAP数组
     *
     * @param json
     *
     * @return
     */
    public static ArrayList<Map<String, String>> getListMapByJson(Object json) {
        ArrayList<Map<String, String>> objs = new ArrayList<Map<String, String>>();
        JSONArray array = new JSONArray();
        // 尝试解析
        try {
            if (json == null)
                return objs;
            else if (json.getClass() == JSONArray.class)
                array = (JSONArray) json;
            else if (((String) json).length() == 0)
                return objs;
            else
                array = new JSONArray((String) json);
        } catch (JSONException e1) {
            try {
                array.put(new JSONObject((String) json));
            } catch (JSONException e2) {
                Log.w("xh_default", "Json无法解析:" + json);
            }
        }
        for (int i = 0; i < array.length(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            try {
                Iterator<?> it = array.getJSONObject(i).keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    Object xx = array.getJSONObject(i).get(key);
                    map.put(key, xx.toString());
                }
            } catch (Exception e) {
                // 直接取数组值到map中,key为空
                try {
                    map.put("", array.get(i).toString());
                } catch (JSONException e1) {
                    Log.w("xh_default", "Json无法解析:" + array.toString());
                }
            }
            objs.add(map);
        }
        return objs;
    }
}
