package com.popdialog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;

/**
 * PackageName : com.popdialog.util
 * Created by MrTrying on 2017/9/19 16:17.
 * E_mail : ztanzeyu@gmail.com
 */

public class FileManager {

    public static final String file_welcome = "welcomeData";
    //好评弹框
    public static final String GOODCOMMENT_INFO = "goodcomment_info";//好评信息
    public static final String GOODCOMMENT_TIME = "goodcomment_time";//点击去好评时时间
    public static final String GOODCOMMENT_SHOW_TIME_NUM = "goodcomment_show_time_num";//好评出现次数，当出现过2次，则3个月之内不再显示
    public static final String GOODCOMMENT_SHOW_NUM = "goodcomment_show_num";//好评弹框显示次数,当在线开关变化后，此数据清空
    public static final String GOODCOMMENT_SHOW_TIME = "goodcomment_show_time";//好评弹框上次显示时间
    public static final String GOODCOMMENT_SHOW_NUM_ALL = "goodcomment_show_num_all";//好评弹框显示总次数
    public static final String GOODCOMMENT_TYPE = "goodcomment_type";//好评弹框类型
    //推送
    public static final String PUSH_INFO = "push_info";//推送文件名
    public static final String PUSH_TIME = "push_time";//上次弹推送的时间
    public static final String PUSH_TAG = "push_tag";//上次弹推送的时间

    public static final String xmlFile_appUrl = "appUrl";
    public static final String xmlFile_popdialog = "popdialog";

    public static final String xmlKey_confirmCount = "confirmCount";
    public static final String xmlKey_confirmLastShowTime = "confirmLastShowTime";
    public static final String xmlKey_fullSrceenShowNum = "fullSrceenShowNum ";

    public static void saveShared(Context context, String filename, String key, String value) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(filename, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public static void delShared(Context context, String filename, String key) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(filename, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (key.equals("")) {
                editor.clear();
            } else {
                editor.remove(key);
            }

            editor.commit();
        }
    }

    public static Object loadShared(Context conte, String filename, String key) {
        if (conte != null && filename != null) {
            SharedPreferences sharedPreferences = conte.getSharedPreferences(filename, 0);
            return key.length() == 0 ? sharedPreferences.getAll() : sharedPreferences.getString(key, "");
        } else {
            return "";
        }
    }

    /**
     * 在SD卡上存文件
     *
     * @param completePath : 完整路径
     * @param str
     * @param append       是否在文件后面增加字符
     *
     * @return 成功file，失败null
     */
    public static File saveFileToCompletePath(String completePath, String str, boolean append) {
        File file = new File(completePath);
        FileOutputStream fileOutputStream = null;
        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            fileOutputStream = new FileOutputStream(file, append);
            fileOutputStream.write(str.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    /**
     * 在SD卡上存文件
     *
     * @param completePath : 完整路径
     * @param is
     *
     * @return 成功file，失败null
     */
    public static File saveFileToCompletePath(String completePath, InputStream is, boolean append) {
        File file = new File(completePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists())
            parentFile.mkdirs();
        if (is != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, append);
                byte[] b = new byte[1024];
                int len = -1;
                while ((len = is.read(b)) != -1) {
                    fos.write(b, 0, len);
                }
                fos.close();
                is.close();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                // LogManager.reportError("写sd文件异常",e);
                return null;
            }
        } else
            return null;
    }

    /**
     * 读取文件
     *
     * @param completePath ：文件的完整的路径
     *
     * @return
     */
    public static String readFile(String completePath) {
        FileInputStream fi;
        StringBuilder str = new StringBuilder();
        try {
            fi = new FileInputStream(completePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fi));
            String readString = "";
            while ((readString = br.readLine()) != null) {
                str.append(readString + "\r\n");
            }
            br.close();
            fi.close();
        } catch (Exception e) {
            return "";
        }
        return str.toString();
    }

    /**
     * 删除SD卡上的文件夹《包括文件夹下的文件》或者文件
     *
     * @param path
     */
    public static void delDirectoryOrFile(String completePath) {
        delDirectoryOrFile(completePath, 0);
    }

    /**
     * 删除SD卡上时间较早的文件
     *
     * @param completePath
     * @param 文件夹内只保留      (keep~keep*2)个文件
     */
    public static void delDirectoryOrFile(String completePath, int keep) {
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
                        Log.w("tzy", "文件排序错误");
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

    /** 获取SD卡路径 */
    public static String getSDDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/xiangha/";
    }

    /** 获取App data路径 */
    public static String getDataDir(Context context) {
        return Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/file/";
    }
}
