package acore.tools;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.xianghatest.LoadResActivity;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static android.content.Context.MODE_MULTI_PROCESS;

/**
 * Created by xiangha on 2016/8/23.
 */

public class MultiDexTools {

    private static Context mCon;

    public static final String KEY_DEX2_SHA1 = "dex2-SHA1-Digest";

    public static void initMultiDexf(Context con){
        mCon = con;
        if (!quickStart() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//>=5.0的系统默认对dex进行oat优化
            if (needWait(mCon)){
                waitForDexopt(mCon);
            }
            MultiDex.install (mCon );
        } else {
            return;
        }
    }

    private static boolean quickStart() {
        if (TextUtils.indexOf(getCurProcessName(mCon), ":mini") > -1) {
            return true;
        }
        return false ;
    }
    //neead wait for dexopt ?
    private static boolean needWait(Context context){
        String flag = get2thDexSHA1(context);
        SharedPreferences sp = context.getSharedPreferences(getPackageInfo(context).versionName,Context.MODE_MULTI_PROCESS);
        String saveValue = sp.getString(KEY_DEX2_SHA1, "");
        return !TextUtils.equals(flag,saveValue);
    }
    /**
     * Get classes.dex file signature
     * @param context
     * @return
     */
    private static String get2thDexSHA1(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        String source = ai.sourceDir;
        try {
            JarFile jar = new JarFile(source);
            Manifest mf = jar.getManifest();
            Map<String, Attributes> map = mf.getEntries();
            Attributes a = map.get("classes2.dex");
            return a.getValue("SHA1-Digest");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }
    // optDex finish
    public static void installFinish(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                getPackageInfo(context).versionName, MODE_MULTI_PROCESS);
        sp.edit().putString(KEY_DEX2_SHA1,get2thDexSHA1(context)).commit();
    }


    private static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context
                    .getSystemService(Context. ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess. processName;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null ;
    }
    private static void waitForDexopt(Context base) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName( "com.xianghatest", LoadResActivity.class.getName());
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
//        Intent it = new Intent(base, LoadResActivity.class);
//        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        base.startActivity(it);

        long startWait = System.currentTimeMillis ();
        long waitTime = 10 * 1000 ;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1 ) {
            waitTime = 20 * 1000 ;//实测发现某些场景下有些2.3版本有可能10s都不能完成optdex
        }
        while (needWait(base)) {
            try {
                long nowWait = System.currentTimeMillis() - startWait;
                if (nowWait >= waitTime) {
                    return;
                }
                Thread.sleep(200 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static PackageInfo getPackageInfo(Context context){
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("FRJ",e.getLocalizedMessage());
        }
        return  new PackageInfo();
    }

}
