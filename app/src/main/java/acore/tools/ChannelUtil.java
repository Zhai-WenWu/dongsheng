package acore.tools;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import com.meituan.android.walle.WalleChannelReader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by XiangHa on 2016/8/25.
 */

public class ChannelUtil {
    private static final String CHANNEL_KEY = "cztchannel";
    public static String appChannel = "";
    /**
     * 返回市场。  如果获取失败返回""
     * @param context
     * @return
     */
    public static String getChannel(Context context){
        if (TextUtils.isEmpty(appChannel)) {
            if(context == null) return "xiangha_sp";
            try {
                //通过修改manifest中的umeng变量进行渠道修改，1、manifest文件放开UMENG_CHANNEL配置   2、注释XHApplication动态设置渠道名方式
//                ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
//				appChannel = appInfo.metaData.getString("UMENG_CHANNEL");
                //通过美团打包读取文件名进行渠道修改
//                appChannel = ChannelUtil.getChannel(context);
                //通过walle方式获取渠道
                appChannel = WalleChannelReader.getChannel(context.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       if(TextUtils.isEmpty(appChannel))return "xiangha_sp";
        return appChannel;
    }
    /**
     * 返回市场。  如果获取失败返回defaultChannel
     * @param context
     * @param defaultChannel
     * @return
     */
    public static String getChannel(Context context, String defaultChannel) {
        //从apk中获取
        String mChannel = getChannelFromApk(context, CHANNEL_KEY);
        if(!TextUtils.isEmpty(mChannel)){
            return mChannel;
        }
        //全部获取失败
        return defaultChannel;
    }
    /**
     * 从apk中获取版本信息
     * @param context
     * @param channelKey
     * @return
     */
    private static String getChannelFromApk(Context context, String channelKey) {
        //从apk包中获取
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelKey;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split("_");
        String channel = "";
        if (split != null && split.length >= 2) {
            channel = ret.substring(split[0].length() + 1);
        }
        return channel;
    }
}
