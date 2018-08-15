package amodule._common.conf;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class GlobalVariableConfig {
    public static boolean shortVideoDetail_netStateTip_dialogEnable = true;//是否可以提示网络状态
    private static ArrayList<GlobalAttentionModule> mGlobalAttentionModules = new ArrayList<>();
    private static ArrayList<GlobalFavoriteModule> mGlobalFavoriteModules = new ArrayList<>();

    public static void restoreConf() {
        shortVideoDetail_netStateTip_dialogEnable = true;
        mGlobalAttentionModules.clear();
        mGlobalFavoriteModules.clear();
    }

    public static GlobalAttentionModule containsAttentionModule(String attentionUserCode) {
        if (TextUtils.isEmpty(attentionUserCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalAttentionModule ret = null;
            Iterator<GlobalAttentionModule> iterator = mGlobalAttentionModules.iterator();
            while (iterator.hasNext()) {
                GlobalAttentionModule attentionModule = iterator.next();
                if (TextUtils.equals(attentionUserCode, attentionModule.getAttentionUserCode())) {
                    ret = attentionModule;
                    break;
                }
            }
            return  ret;
        }
    }

    public static void addAttentionModule(GlobalAttentionModule attentionModule) {
        if (attentionModule == null)
            return;
        GlobalAttentionModule module = containsAttentionModule(attentionModule.getAttentionUserCode());
        if (module == null) {
            mGlobalAttentionModules.add(attentionModule);
        }
    }

    private static void updateAttentionModule(GlobalAttentionModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalAttentionModule targetModule = containsAttentionModule(sourceModule.getAttentionUserCode());
        if (targetModule == null)
            return;
        targetModule.setAttention(sourceModule.isAttention());
        targetModule.setAttentionNum(sourceModule.getAttentionNum());
        targetModule.setAttentionUserCode(sourceModule.getAttentionUserCode());
    }

    public static GlobalAttentionModule deleteAttentionModuleByUserCode(String userCode) {
        if (TextUtils.isEmpty(userCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalAttentionModule retModule = null;
            Iterator<GlobalAttentionModule> iterator = mGlobalAttentionModules.iterator();
            while (iterator.hasNext()) {
                GlobalAttentionModule iteratorModule = iterator.next();
                if (TextUtils.equals(userCode, iteratorModule.getAttentionUserCode())) {
                    iterator.remove();
                    retModule = iteratorModule;
                    break;
                }
            }
            return retModule;
        }
    }

    public static GlobalFavoriteModule containsFavoriteModule(String favCode) {
        if (TextUtils.isEmpty(favCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalFavoriteModule ret = null;
            Iterator<GlobalFavoriteModule> iterator = mGlobalFavoriteModules.iterator();
            while (iterator.hasNext()) {
                GlobalFavoriteModule attentionModule = iterator.next();
                if (TextUtils.equals(favCode, attentionModule.getFavCode())) {
                    ret = attentionModule;
                    break;
                }
            }
            return  ret;
        }
    }

    public static void addFavoriteModule(GlobalFavoriteModule favModule) {
        if (favModule == null)
            return;
        GlobalFavoriteModule module = containsFavoriteModule(favModule.getFavCode());
        if (module == null) {
            mGlobalFavoriteModules.add(favModule);
        }
    }

    private static void updateFavoriteModule(GlobalFavoriteModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalFavoriteModule targetModule = containsFavoriteModule(sourceModule.getFavCode());
        if (targetModule == null)
            return;
        targetModule.setFav(sourceModule.isFav());
        targetModule.setFavNum(sourceModule.getFavNum());
        targetModule.setFavCode(sourceModule.getFavCode());
    }

    public static GlobalFavoriteModule deleteFavoriteModuleByCode(String code) {
        if (TextUtils.isEmpty(code))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalFavoriteModule retModule = null;
            Iterator<GlobalFavoriteModule> iterator = mGlobalFavoriteModules.iterator();
            while (iterator.hasNext()) {
                GlobalFavoriteModule iteratorModule = iterator.next();
                if (TextUtils.equals(code, iteratorModule.getFavCode())) {
                    iterator.remove();
                    retModule = iteratorModule;
                    break;
                }
            }
            return retModule;
        }
    }
}
