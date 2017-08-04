package amodule.dish.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.db.DishOffData;
import amodule.dish.db.DishOffSqlite;
import amodule.dish.db.ShowBuySqlite;
import amodule.dish.view.DishsWebView;
import amodule.main.Main;
import amodule.user.activity.MyFavorite;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2017/7/25.
 */
public class OffDishToFavoriteControl {

    /**
     * 设置收藏时是否自动离线
     * @param context
     * @param isAutoOffDish
     */
    public static void setIsAutoOffDish(Context context,boolean isAutoOffDish){
        FileManager.saveShared(context,FileManager.xmlFile_appInfo,"isAutoOffDish",isAutoOffDish ? "2" : "1");
    }

    /**
     * 获取是否收藏时是否自动离线
     * @param context
     * @return
     */
    public static boolean getIsAutoOffDish(Context context){
        Object isAutoOffDish = FileManager.loadShared(context,FileManager.xmlFile_appInfo,"isAutoOffDish");
        return "2".equals(String.valueOf(isAutoOffDish));
    }

    /**
     * 处理老版离线菜谱添加到收藏列表，并且修改成最新的离线样式：头部存数据库，中间h5存本地，以菜谱code命名
     * @param context
     */
    public static synchronized void offDishToFavorite(final Activity context) {
        try {
            if(context == null) return;
            RelativeLayout rootLayout = null;
            if (context instanceof BaseActivity) {
                rootLayout = ((BaseActivity) context).rl;
            } else if (context instanceof MainBaseActivity) {
                rootLayout = ((MainBaseActivity) context).rl;
            } else if (context instanceof Main) {
                rootLayout = ((Main) context).getRootLayout();
            }
            if (rootLayout == null) return;

            final RelativeLayout mRootLayout = rootLayout;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ShowBuySqlite oldDishSqlite = new ShowBuySqlite(context);
                    int oldDishCount = oldDishSqlite.selectCount();
                    if (oldDishCount <= 0) return;
                    final ArrayList<String> codes = oldDishSqlite.getAllCodes();
                    if(codes.size() <= 0) return;
                    final ArrayList<DishOffData> oldDishArray = oldDishSqlite.getAllDataFromDB();
                    if(oldDishArray.size() <= 0)return;
                    final DishOffSqlite dishOffSqlite = new DishOffSqlite(context);
                    mRootLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            addCollection(context, codes);
                            //同步cookie并获得webview
                            final DishsWebView webView = new DishsWebView(context);
                            webView.setOnLoadCallback(new DishsWebView.OnWebViewLoadDataCallback() {
                                int index = 0;
                                @Override
                                public void onLoadFinish() {
                                    DishOffData dishOffData = oldDishArray.get(index);
                                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(dishOffData.getJson());
                                    if(arrayList.size() > 0) {
                                        Map<String, String> offDishMap = arrayList.get(0);
                                        offDishMap.put("type", offDishMap.get("hasVideo"));
                                        offDishMap.put("isMakeImg", "2");
                                        offDishMap.put("isFine", offDishMap.get("rank"));
                                        dishOffData.setJson(StringManager.getJsonByMap(offDishMap).toString());
                                        dishOffSqlite.insert(oldDishArray.get(index));
                                        oldDishSqlite.deleteByCode(codes.get(index));
                                    }
                                    if (index + 1 < codes.size()) {
                                        webView.loadDishData(codes.get(++index));
                                    }
                                }
                            });
                            mRootLayout.addView(webView, 0, 0);
                            webView.loadDishData(codes.get(0));
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新菜谱模板
     * @param context
     * @param moudleVersion ： 最新模板版本号
     */
    public static void updataAllNedUpdataMoulderDish(final Context context, final String moudleVersion){
        try {
            if (context == null) return;
            RelativeLayout rootLayout = null;
            if (context instanceof BaseActivity) {
                rootLayout = ((BaseActivity) context).rl;
            } else if (context instanceof MainBaseActivity) {
                rootLayout = ((MainBaseActivity) context).rl;
            } else if (context instanceof Main) {
                rootLayout = ((Main) context).getRootLayout();
            }
            if (rootLayout == null) return;

            final RelativeLayout mRootLayout = rootLayout;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DishOffSqlite dishOffSqlite = new DishOffSqlite(context);
                    final ArrayList<String> codes = dishOffSqlite.getAllNedUpdataMoulderCodes(moudleVersion);
                    if (codes.size() <= 0) return;
                    mRootLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            //同步cookie并获得webview
                            final DishsWebView webView = new DishsWebView(context);
                            mRootLayout.addView(webView, 0, 0);
                            webView.loadDishData(codes);

                        }
                    });
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 添加code集合到收藏列表
     * @param context
     * @param codes
     */
    private static void addCollection(Context context, final ArrayList<String> codes) {
        StringBuffer params = new StringBuffer("codes=");
        for (String code : codes) {
            params.append(code);
            params.append(",");
        }
        ReqEncyptInternet.in().doEncypt(StringManager.api_addCollection, params.toString(), new InternetCallback(context) {
            @Override
            public void loaded(int flag, String s, Object o) {
                if(flag >= ReqInternet.REQ_OK_STRING){
                    Object offToFavHint = FileManager.loadShared(context,"offToFavHint","offToFavHint");
                    if(offToFavHint == null || TextUtils.isEmpty(String.valueOf(offToFavHint))) {
                        final XhDialog xhDialog = new XhDialog(context);
                        xhDialog.setTitle("离线菜谱已全部放入“我的收藏”中，点击查看~")
                                .setSureButton("确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        xhDialog.cancel();
                                        Intent intent = new Intent(context, MyFavorite.class);
                                        context.startActivity(intent);
                                    }
                                }).show();
                        FileManager.saveShared(context,"offToFavHint","offToFavHint","2");
                    }
                }
            }
        });
    }


}
