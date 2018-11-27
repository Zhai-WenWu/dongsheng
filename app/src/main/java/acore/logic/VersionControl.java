package acore.logic;

import android.content.Context;

import acore.tools.FileManager;

/**
 * Description :
 * PackageName : acore.logic
 * Created by mrtrying on 2018/10/24 15:14.
 * e_mail : ztanzeyu@gmail.com
 */
public class VersionControl {

    public static boolean isCurrentVersionOnce(Context context,String key){
        return !"2".equals(FileManager.loadShared(context,VersionOp.getVerName(context),key));
    }

    public static void recordCurrentVersionOnce(Context context,String key){
        FileManager.saveShared(context,VersionOp.getVerName(context),key,"2");
    }
}
