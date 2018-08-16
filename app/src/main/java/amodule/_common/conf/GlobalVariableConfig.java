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
        clearAttentionModules();
        clearFavoriteModules();
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
            GlobalAttentionModule add = new GlobalAttentionModule();
            add.setAttention(attentionModule.isAttention());
            add.setAttentionNum(attentionModule.getAttentionNum());
            add.setAttentionUserCode(attentionModule.getAttentionUserCode());
            mGlobalAttentionModules.add(add);
        }
    }

    private static void updateAttentionModule(GlobalAttentionModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalAttentionModule targetModule = containsAttentionModule(sourceModule.getAttentionUserCode());
        if (targetModule == null)
            return;
        targetModule.setAttention(sourceModule.isAttention());
        if (!TextUtils.isEmpty(sourceModule.getAttentionNum())) {
            targetModule.setAttentionNum(sourceModule.getAttentionNum());
        }
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

    public static GlobalFavoriteModule containsFavoriteModule(String favCode, FavoriteTypeEnum favoriteType) {
        if (TextUtils.isEmpty(favCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalFavoriteModule ret = null;
            Iterator<GlobalFavoriteModule> iterator = mGlobalFavoriteModules.iterator();
            while (iterator.hasNext()) {
                GlobalFavoriteModule favoriteModule = iterator.next();
                if (TextUtils.equals(favCode, favoriteModule.getFavCode()) && favoriteType == favoriteModule.getFavType()) {
                    ret = favoriteModule;
                    break;
                }
            }
            return  ret;
        }
    }

    public static void addFavoriteModule(GlobalFavoriteModule favModule) {
        if (favModule == null)
            return;
        GlobalFavoriteModule module = containsFavoriteModule(favModule.getFavCode(), favModule.getFavType());
        if (module == null) {
            GlobalFavoriteModule add = new GlobalFavoriteModule();
            add.setFav(favModule.isFav());
            add.setFavNum(favModule.getFavNum());
            add.setFavCode(favModule.getFavCode());
            add.setFavType(favModule.getFavType());
            mGlobalFavoriteModules.add(add);
        }
    }

    private static void updateFavoriteModule(GlobalFavoriteModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalFavoriteModule targetModule = containsFavoriteModule(sourceModule.getFavCode(), sourceModule.getFavType());
        if (targetModule == null)
            return;
        targetModule.setFav(sourceModule.isFav());
        if (!TextUtils.isEmpty(sourceModule.getFavNum())) {
            targetModule.setFavNum(sourceModule.getFavNum());
        }
        targetModule.setFavCode(sourceModule.getFavCode());
    }

    public static GlobalFavoriteModule deleteFavoriteModuleByCode(String code, FavoriteTypeEnum favoriteType) {
        if (TextUtils.isEmpty(code))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalFavoriteModule retModule = null;
            Iterator<GlobalFavoriteModule> iterator = mGlobalFavoriteModules.iterator();
            while (iterator.hasNext()) {
                GlobalFavoriteModule iteratorModule = iterator.next();
                if (TextUtils.equals(code, iteratorModule.getFavCode()) && favoriteType == iteratorModule.getFavType()) {
                    iterator.remove();
                    retModule = iteratorModule;
                    break;
                }
            }
            return retModule;
        }
    }

    public static void handleFavoriteModule(GlobalFavoriteModule module) {
        if (module == null)
            return;
        GlobalFavoriteModule contains = containsFavoriteModule(module.getFavCode(), module.getFavType());
        if (contains == null) {
            addFavoriteModule(module);
        } else {
            updateFavoriteModule(module);
        }
    }

    public static void handleAttentionModule(GlobalAttentionModule module) {
        if (module == null)
            return;
        GlobalAttentionModule contains = containsAttentionModule(module.getAttentionUserCode());
        if (contains == null) {
            addAttentionModule(module);
        } else {
            updateAttentionModule(module);
        }
    }

    public static void clearAttentionModules() {
        mGlobalAttentionModules.clear();
    }

    public static void clearFavoriteModules() {
        mGlobalFavoriteModules.clear();
    }
}
