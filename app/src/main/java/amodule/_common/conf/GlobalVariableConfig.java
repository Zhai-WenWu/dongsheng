package amodule._common.conf;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class GlobalVariableConfig {
    public static boolean shortVideoDetail_netStateTip_dialogEnable = true;//是否可以提示网络状态
    private static ArrayList<GlobalAttentionModule> mGlobalAttentionModules = new ArrayList<>();//关注
    private static ArrayList<GlobalFavoriteModule> mGlobalFavoriteModules = new ArrayList<>();//收藏
    private static ArrayList<GlobalGoodModule> mGlobalGoodModules = new ArrayList<>();//点赞
    private static ArrayList<GlobalCommentModule> mGlobalCommentModules = new ArrayList<>();//评论

    public static void restoreConf() {
        shortVideoDetail_netStateTip_dialogEnable = true;
        clearAttentionModules();
        clearFavoriteModules();
        clearGoodModules();
        clearCommentModules();
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

    public static void addGoodModule(GlobalGoodModule goodModule) {
        if (goodModule == null)
            return;
        GlobalAttentionModule module = containsAttentionModule(goodModule.getGoodCode());
        if (module == null) {
            GlobalGoodModule add = new GlobalGoodModule();
            add.setGood(goodModule.isGood());
            add.setGoodNum(goodModule.getGoodNum());
            add.setGoodCode(goodModule.getGoodCode());
            mGlobalGoodModules.add(add);
        }
    }

    public static GlobalGoodModule containsGoodModule(String goodCode) {
        if (TextUtils.isEmpty(goodCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalGoodModule ret = null;
            Iterator<GlobalGoodModule> iterator = mGlobalGoodModules.iterator();
            while (iterator.hasNext()) {
                GlobalGoodModule goodModule = iterator.next();
                if (TextUtils.equals(goodCode, goodModule.getGoodCode())) {
                    ret = goodModule;
                    break;
                }
            }
            return  ret;
        }
    }

    private static void updateGoodModule(GlobalGoodModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalGoodModule targetModule = containsGoodModule(sourceModule.getGoodCode());
        if (targetModule == null)
            return;
        targetModule.setGood(sourceModule.isGood());
        if (!TextUtils.isEmpty(sourceModule.getGoodNum())) {
            targetModule.setGoodNum(sourceModule.getGoodNum());
        }
        targetModule.setGoodCode(sourceModule.getGoodCode());
    }

    public static GlobalGoodModule deleteGoodModuleByUserCode(String goodCode) {
        if (TextUtils.isEmpty(goodCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalGoodModule retModule = null;
            Iterator<GlobalGoodModule> iterator = mGlobalGoodModules.iterator();
            while (iterator.hasNext()) {
                GlobalGoodModule iteratorModule = iterator.next();
                if (TextUtils.equals(goodCode, iteratorModule.getGoodCode())) {
                    iterator.remove();
                    retModule = iteratorModule;
                    break;
                }
            }
            return retModule;
        }
    }

    public static void handleGoodModule(GlobalGoodModule module) {
        if (module == null)
            return;
        GlobalGoodModule contains = containsGoodModule(module.getGoodCode());
        if (contains == null) {
            addGoodModule(module);
        } else {
            updateGoodModule(module);
        }
    }

    public static void addCommentModule(GlobalCommentModule commentModule) {
        if (commentModule == null)
            return;
        GlobalCommentModule module = containsCommentModule(commentModule.getFlagCode());
        if (module == null) {
            GlobalCommentModule add = new GlobalCommentModule();
            add.setCommentNum(commentModule.getCommentNum());
            add.setFlagCode(commentModule.getFlagCode());
            mGlobalCommentModules.add(add);
        }
    }

    public static GlobalCommentModule containsCommentModule(String flagCode) {
        if (TextUtils.isEmpty(flagCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalCommentModule ret = null;
            Iterator<GlobalCommentModule> iterator = mGlobalCommentModules.iterator();
            while (iterator.hasNext()) {
                GlobalCommentModule commentModule = iterator.next();
                if (TextUtils.equals(flagCode, commentModule.getFlagCode())) {
                    ret = commentModule;
                    break;
                }
            }
            return  ret;
        }
    }

    private static void updateCommentModule(GlobalCommentModule sourceModule) {
        if (sourceModule == null)
            return;
        GlobalCommentModule targetModule = containsCommentModule(sourceModule.getFlagCode());
        if (targetModule == null)
            return;
        if (!TextUtils.isEmpty(sourceModule.getCommentNum())) {
            targetModule.setCommentNum(sourceModule.getCommentNum());
        }
        targetModule.setFlagCode(sourceModule.getFlagCode());
    }

    public static GlobalCommentModule deleteCommentModuleByFlagCode(String flagCode) {
        if (TextUtils.isEmpty(flagCode))
            return null;
        synchronized (GlobalVariableConfig.class) {
            GlobalCommentModule retModule = null;
            Iterator<GlobalCommentModule> iterator = mGlobalCommentModules.iterator();
            while (iterator.hasNext()) {
                GlobalCommentModule iteratorModule = iterator.next();
                if (TextUtils.equals(flagCode, iteratorModule.getFlagCode())) {
                    iterator.remove();
                    retModule = iteratorModule;
                    break;
                }
            }
            return retModule;
        }
    }

    public static void handleCommentModule(GlobalCommentModule module) {
        if (module == null)
            return;
        GlobalCommentModule contains = containsCommentModule(module.getFlagCode());
        if (contains == null) {
            addCommentModule(module);
        } else {
            updateCommentModule(module);
        }
    }

    public static void clearCommentModules() {
        mGlobalAttentionModules.clear();
    }

    public static void clearAttentionModules() {
        mGlobalAttentionModules.clear();
    }

    public static void clearFavoriteModules() {
        mGlobalFavoriteModules.clear();
    }

    public static void clearGoodModules() {
        mGlobalGoodModules.clear();
    }
}
